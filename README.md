<p align="center">
  <img src="assets/logo.svg" alt="Photo Slideshow logo" width="120" />
</p>

<h1 align="center">Photo Slideshow</h1>

<p align="center">
  A photo slideshow and screen saver app for Android phones and tablets, visually aligned with
  first-party Google Pixel apps (Material 3 Expressive). No backend, no analytics — the only
  account involved is an optional, user-initiated Google Photos sign-in.
</p>

## Features

- **Photo sources, local or cloud** — pick individual photos through the system Photo Picker
  (which already surfaces Google-Photos-backed images alongside local ones), hand over a whole
  folder/album via the Storage Access Framework (works with any cloud provider that exposes a
  documents provider — Drive, Dropbox, OneDrive, and more), or connect a Google Photos account
  directly via the Google Photos Picker API — sign-in uses Play services' account authorization,
  then a picker session (code + QR) completed in your browser.
- **Slideshow** — full-screen, crossfade or Ken Burns pan-zoom, configurable duration, shuffle,
  optional file-name captions, adjustable dim overlay.
- **Manual or automatic** — start it yourself from the app, or enable Photo Slideshow as your
  Android screen saver ("Daydream") and let the system start it automatically while charging,
  docked, or both — configured entirely from system Settings, no battery-polling in the app.
- **Material 3 Expressive design** — light/dark mode, dynamic color (Material You), rounded
  tonal surfaces.
- No backend, no analytics or crash-reporting SDKs. Local photo access is scoped to exactly
  what you pick, with no storage permission. Google Photos access is opt-in, OAuth-based, and
  limited to the specific items you select through Google's own picker each time — Photo Slideshow
  never gets broad library access.

## Prerequisites

- Android Studio (Narwhal or newer): https://developer.android.com/studio
- JDK 17+ (bundled with Android Studio)
- An Android device or emulator running API 26 (Android 8.0) or newer

## Build & run

```
git clone https://github.com/wwwescape/photo-slideshow.git
cd photo-slideshow
```

Open the project in Android Studio and run the `app` configuration, or from the command line:

```
./gradlew installDebug
```

## Test

```
./gradlew lint testDebugUnitTest connectedDebugAndroidTest
```

`connectedDebugAndroidTest` needs a connected device or running emulator.

## Release a new version

```
git tag v0.1.0
git push origin v0.1.0
```

That tag push builds a signed release APK and AAB and attaches them to an auto-generated
GitHub Release. See `.github/workflows/release.yml`; it needs the
`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` repository secrets set
(see `keystore.properties`, which is gitignored and holds these locally).

## Project layout

```
app/       Kotlin, Jetpack Compose (Material 3), single module
  data/        Settings (DataStore), photo-source repository, and Google Photos OAuth/Picker
               API repositories (data/googlephotos/)
  slideshow/   Android-SDK-only slideshow engine (photo order, auto-advance) — shared by the
               in-app Activity and the DreamService
  dream/       DreamService: hosts the slideshow as an automatic screen saver
  ui/          Compose screens/navigation/theme
design/    Source logo and Play Store icon assets
assets/    README/repo assets, screenshots
```

## Google Photos setup

Google Photos integration needs a Google Cloud OAuth client before it'll work — see the setup
steps and scope in `app/src/main/java/com/wwwescape/photoslideshow/data/googlephotos/GooglePhotosOAuthConfig.kt`.

## Roadmap

- Photo frame "kiosk" addon for a dedicated always-on display

## Privacy

Photo Slideshow collects nothing itself, and sends nothing to any server it controls — the only
network activity is talking directly to Google's own OAuth/Photos Picker APIs, and only if you
choose to connect a Google Photos account. See the in-app Privacy statement (Settings → About)
for the full breakdown of what's accessed and why.

## License

GPL-3.0 — see `LICENSE`.

## Support

If you find Photo Slideshow useful, consider buying me a coffee:

[<img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" height="40" />](https://buymeacoffee.com/wwwescape)
