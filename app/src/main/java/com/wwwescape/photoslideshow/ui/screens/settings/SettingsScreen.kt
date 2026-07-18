package com.wwwescape.photoslideshow.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tonality
import androidx.compose.material.icons.rounded.Transform
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wwwescape.photoslideshow.R
import com.wwwescape.photoslideshow.data.music.MusicTrack
import com.wwwescape.photoslideshow.data.settings.AppSettings
import com.wwwescape.photoslideshow.data.settings.ClockPosition
import com.wwwescape.photoslideshow.data.settings.ColorTheme
import com.wwwescape.photoslideshow.data.settings.DimLevel
import com.wwwescape.photoslideshow.data.settings.SlideshowInterval
import com.wwwescape.photoslideshow.data.settings.SoundSource
import com.wwwescape.photoslideshow.data.settings.ThemeContrast
import com.wwwescape.photoslideshow.data.settings.ThemeMode
import com.wwwescape.photoslideshow.data.settings.TransitionStyle
import com.wwwescape.photoslideshow.util.openUrl

private val SettingsRowHeight = 72.dp

private const val PRIVACY_POLICY_URL = "https://www.ericppereira.co.in/apps/photo-slideshow/privacy-policy.html"

@Composable
fun SettingsScreen(
    onNavigateToLicenses: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    SettingsScreenContent(
        settings = settings,
        onThemeModeSelected = viewModel::setThemeMode,
        onDynamicColorChanged = viewModel::setDynamicColor,
        onColorThemeSelected = viewModel::setColorTheme,
        onThemeContrastSelected = viewModel::setThemeContrast,
        onPureDarkChanged = viewModel::setPureDark,
        onAbsoluteDarkChanged = viewModel::setAbsoluteDark,
        onIntervalSelected = viewModel::setInterval,
        onShuffleChanged = viewModel::setShuffle,
        onShowCaptionsChanged = viewModel::setShowCaptions,
        onTransitionStyleSelected = viewModel::setTransitionStyle,
        onDimLevelSelected = viewModel::setDimLevel,
        onShowClockChanged = viewModel::setShowClock,
        onShowClockDateChanged = viewModel::setShowClockDate,
        onShowClockTimeChanged = viewModel::setShowClockTime,
        onClockPositionSelected = viewModel::setClockPosition,
        onClockOpacityChanged = viewModel::setClockOpacity,
        onMusicEnabledChanged = viewModel::setMusicEnabled,
        onSoundSourceSelected = viewModel::setSoundSource,
        onMusicTrackSelected = viewModel::setMusicTrack,
        onCustomTrackPicked = viewModel::setCustomTrack,
        onMusicVolumeChanged = viewModel::setMusicVolume,
        onNavigateToLicenses = onNavigateToLicenses,
        modifier = modifier,
    )
}

