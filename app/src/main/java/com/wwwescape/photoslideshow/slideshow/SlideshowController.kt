package com.wwwescape.photoslideshow.slideshow

import android.content.Context
import android.net.Uri
import com.wwwescape.photoslideshow.data.photos.PhotoSourceRepository
import com.wwwescape.photoslideshow.data.settings.SettingsRepository
import com.wwwescape.photoslideshow.data.settings.SlideshowSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class SlideshowUiState(
    val currentUri: Uri? = null,
    val settings: SlideshowSettings = SlideshowSettings(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
)

/**
 * Drives the slideshow's photo order and auto-advance timing. Deliberately Android-SDK-only (no
 * Activity/Compose types) so it can be driven from different hosts — a plain full-screen
 * Activity and a DreamService window — without duplicating this logic in each. The caller owns
 * [scope]'s lifetime (a ViewModel's viewModelScope, or a scope the DreamService cancels itself)
 * and must call [stop] when done with it.
 *
 * The advance timer only runs while the host is visible — see [setActive] — so a backgrounded
 * slideshow doesn't keep waking the CPU to swap photos nobody can see.
 */
class SlideshowController(
    private val scope: CoroutineScope,
    private val context: Context,
) {
    private val _uiState = MutableStateFlow(SlideshowUiState())
    val uiState: StateFlow<SlideshowUiState> = _uiState.asStateFlow()

    private var photos: List<Uri> = emptyList()
    private var index = 0
    private var advanceJob: Job? = null
    private var active = true

    fun start() {
        scope.launch {
            // Only slideshow settings matter here — theme/sound edits elsewhere must not reshuffle
            // the deck or reset the advance timer.
            SettingsRepository.settingsFlow(context)
                .map { it.slideshow }
                .distinctUntilChanged()
                .collect { settings ->
                    val previous = _uiState.value.settings
                    _uiState.value = _uiState.value.copy(settings = settings)
                    if (settings.shuffle != previous.shuffle) {
                        reorder(settings.shuffle)
                        _uiState.value = _uiState.value.copy(currentUri = photos.getOrNull(index))
                    }
                    if (settings.interval != previous.interval) restartAdvanceLoop()
                }
        }
        scope.launch {
            photos = PhotoSourceRepository.resolveImageUris(context)
            reorder(_uiState.value.settings.shuffle)
            _uiState.value = _uiState.value.copy(
                currentUri = photos.getOrNull(0),
                isLoading = false,
                isEmpty = photos.isEmpty(),
            )
            restartAdvanceLoop()
        }
    }

    fun advance() = move(1)

    fun previous() = move(-1)

    /** Pauses ([active] = false) or resumes auto-advance, e.g. when the hosting screen leaves or
     * returns to the foreground. */
    fun setActive(active: Boolean) {
        if (this.active == active) return
        this.active = active
        if (active) restartAdvanceLoop() else advanceJob?.cancel()
    }

    fun stop() {
        advanceJob?.cancel()
    }

    private fun move(step: Int) {
        if (photos.isEmpty()) return
        index = (index + step).mod(photos.size)
        _uiState.value = _uiState.value.copy(currentUri = photos[index])
    }

    private fun reorder(shuffle: Boolean) {
        if (photos.isEmpty()) return
        photos = if (shuffle) photos.shuffled() else photos.sortedBy { it.toString() }
        index = 0
    }

    private fun restartAdvanceLoop() {
        advanceJob?.cancel()
        if (photos.isEmpty() || !active) return
        val intervalMillis = _uiState.value.settings.interval.seconds * 1000L
        advanceJob = scope.launch {
            while (true) {
                delay(intervalMillis)
                advance()
            }
        }
    }
}
