package com.wwwescape.photoslideshow.ui.screens.settings

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wwwescape.photoslideshow.data.music.MusicTrack
import com.wwwescape.photoslideshow.data.settings.AppSettings
import com.wwwescape.photoslideshow.data.settings.ClockPosition
import com.wwwescape.photoslideshow.data.settings.ColorTheme
import com.wwwescape.photoslideshow.data.settings.DimLevel
import com.wwwescape.photoslideshow.data.settings.SettingsRepository
import com.wwwescape.photoslideshow.data.settings.SlideshowInterval
import com.wwwescape.photoslideshow.data.settings.SoundSource
import com.wwwescape.photoslideshow.data.settings.ThemeContrast
import com.wwwescape.photoslideshow.data.settings.ThemeMode
import com.wwwescape.photoslideshow.data.settings.TransitionStyle
import com.wwwescape.photoslideshow.util.queryDisplayName
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val settings: StateFlow<AppSettings> = SettingsRepository.settingsFlow(application)
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { SettingsRepository.setThemeMode(getApplication(), mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { SettingsRepository.setDynamicColor(getApplication(), enabled) }
    }

    fun setColorTheme(theme: ColorTheme) {
        viewModelScope.launch { SettingsRepository.setColorTheme(getApplication(), theme) }
    }

    fun setThemeContrast(contrast: ThemeContrast) {
        viewModelScope.launch { SettingsRepository.setThemeContrast(getApplication(), contrast) }
    }

    fun setPureDark(enabled: Boolean) {
        viewModelScope.launch { SettingsRepository.setPureDark(getApplication(), enabled) }
    }

    fun setAbsoluteDark(enabled: Boolean) {
        viewModelScope.launch { SettingsRepository.setAbsoluteDark(getApplication(), enabled) }
    }

    fun setInterval(interval: SlideshowInterval) {
        viewModelScope.launch { SettingsRepository.setInterval(getApplication(), interval) }
    }

    fun setShuffle(enabled: Boolean) {
        viewModelScope.launch { SettingsRepository.setShuffle(getApplication(), enabled) }
    }

    fun setShowCaptions(enabled: Boolean) {
        viewModelScope.launch { SettingsRepository.setShowCaptions(getApplication(), enabled) }
    }

    fun setTransitionStyle(style: TransitionStyle) {
        viewModelScope.launch { SettingsRepository.setTransitionStyle(getApplication(), style) }
    }

    fun setDimLevel(level: DimLevel) {
        viewModelScope.launch { SettingsRepository.setDimLevel(getApplication(), level) }
    }

    fun setShowClock(enabled: Boolean) {
        viewModelScope.launch { SettingsRepository.setShowClock(getApplication(), enabled) }
    }

    fun setShowClockDate(enabled: Boolean) {
        viewModelScope.launch { SettingsRepository.setShowClockDate(getApplication(), enabled) }
    }

    fun setShowClockTime(enabled: Boolean) {
        viewModelScope.launch { SettingsRepository.setShowClockTime(getApplication(), enabled) }
    }

    fun setClockPosition(position: ClockPosition) {
        viewModelScope.launch { SettingsRepository.setClockPosition(getApplication(), position) }
    }

    fun setClockOpacity(opacity: Float) {
        viewModelScope.launch { SettingsRepository.setClockOpacity(getApplication(), opacity) }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch { SettingsRepository.setMusicEnabled(getApplication(), enabled) }
    }

    fun setSoundSource(source: SoundSource) {
        viewModelScope.launch { SettingsRepository.setSoundSource(getApplication(), source) }
    }

    fun setMusicTrack(track: MusicTrack) {
        viewModelScope.launch { SettingsRepository.setMusicTrack(getApplication(), track) }
    }

    /** Persists read access to the picked file so playback keeps working across app restarts —
     * the picker only grants it for this call unless we ask to keep it. */
    fun setCustomTrack(uri: Uri) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            runCatching {
                app.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            // Drop the grant on the previously picked file — the app no longer needs it, and
            // persisted grants are a capped per-app resource.
            settings.value.sound.customTrackUri
                ?.takeIf { it != uri.toString() }
                ?.let { previous ->
                    runCatching {
                        app.contentResolver.releasePersistableUriPermission(Uri.parse(previous), Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
            val name = queryDisplayName(app, uri) ?: uri.lastPathSegment ?: uri.toString()
            SettingsRepository.setCustomTrack(app, uri.toString(), name)
        }
    }

    fun setMusicVolume(volume: Float) {
        viewModelScope.launch { SettingsRepository.setMusicVolume(getApplication(), volume) }
    }
}
