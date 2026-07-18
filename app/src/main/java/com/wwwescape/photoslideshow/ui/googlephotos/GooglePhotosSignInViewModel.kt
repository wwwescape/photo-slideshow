package com.wwwescape.photoslideshow.ui.googlephotos

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wwwescape.photoslideshow.data.googlephotos.GooglePhotosAuthRepository
import com.wwwescape.photoslideshow.data.googlephotos.GooglePhotosMediaItem
import com.wwwescape.photoslideshow.data.googlephotos.GooglePhotosPickerRepository
import com.wwwescape.photoslideshow.data.googlephotos.PickerSession
import com.wwwescape.photoslideshow.data.photos.PhotoSourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

sealed interface GooglePhotosSignInUiState {
    data object Idle : GooglePhotosSignInUiState
    data object RequestingAuthorization : GooglePhotosSignInUiState

    /** Consent hasn't been granted yet (or was revoked) — the UI must launch [pendingIntent]
     * (e.g. via `ActivityResultContracts.StartIntentSenderForResult`) and report the result back
     * through [GooglePhotosSignInViewModel.onAuthorizationActivityResult]. */
    data class NeedsAuthorization(val pendingIntent: PendingIntent) : GooglePhotosSignInUiState
    data object CreatingPickerSession : GooglePhotosSignInUiState

    /** [pickerUri] is captured once from the session-creation response and held here for the
     * duration of the wait — see [PickerSession.pickerUri] for why it can't be re-read from
     * later polls. */
    data class AwaitingPickerSelection(val session: PickerSession, val pickerUri: String) : GooglePhotosSignInUiState
    data class ReadyToImport(val session: PickerSession, val items: List<GooglePhotosMediaItem>) : GooglePhotosSignInUiState
    data object Importing : GooglePhotosSignInUiState
    data class Imported(val count: Int) : GooglePhotosSignInUiState
    data class Failed(val message: String) : GooglePhotosSignInUiState
}

/** Drives the Google Photos connect flow: (1) Play services authorization for the Picker scope —
 * silent if already granted, otherwise the UI launches a native Google consent screen — then
 * (2) a Picker API session (QR + code, meant for a phone/computer), repeatable every time the
 * user wants to add more photos. Used by
 * [com.wwwescape.photoslideshow.ui.screens.googlephotos.GooglePhotosSignInScreen]. */
class GooglePhotosSignInViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<GooglePhotosSignInUiState>(GooglePhotosSignInUiState.Idle)
    val uiState: StateFlow<GooglePhotosSignInUiState> = _uiState.asStateFlow()

    fun start() {
        viewModelScope.launch {
            _uiState.value = GooglePhotosSignInUiState.RequestingAuthorization
            val app = getApplication<Application>()
            val result = runCatching { GooglePhotosAuthRepository.authorize(app) }.getOrElse {
                _uiState.value = GooglePhotosSignInUiState.Failed(it.message ?: "Could not start sign-in")
                return@launch
            }
            if (result.hasResolution()) {
                val pendingIntent = result.pendingIntent
                if (pendingIntent == null) {
                    _uiState.value = GooglePhotosSignInUiState.Failed("Sign-in isn't available right now")
                } else {
                    _uiState.value = GooglePhotosSignInUiState.NeedsAuthorization(pendingIntent)
                }
            } else {
                startPickerSession()
            }
        }
    }

    /** Called by the UI once the intent launched from [GooglePhotosSignInUiState.NeedsAuthorization]
     * returns. [intent] is `null` if the user backed out of the consent screen. */
    fun onAuthorizationActivityResult(intent: Intent?) {
        viewModelScope.launch {
            if (intent == null) {
                _uiState.value = GooglePhotosSignInUiState.Failed("Sign-in was cancelled")
                return@launch
            }
            val app = getApplication<Application>()
            GooglePhotosAuthRepository.resolveAuthorizationResult(app, intent).getOrElse {
                _uiState.value = GooglePhotosSignInUiState.Failed(it.message ?: "Sign-in failed")
                return@launch
            }
            startPickerSession()
        }
    }

    private suspend fun startPickerSession() {
        _uiState.value = GooglePhotosSignInUiState.CreatingPickerSession
        val app = getApplication<Application>()
        val session = GooglePhotosPickerRepository.createSession(app).getOrElse {
            _uiState.value = GooglePhotosSignInUiState.Failed(it.message ?: "Could not start picking session")
            return
        }
        val pickerUri = session.pickerUri
        if (pickerUri == null) {
            _uiState.value = GooglePhotosSignInUiState.Failed("Google didn't return a picking link")
            return
        }
        _uiState.value = GooglePhotosSignInUiState.AwaitingPickerSelection(session, pickerUri)

        var current = session
        val pollIntervalMillis = (current.pollIntervalSeconds * 1000L).toLong().coerceAtLeast(1000L)
        var consecutiveFailures = 0
        // Google's picking sessions don't carry a hard expiry the client needs to enforce the
        // way device codes do; poll until the user finishes (or navigates away, which cancels
        // this coroutine via viewModelScope). Coming back from the browser after picking photos
        // resumes the app process, and Android can briefly tear down background network/DNS
        // right at that moment — a single transient failure right after resume shouldn't be
        // fatal, so tolerate a few in a row before giving up.
        while (!current.mediaItemsSet) {
            delay(pollIntervalMillis)
            val result = GooglePhotosPickerRepository.getSession(app, current.id)
            val next = result.getOrNull()
            if (next != null) {
                current = next
                consecutiveFailures = 0
            } else {
                consecutiveFailures++
                if (consecutiveFailures >= MAX_CONSECUTIVE_POLL_FAILURES) {
                    val message = result.exceptionOrNull()?.message ?: "Lost connection while waiting for selection"
                    _uiState.value = GooglePhotosSignInUiState.Failed(message)
                    return
                }
            }
        }

        val items = retrying(MAX_CONSECUTIVE_POLL_FAILURES) {
            GooglePhotosPickerRepository.listSelectedMediaItems(app, current.id)
        }.getOrElse {
            _uiState.value = GooglePhotosSignInUiState.Failed(it.message ?: "Could not list selected photos")
            return
        }
        _uiState.value = GooglePhotosSignInUiState.ReadyToImport(current, items)
    }

    /** Downloads each selected item's bytes to app-private storage *before* deleting the picking
     * session — Google only guarantees a picked item resolves while its session is alive, so
     * this can't be done lazily at display time (see [com.wwwescape.photoslideshow.data.photos.PhotoSourceType]). */
    fun confirmImport() {
        val state = _uiState.value
        if (state !is GooglePhotosSignInUiState.ReadyToImport) return
        viewModelScope.launch {
            _uiState.value = GooglePhotosSignInUiState.Importing
            val app = getApplication<Application>()
            val downloaded = withContext(Dispatchers.IO) {
                val dir = File(app.filesDir, "google_photos").apply { mkdirs() }
                state.items.mapNotNull { item ->
                    val extension = item.mimeType?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) } ?: "jpg"
                    val destination = File(dir, "${UUID.randomUUID()}.$extension")
                    GooglePhotosPickerRepository.downloadMediaItem(app, item, destination)
                        .onFailure { destination.delete() }
                        .map { Uri.fromFile(destination) to (item.filename ?: item.id) }
                        .getOrNull()
                }
            }

            if (downloaded.isEmpty()) {
                _uiState.value = GooglePhotosSignInUiState.Failed("Couldn't download any of the selected photos")
                return@launch
            }
            PhotoSourceRepository.addGooglePhotosItems(app, downloaded)
            GooglePhotosPickerRepository.deleteSession(app, state.session.id)
            _uiState.value = GooglePhotosSignInUiState.Imported(downloaded.size)
        }
    }

    fun reset() {
        _uiState.value = GooglePhotosSignInUiState.Idle
    }

    private companion object {
        const val MAX_CONSECUTIVE_POLL_FAILURES = 3

        /** Retries a single fallible call up to [maxAttempts] times with a short delay, for the
         * same transient-network-right-after-resume reason the picker-session poll loop
         * tolerates repeated failures. */
        suspend fun <T> retrying(maxAttempts: Int, block: suspend () -> Result<T>): Result<T> {
            var lastResult = block()
            var attempt = 1
            while (lastResult.isFailure && attempt < maxAttempts) {
                delay(1000L)
                lastResult = block()
                attempt++
            }
            return lastResult
        }
    }
}
