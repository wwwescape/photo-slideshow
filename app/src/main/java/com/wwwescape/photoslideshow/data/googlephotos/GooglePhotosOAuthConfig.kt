package com.wwwescape.photoslideshow.data.googlephotos

/**
 * Google Photos access uses Google Play services' [com.google.android.gms.auth.api.identity.AuthorizationClient]
 * ("Authorize access to Google user data" — https://developer.android.com/identity/authorization),
 * not the OAuth device-authorization ("TVs and Limited Input Devices") grant: that grant type only
 * supports a small fixed allow-list of scopes (openid/email/profile, limited Drive, YouTube) and
 * Google's servers reject anything else — including the Photos Picker scope — with `invalid_scope`.
 *
 * AuthorizationClient resolves the caller's own registered OAuth client automatically from the
 * app's package name + signing certificate, so there's no client ID/secret to embed in code here.
 * That lookup is keyed on the exact package name below — this app has been renamed twice
 * (`com.wwwescape.photodream` → `com.wwwescape.pixelphotoslideshow` → the current
 * `com.wwwescape.photoslideshow`), so the OAuth client registered under either older package name
 * does *not* carry over. A new Android OAuth client must be registered for this package name before
 * sign-in will work again. To register that client:
 * 1. https://console.cloud.google.com/ → your project → APIs & Services → Credentials →
 *    Create Credentials → OAuth client ID → Application type **Android**.
 * 2. Package name: `com.wwwescape.photoslideshow`.
 * 3. SHA-1 certificate fingerprint of whichever keystore signs the build you're testing
 *    (`keytool -list -v -keystore <path>`) — the debug keystore for local testing/sideloading,
 *    the release keystore's fingerprint too once you cut signed builds. Multiple fingerprints
 *    can be added to the same Android OAuth client.
 * 4. No client secret is issued for this client type — it isn't needed on-device.
 *
 * The app's OAuth consent screen (Google Auth Platform) test-user list from the earlier device-flow
 * setup still applies here unchanged.
 */
internal object GooglePhotosOAuthConfig {
    /** Read-only access to the media items a user has explicitly selected via the Picker API —
     * does not grant broad library access. */
    const val SCOPE: String = "https://www.googleapis.com/auth/photospicker.mediaitems.readonly"
}
