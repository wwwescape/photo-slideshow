package com.wwwescape.photoslideshow.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wwwescape.photoslideshow.data.music.MusicTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** Mirrors [ThemeMode] outside DataStore, which is Flow-only/async. [MainActivity] reads this
 * synchronously in `attachBaseContext`, before any Compose or DataStore code can run, so the
 * window's initial (pre-Compose) theme resolution honors the user's choice instead of always
 * following the raw system day/night setting. */
const val THEME_PREFS_NAME = "theme_prefs_sync"
const val THEME_MODE_PREF_KEY = "theme_mode"

object SettingsRepository {

    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
    private val COLOR_THEME_KEY = stringPreferencesKey("color_theme")
    private val THEME_CONTRAST_KEY = stringPreferencesKey("theme_contrast")
    private val PURE_DARK_KEY = booleanPreferencesKey("pure_dark")
    private val ABSOLUTE_DARK_KEY = booleanPreferencesKey("absolute_dark")
    private val INTERVAL_KEY = stringPreferencesKey("slideshow_interval")
    private val SHUFFLE_KEY = booleanPreferencesKey("slideshow_shuffle")
    private val CAPTIONS_KEY = booleanPreferencesKey("slideshow_captions")
    private val TRANSITION_KEY = stringPreferencesKey("slideshow_transition")
    private val DIM_LEVEL_KEY = stringPreferencesKey("slideshow_dim_level")
    private val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")
    private val SOUND_SOURCE_KEY = stringPreferencesKey("sound_source")
    private val MUSIC_TRACK_KEY = stringPreferencesKey("music_track")
    private val CUSTOM_TRACK_URI_KEY = stringPreferencesKey("custom_track_uri")
    private val CUSTOM_TRACK_NAME_KEY = stringPreferencesKey("custom_track_name")
    private val MUSIC_VOLUME_KEY = floatPreferencesKey("music_volume")
    private val SHOW_CLOCK_KEY = booleanPreferencesKey("show_clock")
    private val SHOW_CLOCK_DATE_KEY = booleanPreferencesKey("show_clock_date")
    private val SHOW_CLOCK_TIME_KEY = booleanPreferencesKey("show_clock_time")
    private val CLOCK_POSITION_KEY = stringPreferencesKey("clock_position")
    private val CLOCK_OPACITY_KEY = floatPreferencesKey("clock_opacity")

