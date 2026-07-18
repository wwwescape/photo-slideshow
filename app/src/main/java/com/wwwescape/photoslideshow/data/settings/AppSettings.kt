package com.wwwescape.photoslideshow.data.settings

import com.wwwescape.photoslideshow.data.music.MusicTrack

enum class ThemeMode { LIGHT, DARK, SYSTEM }

/** [seedHue] is a 0-359 HSV hue driving [com.wwwescape.photoslideshow.ui.theme.generateColorScheme].
 * DEFAULT (null) keeps the app's own hand-authored static palette (see Color.kt) instead of a
 * generated one. Ignored entirely when dynamic color is on, which takes precedence. */
enum class ColorTheme(val seedHue: Float?) {
    DEFAULT(null),
    OCEAN(205f),
    FOREST(140f),
    SUNSET(25f),
    GRAPE(280f),
    ROSE(340f),
    SLATE(220f),
    GOLD(45f),
    TEAL(175f),
    PLUM(300f),
    MOSS(95f),
    CORAL(12f),
    INDIGO(245f),
    MUSTARD(55f),
    CRIMSON(355f),
    MINT(160f),
    PERIWINKLE(230f),
}

/** Only affects (or only makes sense for) dark theme. */
enum class ThemeContrast { STANDARD, MEDIUM, HIGH }

enum class TransitionStyle { CROSSFADE, KEN_BURNS }

enum class DimLevel(val alpha: Float) {
    OFF(0f),
    LOW(0.15f),
    MEDIUM(0.35f),
    HIGH(0.6f),
}

/** How long each photo stays on screen before advancing. */
enum class SlideshowInterval(val seconds: Int) {
    FIVE_SECONDS(5),
    TEN_SECONDS(10),
    THIRTY_SECONDS(30),
    ONE_MINUTE(60),
}

/** Where the clock overlay sits on screen — the full 3x3 alignment grid. */
enum class ClockPosition {
    TOP_LEFT, TOP_CENTER, TOP_RIGHT,
    CENTER_LEFT, CENTER, CENTER_RIGHT,
    BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT,
}

data class SlideshowSettings(
    val interval: SlideshowInterval = SlideshowInterval.TEN_SECONDS,
    val shuffle: Boolean = true,
    val showCaptions: Boolean = false,
    val transitionStyle: TransitionStyle = TransitionStyle.KEN_BURNS,
    val dimLevel: DimLevel = DimLevel.OFF,
    val showClock: Boolean = false,
    val showClockDate: Boolean = true,
    val showClockTime: Boolean = true,
    val clockPosition: ClockPosition = ClockPosition.BOTTOM_CENTER,
    val clockOpacity: Float = 1f,
)

/** Where the looped audio comes from — one of the tracks bundled with the app, or a single
 * file the user picked from their own device. (Streaming services like Spotify/YouTube Music
 * are a possible future source, not implemented yet.) */
enum class SoundSource { BUNDLED_TRACK, CUSTOM_FILE }

/** Off by default — the slideshow should stay silent until the user opts in. Governs both the
 * manual slideshow and the automatic screen saver alike. */
data class SoundSettings(
    val musicEnabled: Boolean = false,
    val source: SoundSource = SoundSource.BUNDLED_TRACK,
    val track: MusicTrack = MusicTrack.RENEW,
    val customTrackUri: String? = null,
    val customTrackName: String? = null,
    val volume: Float = 0.5f,
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val colorTheme: ColorTheme = ColorTheme.DEFAULT,
    val themeContrast: ThemeContrast = ThemeContrast.STANDARD,
    val pureDark: Boolean = false,
    val absoluteDark: Boolean = false,
    val slideshow: SlideshowSettings = SlideshowSettings(),
    val sound: SoundSettings = SoundSettings(),
)