@Composable
private fun SettingsScreenContent(
    settings: AppSettings,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onColorThemeSelected: (ColorTheme) -> Unit,
    onThemeContrastSelected: (ThemeContrast) -> Unit,
    onPureDarkChanged: (Boolean) -> Unit,
    onAbsoluteDarkChanged: (Boolean) -> Unit,
    onIntervalSelected: (SlideshowInterval) -> Unit,
    onShuffleChanged: (Boolean) -> Unit,
    onShowCaptionsChanged: (Boolean) -> Unit,
    onTransitionStyleSelected: (TransitionStyle) -> Unit,
    onDimLevelSelected: (DimLevel) -> Unit,
    onShowClockChanged: (Boolean) -> Unit,
    onShowClockDateChanged: (Boolean) -> Unit,
    onShowClockTimeChanged: (Boolean) -> Unit,
    onClockPositionSelected: (ClockPosition) -> Unit,
    onClockOpacityChanged: (Float) -> Unit,
    onMusicEnabledChanged: (Boolean) -> Unit,
    onSoundSourceSelected: (SoundSource) -> Unit,
    onMusicTrackSelected: (MusicTrack) -> Unit,
    onCustomTrackPicked: (Uri) -> Unit,
    onMusicVolumeChanged: (Float) -> Unit,
    onNavigateToLicenses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "—"
    }
    val isDarkTheme = when (settings.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val currentLanguageTag = AppCompatDelegate.getApplicationLocales().toLanguageTags().ifEmpty { null }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorThemeDialog by remember { mutableStateOf(false) }
    var showContrastDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showIntervalDialog by remember { mutableStateOf(false) }
    var showTransitionDialog by remember { mutableStateOf(false) }
    var showDimDialog by remember { mutableStateOf(false) }
    var showClockPositionDialog by remember { mutableStateOf(false) }
    var showTrackDialog by remember { mutableStateOf(false) }
    var showSoundSourceDialog by remember { mutableStateOf(false) }
    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let(onCustomTrackPicked) },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionLabel(stringResource(R.string.section_appearance))
        SettingsGroupCard {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ToggleRow(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(R.string.setting_dynamic_color),
                    subtitle = stringResource(R.string.setting_dynamic_color_subtitle),
                    checked = settings.useDynamicColor,
                    onCheckedChange = onDynamicColorChanged,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
            NavigationRow(
                icon = Icons.Rounded.Palette,
                title = stringResource(R.string.setting_color_theme),
                subtitle = settings.colorTheme.label(),
                onClick = { showColorThemeDialog = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            NavigationRow(
                icon = Icons.Rounded.DarkMode,
                title = stringResource(R.string.setting_theme),
                subtitle = settings.themeMode.label(),
                onClick = { showThemeDialog = true },
            )
            // Contrast/pure dark/absolute dark all only affect (or only make sense for) dark
            // theme — hidden entirely rather than shown-but-inert when the effective theme
            // (accounting for "System default" actually resolving to light) is light.
            if (isDarkTheme) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                NavigationRow(
                    icon = Icons.Rounded.Contrast,
                    title = stringResource(R.string.setting_theme_contrast),
                    subtitle = settings.themeContrast.label(),
                    onClick = { showContrastDialog = true },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleRow(
                    icon = Icons.Rounded.Brightness6,
                    title = stringResource(R.string.setting_pure_dark),
                    subtitle = stringResource(R.string.setting_pure_dark_subtitle),
                    checked = settings.pureDark,
                    onCheckedChange = onPureDarkChanged,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleRow(
                    icon = Icons.Rounded.NightsStay,
                    title = stringResource(R.string.setting_absolute_dark),
                    subtitle = stringResource(R.string.setting_absolute_dark_subtitle),
                    checked = settings.absoluteDark,
                    onCheckedChange = onAbsoluteDarkChanged,
                )
            }
        }

        SectionLabel(stringResource(R.string.section_slideshow))
        SettingsGroupCard {
            NavigationRow(
                icon = Icons.Rounded.Timer,
                title = stringResource(R.string.setting_interval),
                subtitle = settings.slideshow.interval.label(),
                onClick = { showIntervalDialog = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ToggleRow(
                icon = Icons.Rounded.Shuffle,
                title = stringResource(R.string.setting_shuffle),
                subtitle = null,
                checked = settings.slideshow.shuffle,
                onCheckedChange = onShuffleChanged,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ToggleRow(
                icon = Icons.Rounded.Subtitles,
                title = stringResource(R.string.setting_captions),
                subtitle = null,
                checked = settings.slideshow.showCaptions,
                onCheckedChange = onShowCaptionsChanged,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            NavigationRow(
                icon = Icons.Rounded.Transform,
                title = stringResource(R.string.setting_transition),
                subtitle = settings.slideshow.transitionStyle.label(),
                onClick = { showTransitionDialog = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            NavigationRow(
                icon = Icons.Rounded.Tonality,
                title = stringResource(R.string.setting_dim_level),
                subtitle = settings.slideshow.dimLevel.label(),
                onClick = { showDimDialog = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ToggleRow(
                icon = Icons.Rounded.AccessTime,
                title = stringResource(R.string.setting_show_clock),
                subtitle = stringResource(R.string.setting_show_clock_subtitle),
                checked = settings.slideshow.showClock,
                onCheckedChange = onShowClockChanged,
            )
            if (settings.slideshow.showClock) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleRow(
                    icon = Icons.Rounded.AccessTime,
                    title = stringResource(R.string.setting_show_date),
                    subtitle = null,
                    checked = settings.slideshow.showClockDate,
                    onCheckedChange = onShowClockDateChanged,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ToggleRow(
                    icon = Icons.Rounded.AccessTime,
                    title = stringResource(R.string.setting_show_time),
                    subtitle = null,
                    checked = settings.slideshow.showClockTime,
                    onCheckedChange = onShowClockTimeChanged,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                NavigationRow(
                    icon = Icons.Rounded.PushPin,
                    title = stringResource(R.string.setting_clock_position),
                    subtitle = settings.slideshow.clockPosition.label(),
                    onClick = { showClockPositionDialog = true },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                SliderRow(
                    icon = Icons.Rounded.Opacity,
                    value = settings.slideshow.clockOpacity,
                    onValueChange = onClockOpacityChanged,
                )
            }
        }

        SectionLabel(stringResource(R.string.section_sound))
        SettingsGroupCard {
            ToggleRow(
                icon = Icons.Rounded.MusicNote,
                title = stringResource(R.string.setting_background_music),
                subtitle = stringResource(R.string.setting_background_music_subtitle),
                checked = settings.sound.musicEnabled,
                onCheckedChange = onMusicEnabledChanged,
            )
            if (settings.sound.musicEnabled) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                NavigationRow(
                    icon = Icons.Rounded.GraphicEq,
                    title = stringResource(R.string.setting_sound_source),
                    subtitle = settings.sound.source.label(),
                    onClick = { showSoundSourceDialog = true },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                when (settings.sound.source) {
                    SoundSource.BUNDLED_TRACK -> NavigationRow(
                        icon = Icons.Rounded.MusicNote,
                        title = stringResource(R.string.setting_track),
                        subtitle = settings.sound.track.displayName,
                        onClick = { showTrackDialog = true },
                    )
                    SoundSource.CUSTOM_FILE -> NavigationRow(
                        icon = Icons.Rounded.AudioFile,
                        title = stringResource(R.string.setting_audio_file),
                        subtitle = settings.sound.customTrackName ?: stringResource(R.string.setting_audio_file_none),
                        onClick = { pickAudioLauncher.launch(arrayOf("audio/*")) },
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                SliderRow(
                    icon = Icons.AutoMirrored.Rounded.VolumeUp,
                    value = settings.sound.volume,
                    onValueChange = onMusicVolumeChanged,
                )
                if (settings.sound.source == SoundSource.BUNDLED_TRACK) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    CreditRow(
                        text = settings.sound.track.creditLine,
                        onClick = { openUrl(context, settings.sound.track.creditUrl) },
                    )
                }
            }
        }

        SectionLabel(stringResource(R.string.section_automatic_trigger))
        SettingsGroupCard {
            NavigationRow(
                icon = Icons.Rounded.Nightlight,
                title = stringResource(R.string.setting_screen_saver),
                subtitle = stringResource(R.string.setting_screen_saver_subtitle),
                onClick = { openDreamSettings(context) },
            )
        }

        SectionLabel(stringResource(R.string.section_general))
        SettingsGroupCard {
            NavigationRow(
                icon = Icons.Rounded.Language,
                title = stringResource(R.string.setting_language),
                subtitle = appLanguageLabel(currentLanguageTag),
                onClick = { showLanguageDialog = true },
            )
        }

        SectionLabel(stringResource(R.string.section_about))
        SettingsGroupCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_app_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = stringResource(R.string.about_version, versionName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Text(
                text = stringResource(R.string.privacy_statement_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            NavigationRow(
                icon = Icons.Rounded.Shield,
                title = stringResource(R.string.section_privacy_policy),
                subtitle = null,
                trailingIcon = Icons.AutoMirrored.Rounded.OpenInNew,
                onClick = { openUrl(context, PRIVACY_POLICY_URL) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            NavigationRow(
                icon = Icons.Rounded.Code,
                title = stringResource(R.string.section_open_source_licenses),
                subtitle = stringResource(R.string.settings_row_licenses_subtitle),
                onClick = onNavigateToLicenses,
            )
        }
    }

    if (showThemeDialog) {
        SettingsPickerDialog(
            title = stringResource(R.string.setting_theme),
            options = ThemeMode.entries,
            selected = settings.themeMode,
            label = { it.label() },
            onSelect = onThemeModeSelected,
            onDismiss = { showThemeDialog = false },
        )
    }
    if (showColorThemeDialog) {
        ThemePickerDialog(
            selected = settings.colorTheme,
            onSelect = onColorThemeSelected,
            onDismiss = { showColorThemeDialog = false },
        )
    }
    if (showContrastDialog) {
        SettingsPickerDialog(
            title = stringResource(R.string.setting_theme_contrast),
            options = ThemeContrast.entries,
            selected = settings.themeContrast,
            label = { it.label() },
            onSelect = onThemeContrastSelected,
            onDismiss = { showContrastDialog = false },
        )
    }
    if (showLanguageDialog) {
        SettingsPickerDialog(
            title = stringResource(R.string.setting_language),
            options = SUPPORTED_APP_LANGUAGES,
            selected = currentLanguageTag,
            label = { appLanguageLabel(it) },
            onSelect = { tag ->
                AppCompatDelegate.setApplicationLocales(
                    if (tag == null) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag),
                )
            },
            onDismiss = { showLanguageDialog = false },
        )
    }
    if (showIntervalDialog) {
        SettingsPickerDialog(
            title = stringResource(R.string.setting_interval),
            options = SlideshowInterval.entries,
            selected = settings.slideshow.interval,
            label = { it.label() },
            onSelect = onIntervalSelected,
            onDismiss = { showIntervalDialog = false },
        )
    }
    if (showTransitionDialog) {
        SettingsPickerDialog(
            title = stringResource(R.string.setting_transition),
            options = TransitionStyle.entries,
            selected = settings.slideshow.transitionStyle,
            label = { it.label() },
            onSelect = onTransitionStyleSelected,
            onDismiss = { showTransitionDialog = false },
        )
    }
    if (showDimDialog) {
        SettingsPickerDialog(
            title = stringResource(R.string.setting_dim_level),
            options = DimLevel.entries,
            selected = settings.slideshow.dimLevel,
            label = { it.label() },
            onSelect = onDimLevelSelected,
            onDismiss = { showDimDialog = false },
        )
    }
    if (showClockPositionDialog) {
        SettingsPickerDialog(
            title = stringResource(R.string.setting_clock_position),
            options = ClockPosition.entries,
            selected = settings.slideshow.clockPosition,
            label = { it.label() },
            onSelect = onClockPositionSelected,
            onDismiss = { showClockPositionDialog = false },
        )
    }
    if (showTrackDialog) {
        SettingsPickerDialog(
            title = stringResource(R.string.setting_track),
            options = MusicTrack.entries,
            selected = settings.sound.track,
            label = { it.displayName },
            onSelect = onMusicTrackSelected,
            onDismiss = { showTrackDialog = false },
        )
    }
    if (showSoundSourceDialog) {
        SettingsPickerDialog(
            title = stringResource(R.string.setting_sound_source),
            options = SoundSource.entries,
            selected = settings.sound.source,
            label = { it.label() },
            onSelect = onSoundSourceSelected,
            onDismiss = { showSoundSourceDialog = false },
        )
    }
}

/** Deep-links to the system screen saver picker so the user can enable Photo Slideshow and choose
 * its charging/docked/either start condition — this app has no way to enroll itself. */
private fun openDreamSettings(context: Context) {
    val intent = Intent(Settings.ACTION_DREAM_SETTINGS)
    runCatching { context.startActivity(intent) }
}

@Composable
private fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
    )
}

@Composable
private fun RowIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SettingsRowHeight),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            RowIcon(icon)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SliderRow(
    icon: ImageVector,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SettingsRowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowIcon(icon)
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        )
    }
}

/** A compact, non-chevron row for third-party credit text that links out to the source. */
@Composable
private fun CreditRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun NavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    trailingIcon: ImageVector = Icons.Rounded.ChevronRight,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(SettingsRowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowIcon(icon)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
