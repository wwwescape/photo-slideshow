<p align="center">
  <img src="assets/logo.svg" alt="Photo Slideshow logo" width="120" />
</p>

<h1 align="center">Photo Slideshow</h1>

<p align="center">
  A photo slideshow and screen saver app for Android phones and tablets, built with Material 3
  Expressive to feel at home alongside Google's own Pixel apps. No backend, no analytics — the
  only sign-in is an optional Google Photos connection.
</p>

<p align="center">
  <a href="https://github.com/wwwescape/photo-slideshow/releases"><img src="https://img.shields.io/github/v/release/wwwescape/photo-slideshow.svg?style=flat-square" alt="GitHub release" /></a>
  <a href="https://github.com/wwwescape/photo-slideshow/commits/master"><img src="https://img.shields.io/github/last-commit/wwwescape/photo-slideshow.svg?style=flat-square" alt="GitHub last commit" /></a>
  <a href="https://github.com/wwwescape/photo-slideshow"><img src="https://img.shields.io/github/languages/code-size/wwwescape/photo-slideshow.svg?color=red&style=flat-square" alt="GitHub code size" /></a>
</p>

## Features

- **Flexible photo sources** — pick individual photos with the Android Photo Picker, whole
  folders or albums through the Storage Access Framework (works with Google Drive, Dropbox,
  OneDrive, and any other documents provider), or Google Photos through the Google Photos
  Picker API.
- **Slideshows** — full screen, with crossfade or Ken Burns pan & zoom, configurable slide
  duration, shuffle, optional file-name captions, and an adjustable dim overlay.
- **Clock & date overlay** — optional, with adjustable position and opacity.
- **Background music** — bundled royalty-free tracks or any audio file on your device, with
  adjustable volume. Pauses automatically for calls and other apps.
- **Android screen saver** — start a slideshow yourself, or enable Photo Slideshow as your Screen
  Saver (Daydream) to start it automatically while charging, docked, or both.
- **Material 3 Expressive design** — light, dark, and system themes, dynamic color (Material
  You), 16+ curated color themes, adjustable contrast, and Pure Black / Absolute Black modes.
- **Languages** — English, Spanish, French, Hindi, and Portuguese.
- **Light on battery** — photo changes and music pause when the slideshow isn't on screen, and
  Ken Burns switches to a crossfade while Battery Saver is on.

## Installation

Download the APK from the [latest release](https://github.com/wwwescape/photo-slideshow/releases/latest)
and install it on your device.

Requires Android 8.0 (API 26) or newer.

## Privacy

Photo Slideshow has no backend, no analytics, and no crash-reporting SDKs. Local photo access is
limited to exactly what you choose through the Photo Picker or the Storage Access Framework, so
no broad storage permission is needed.

Google Photos is entirely opt-in and only shares the photos you select in each picker session.
The only network traffic is directly between your device and Google's own sign-in and Photos
Picker services, and only your preferences are included in Android backups. See the in-app
Privacy statement (**Settings → About**) for the full breakdown.

## Development

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (Narwhal or newer)
- JDK 17+ (bundled with Android Studio)
- An Android device or emulator running Android 8.0 (API 26) or newer

### Build & run

```bash
git clone https://github.com/wwwescape/photo-slideshow.git
cd photo-slideshow
./gradlew installDebug
```

Or open the project in Android Studio and run the `app` configuration.

### Google Photos setup

Google Photos needs a Google Cloud OAuth client before it works — see the setup steps and scope
in `app/src/main/java/com/wwwescape/photoslideshow/data/googlephotos/GooglePhotosOAuthConfig.kt`.

### Test

```bash
./gradlew lint testDebugUnitTest
```

### Release a new version

Bump `versionCode` and `versionName` in `app/build.gradle.kts`, commit, then:

```bash
git tag v0.1.0
git push origin v0.1.0
```

The tag push builds a signed release APK and AAB and attaches them to a new GitHub Release (see
`.github/workflows/release.yml`). It needs the `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
`KEY_ALIAS`, and `KEY_PASSWORD` repository secrets, which are kept locally in the gitignored
`keystore.properties`.

### Project layout

```
app/       Kotlin, Jetpack Compose (Material 3), single module
  data/        Settings (DataStore), photo sources, and the Google Photos OAuth/Picker client
  slideshow/   Slideshow engine (photo order, auto-advance), shared by the app and the screen saver
  dream/       DreamService that runs the slideshow as a screen saver
  ui/          Compose screens, navigation, and theme
design/    Source logo and Play Store icon assets
assets/    README assets
```

## License

GPL-3.0 — see [LICENSE](LICENSE).

## Support

If you find Photo Slideshow useful, consider buying me a coffee:

[<img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" height="40" />](https://buymeacoffee.com/wwwescape)
