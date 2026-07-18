package com.wwwescape.photoslideshow.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wwwescape.photoslideshow.R
import com.wwwescape.photoslideshow.data.settings.ClockPosition
import com.wwwescape.photoslideshow.data.settings.ColorTheme
import com.wwwescape.photoslideshow.data.settings.DimLevel
import com.wwwescape.photoslideshow.data.settings.SlideshowInterval
import com.wwwescape.photoslideshow.data.settings.SoundSource
import com.wwwescape.photoslideshow.data.settings.ThemeContrast
import com.wwwescape.photoslideshow.data.settings.ThemeMode
import com.wwwescape.photoslideshow.data.settings.TransitionStyle
import java.util.Locale

/** `null` means "follow the system language" ([androidx.appcompat.app.AppCompatDelegate]'s empty
 * locale list) — see the Settings screen's Language row. English is listed explicitly too since
 * the system default might not be English even though that's this app's base `values/strings.xml`. */
val SUPPORTED_APP_LANGUAGES = listOf(null, "en", "es", "fr", "hi", "pt")

@Composable
fun appLanguageLabel(languageTag: String?): String =
    if (languageTag == null) {
        stringResource(R.string.language_system_default)
    } else {
        val locale = Locale.forLanguageTag(languageTag)
        locale.getDisplayName(locale).replaceFirstChar { it.uppercase(locale) }
    }

@Composable
fun ThemeMode.label(): String = stringResource(
    when (this) {
        ThemeMode.LIGHT -> R.string.theme_mode_light
        ThemeMode.DARK -> R.string.theme_mode_dark
        ThemeMode.SYSTEM -> R.string.theme_mode_system
    },
)

@Composable
fun ColorTheme.label(): String = stringResource(
    when (this) {
        ColorTheme.DEFAULT -> R.string.color_theme_default
        ColorTheme.OCEAN -> R.string.color_theme_ocean
        ColorTheme.FOREST -> R.string.color_theme_forest
        ColorTheme.SUNSET -> R.string.color_theme_sunset
        ColorTheme.GRAPE -> R.string.color_theme_grape
        ColorTheme.ROSE -> R.string.color_theme_rose
        ColorTheme.SLATE -> R.string.color_theme_slate
        ColorTheme.GOLD -> R.string.color_theme_gold
        ColorTheme.TEAL -> R.string.color_theme_teal
        ColorTheme.PLUM -> R.string.color_theme_plum
        ColorTheme.MOSS -> R.string.color_theme_moss
        ColorTheme.CORAL -> R.string.color_theme_coral
        ColorTheme.INDIGO -> R.string.color_theme_indigo
        ColorTheme.MUSTARD -> R.string.color_theme_mustard
        ColorTheme.CRIMSON -> R.string.color_theme_crimson
        ColorTheme.MINT -> R.string.color_theme_mint
        ColorTheme.PERIWINKLE -> R.string.color_theme_periwinkle
    },
)

@Composable
fun ThemeContrast.label(): String = stringResource(
    when (this) {
        ThemeContrast.STANDARD -> R.string.contrast_standard
        ThemeContrast.MEDIUM -> R.string.contrast_medium
        ThemeContrast.HIGH -> R.string.contrast_high
    },
)

@Composable
fun SlideshowInterval.label(): String = stringResource(
    when (this) {
        SlideshowInterval.FIVE_SECONDS -> R.string.interval_5s
        SlideshowInterval.TEN_SECONDS -> R.string.interval_10s
        SlideshowInterval.THIRTY_SECONDS -> R.string.interval_30s
        SlideshowInterval.ONE_MINUTE -> R.string.interval_1m
    },
)

@Composable
fun TransitionStyle.label(): String = stringResource(
    when (this) {
        TransitionStyle.CROSSFADE -> R.string.transition_crossfade
        TransitionStyle.KEN_BURNS -> R.string.transition_ken_burns
    },
)

@Composable
fun DimLevel.label(): String = stringResource(
    when (this) {
        DimLevel.OFF -> R.string.dim_level_off
        DimLevel.LOW -> R.string.dim_level_low
        DimLevel.MEDIUM -> R.string.dim_level_medium
        DimLevel.HIGH -> R.string.dim_level_high
    },
)

@Composable
fun ClockPosition.label(): String = stringResource(
    when (this) {
        ClockPosition.TOP_LEFT -> R.string.position_top_left
        ClockPosition.TOP_CENTER -> R.string.position_top_center
        ClockPosition.TOP_RIGHT -> R.string.position_top_right
        ClockPosition.CENTER_LEFT -> R.string.position_center_left
        ClockPosition.CENTER -> R.string.position_center
        ClockPosition.CENTER_RIGHT -> R.string.position_center_right
        ClockPosition.BOTTOM_LEFT -> R.string.position_bottom_left
        ClockPosition.BOTTOM_CENTER -> R.string.position_bottom_center
        ClockPosition.BOTTOM_RIGHT -> R.string.position_bottom_right
    },
)

@Composable
fun SoundSource.label(): String = stringResource(
    when (this) {
        SoundSource.BUNDLED_TRACK -> R.string.sound_source_bundled
        SoundSource.CUSTOM_FILE -> R.string.sound_source_custom_file
    },
)