    fun settingsFlow(context: Context): Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[THEME_MODE_KEY].toEnumOrDefault(ThemeMode.SYSTEM),
            useDynamicColor = prefs[DYNAMIC_COLOR_KEY] ?: true,
            colorTheme = prefs[COLOR_THEME_KEY].toEnumOrDefault(ColorTheme.DEFAULT),
            themeContrast = prefs[THEME_CONTRAST_KEY].toEnumOrDefault(ThemeContrast.STANDARD),
            pureDark = prefs[PURE_DARK_KEY] ?: false,
            absoluteDark = prefs[ABSOLUTE_DARK_KEY] ?: false,
            slideshow = SlideshowSettings(
                interval = prefs[INTERVAL_KEY].toEnumOrDefault(SlideshowInterval.TEN_SECONDS),
                shuffle = prefs[SHUFFLE_KEY] ?: true,
                showCaptions = prefs[CAPTIONS_KEY] ?: false,
                transitionStyle = prefs[TRANSITION_KEY].toEnumOrDefault(TransitionStyle.KEN_BURNS),
                dimLevel = prefs[DIM_LEVEL_KEY].toEnumOrDefault(DimLevel.OFF),
                showClock = prefs[SHOW_CLOCK_KEY] ?: false,
                showClockDate = prefs[SHOW_CLOCK_DATE_KEY] ?: true,
                showClockTime = prefs[SHOW_CLOCK_TIME_KEY] ?: true,
                clockPosition = prefs[CLOCK_POSITION_KEY].toEnumOrDefault(ClockPosition.BOTTOM_CENTER),
                clockOpacity = prefs[CLOCK_OPACITY_KEY] ?: 1f,
            ),
            sound = SoundSettings(
                musicEnabled = prefs[MUSIC_ENABLED_KEY] ?: false,
                source = prefs[SOUND_SOURCE_KEY].toEnumOrDefault(SoundSource.BUNDLED_TRACK),
                track = prefs[MUSIC_TRACK_KEY].toEnumOrDefault(MusicTrack.RENEW),
                customTrackUri = prefs[CUSTOM_TRACK_URI_KEY],
                customTrackName = prefs[CUSTOM_TRACK_NAME_KEY],
                volume = prefs[MUSIC_VOLUME_KEY] ?: 0.5f,
            ),
        )
    }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.settingsDataStore.edit { it[THEME_MODE_KEY] = mode.name }
        context.getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(THEME_MODE_PREF_KEY, mode.name).apply()
    }

    suspend fun setDynamicColor(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[DYNAMIC_COLOR_KEY] = enabled }
    }

    suspend fun setColorTheme(context: Context, theme: ColorTheme) {
        context.settingsDataStore.edit { it[COLOR_THEME_KEY] = theme.name }
    }

    suspend fun setThemeContrast(context: Context, contrast: ThemeContrast) {
        context.settingsDataStore.edit { it[THEME_CONTRAST_KEY] = contrast.name }
    }

    suspend fun setPureDark(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[PURE_DARK_KEY] = enabled }
    }

    suspend fun setAbsoluteDark(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[ABSOLUTE_DARK_KEY] = enabled }
    }

    suspend fun setInterval(context: Context, interval: SlideshowInterval) {
        context.settingsDataStore.edit { it[INTERVAL_KEY] = interval.name }
    }

    suspend fun setShuffle(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[SHUFFLE_KEY] = enabled }
    }

    suspend fun setShowCaptions(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[CAPTIONS_KEY] = enabled }
    }

    suspend fun setTransitionStyle(context: Context, style: TransitionStyle) {
        context.settingsDataStore.edit { it[TRANSITION_KEY] = style.name }
    }

    suspend fun setDimLevel(context: Context, level: DimLevel) {
        context.settingsDataStore.edit { it[DIM_LEVEL_KEY] = level.name }
    }

    suspend fun setMusicEnabled(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[MUSIC_ENABLED_KEY] = enabled }
    }

    suspend fun setSoundSource(context: Context, source: SoundSource) {
        context.settingsDataStore.edit { it[SOUND_SOURCE_KEY] = source.name }
    }

    suspend fun setMusicTrack(context: Context, track: MusicTrack) {
        context.settingsDataStore.edit { it[MUSIC_TRACK_KEY] = track.name }
    }

    suspend fun setCustomTrack(context: Context, uri: String, displayName: String) {
        context.settingsDataStore.edit {
            it[CUSTOM_TRACK_URI_KEY] = uri
            it[CUSTOM_TRACK_NAME_KEY] = displayName
        }
    }

    suspend fun setMusicVolume(context: Context, volume: Float) {
        context.settingsDataStore.edit { it[MUSIC_VOLUME_KEY] = volume }
    }

    suspend fun setShowClock(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[SHOW_CLOCK_KEY] = enabled }
    }

    suspend fun setShowClockDate(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[SHOW_CLOCK_DATE_KEY] = enabled }
    }

    suspend fun setShowClockTime(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[SHOW_CLOCK_TIME_KEY] = enabled }
    }

    suspend fun setClockPosition(context: Context, position: ClockPosition) {
        context.settingsDataStore.edit { it[CLOCK_POSITION_KEY] = position.name }
    }

    suspend fun setClockOpacity(context: Context, opacity: Float) {
        context.settingsDataStore.edit { it[CLOCK_OPACITY_KEY] = opacity }
    }

    private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
        this?.let { name -> runCatching { enumValueOf<T>(name) }.getOrNull() } ?: default
}
