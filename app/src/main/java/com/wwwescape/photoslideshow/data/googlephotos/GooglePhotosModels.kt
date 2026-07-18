package com.wwwescape.photoslideshow.data.googlephotos

/** Whether the Google Play services [com.google.android.gms.auth.api.identity.AuthorizationClient]
 * has previously granted [com.wwwescape.photoslideshow.data.googlephotos.GooglePhotosOAuthConfig.SCOPE]
 * on this device — not "does the user have a Google account," purely local sign-in state for
 * this app, tracked so the UI can skip straight to the Picker session on repeat visits. */
sealed interface GooglePhotosAuthState {
    data object SignedOut : GooglePhotosAuthState
    data object SignedIn : GooglePhotosAuthState
}

/** A Google Photos Picker session — created on this device, completed by the user picking
 * photos at [pickerUri] (typically on their phone/computer, since that's a much better
 * experience than a TV remote). [pickerUri] is only present on the response from
 * [GooglePhotosPickerRepository.createSession] — Google's API omits it from later
 * [GooglePhotosPickerRepository.getSession] polls, so callers should hold onto the value from
 * creation rather than expect every poll to keep supplying it. */
data class PickerSession(
    val id: String,
    val pickerUri: String?,
    val pollIntervalSeconds: Double,
    val mediaItemsSet: Boolean,
)

/** A single media item the user selected in a Picker session. [baseUrl] is short-lived (expires
 * roughly an hour after issue) and only resolvable while the picking session is alive — see
 * [GooglePhotosPickerRepository.downloadMediaItem], which must be called on it before the
 * session is deleted, not lazily at display time. */
data class GooglePhotosMediaItem(
    val id: String,
    val baseUrl: String,
    val mimeType: String?,
    val filename: String?,
)
