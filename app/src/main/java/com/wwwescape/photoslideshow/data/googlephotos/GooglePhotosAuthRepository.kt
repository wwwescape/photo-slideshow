package com.wwwescape.photoslideshow.data.googlephotos

import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.googlePhotosAuthDataStore by preferencesDataStore(name = "google_photos_auth")

/**
 * Google Photos sign-in via Play services' [com.google.android.gms.auth.api.identity.AuthorizationClient]
 * (see [GooglePhotosOAuthConfig] for why this replaces the OAuth device-authorization grant).
 * Play services itself owns token caching/refresh against the device's signed-in Google account —
 * this repository holds no access/refresh tokens of its own, just a local "have we completed this
 * at least once" flag for fast UI state.
 *
 * [getValidAccessToken] is the call every Google Photos API request goes through: it's silent
 * (never shows UI) and returns `null` if interactive consent is required. Interactive consent can
 * only be resolved from a screen holding an `Activity`/launcher — see [authorize] and
 * [resolveAuthorizationResult], used by [com.wwwescape.photoslideshow.ui.googlephotos.GooglePhotosSignInViewModel].
 */
object GooglePhotosAuthRepository {

    private val SIGNED_IN_KEY = booleanPreferencesKey("signed_in")
    private val scope = Scope(GooglePhotosOAuthConfig.SCOPE)

    fun authStateFlow(context: Context): Flow<GooglePhotosAuthState> =
        context.googlePhotosAuthDataStore.data.map { prefs ->
            if (prefs[SIGNED_IN_KEY] == true) GooglePhotosAuthState.SignedIn else GooglePhotosAuthState.SignedOut
        }

    suspend fun isSignedIn(context: Context): Boolean =
        context.googlePhotosAuthDataStore.data.first()[SIGNED_IN_KEY] == true

    /** Requests authorization for [GooglePhotosOAuthConfig.SCOPE]. Never shows UI itself —
     * inspect [AuthorizationResult.hasResolution] on the result: `false` means already granted
     * (accessToken is populated), `true` means the caller must launch
     * `AuthorizationResult.getPendingIntent()` (e.g. via
     * `ActivityResultContracts.StartIntentSenderForResult`) and pass the resulting [Intent] to
     * [resolveAuthorizationResult]. */
    suspend fun authorize(context: Context): AuthorizationResult {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(scope))
            .build()
        return Identity.getAuthorizationClient(context).authorize(request).await()
    }

    /** Completes the interactive consent flow after the launched intent returns, and records
     * the local "signed in" flag on success. */
    suspend fun resolveAuthorizationResult(context: Context, intent: Intent): Result<AuthorizationResult> =
        runCatching {
            Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(intent)
        }.onSuccess { markSignedIn(context) }

    /** The single call site every Google Photos API request should use — a silent, no-UI check.
     * Returns `null` if consent hasn't been granted yet (or was revoked); callers in that case
     * should surface the interactive sign-in flow rather than fail silently forever. */
    suspend fun getValidAccessToken(context: Context): String? {
        val result = runCatching { authorize(context) }.getOrNull() ?: return null
        return if (!result.hasResolution()) result.accessToken else null
    }

    suspend fun signOut(context: Context) {
        context.googlePhotosAuthDataStore.edit { it[SIGNED_IN_KEY] = false }
        runCatching {
            val request = RevokeAccessRequest.builder().setScopes(listOf(scope)).build()
            Identity.getAuthorizationClient(context).revokeAccess(request).await()
        }
    }

    private suspend fun markSignedIn(context: Context) {
        context.googlePhotosAuthDataStore.edit { it[SIGNED_IN_KEY] = true }
    }
}
