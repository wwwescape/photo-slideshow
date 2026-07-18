package com.wwwescape.photoslideshow.ui.slideshow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.PowerManager
import android.text.format.DateFormat
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.wwwescape.photoslideshow.R
import com.wwwescape.photoslideshow.data.settings.ClockPosition
import com.wwwescape.photoslideshow.data.settings.DimLevel
import com.wwwescape.photoslideshow.data.settings.SlideshowSettings
import com.wwwescape.photoslideshow.data.settings.TransitionStyle
import com.wwwescape.photoslideshow.slideshow.SlideshowUiState
import com.wwwescape.photoslideshow.util.queryDisplayName
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val KEN_BURNS_DURATION_MILLIS = 12_000

/** Extra bottom clearance a bottom-anchored clock reserves so it never collides with the
 * single-line caption, which always lives in the very bottom-start corner. */
private val CAPTION_CLEARANCE = 40.dp

/** The full-bleed slideshow surface shared by the manual-trigger [SlideshowActivity] and the
 * automatic-trigger PhotoSlideshowService window. */
@Composable
fun SlideshowContent(uiState: SlideshowUiState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        when {
            uiState.isEmpty -> EmptySlideshowMessage()
            uiState.isLoading -> LoadingIndicator()
            else -> {
                // Ken Burns redraws every frame for the whole interval; fall back to a static
                // crossfade while Battery Saver is on.
                val kenBurns = uiState.settings.transitionStyle == TransitionStyle.KEN_BURNS && !rememberPowerSaveMode()
                Crossfade(targetState = uiState.currentUri, label = "slideshow-photo") { uri ->
                    if (kenBurns) {
                        KenBurnsImage(uri)
                    } else {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                if (uiState.settings.dimLevel != DimLevel.OFF) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = uiState.settings.dimLevel.alpha)),
                    )
                }
                if (uiState.settings.showCaptions) {
                    Caption(uri = uiState.currentUri)
                }
                if (uiState.settings.showClock) {
                    ClockOverlay(settings = uiState.settings)
                }
            }
        }
    }
}

@Composable
private fun KenBurnsImage(uri: Uri?) {
    val scale = remember(uri) { Animatable(1f) }
    LaunchedEffect(uri) {
        scale.snapTo(1f)
        scale.animateTo(1.15f, animationSpec = tween(durationMillis = KEN_BURNS_DURATION_MILLIS, easing = LinearEasing))
    }
    AsyncImage(
        model = uri,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        // Lambda form reads the animated value in the draw phase only, so each frame is a cheap
        // layer re-draw instead of a full recomposition.
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
    )
}

/** A quiet caption in the same visual language as [ClockOverlay] — a soft text shadow for
 * legibility rather than a solid background bar. */
@Composable
private fun BoxScope.Caption(uri: Uri?) {
    val context = LocalContext.current
    var displayName by remember(uri) { mutableStateOf<String?>(null) }
    LaunchedEffect(uri) {
        displayName = uri?.let { queryDisplayName(context, it) }
    }
    if (displayName.isNullOrBlank()) return

    Text(
        text = displayName.orEmpty(),
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp,
        maxLines = 1,
        softWrap = false,
        style = MaterialTheme.typography.labelLarge.copy(
            shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), blurRadius = 10f),
        ),
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(horizontal = 32.dp, vertical = 28.dp)
            .fillMaxWidth()
            .basicMarquee(),
    )
}

/** Styled after the Pixel launcher's "At a Glance" widget: a small date sitting right above a
 * large time, both centered as one block — rather than a single line of body text. */
@Composable
private fun BoxScope.ClockOverlay(settings: SlideshowSettings) {
    if (!settings.showClockDate && !settings.showClockTime) return

    val context = LocalContext.current
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        // Wake once per minute, right on the boundary — accurate to the minute shown, with the
        // fewest possible wakeups.
        while (true) {
            val current = LocalDateTime.now()
            delay(60_000L - (current.second * 1000L + current.nano / 1_000_000L) + 50L)
            now = LocalDateTime.now()
        }
    }

    val textColor = Color.White.copy(alpha = settings.clockOpacity)
    val shadow = Shadow(color = Color.Black.copy(alpha = settings.clockOpacity * 0.7f), blurRadius = 18f)
    val is24Hour = DateFormat.is24HourFormat(context)
    val timeFormatter = remember(is24Hour) { DateTimeFormatter.ofPattern(if (is24Hour) "H:mm" else "h:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val isBottomAnchored = settings.clockPosition in BOTTOM_POSITIONS
    val bottomPadding = if (isBottomAnchored && settings.showCaptions) 28.dp + CAPTION_CLEARANCE else 28.dp

    Column(
        horizontalAlignment = settings.clockPosition.horizontalAlignment(),
        modifier = Modifier
            .align(settings.clockPosition.toAlignment())
            .padding(horizontal = 32.dp)
            .padding(top = 28.dp, bottom = bottomPadding),
    ) {
        if (settings.showClockDate) {
            Text(
                text = now.format(dateFormatter),
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                textAlign = settings.clockPosition.textAlign(),
                style = MaterialTheme.typography.titleMedium.copy(shadow = shadow),
            )
        }
        if (settings.showClockTime) {
            Text(
                text = now.format(timeFormatter),
                color = textColor,
                fontSize = 76.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 80.sp,
                textAlign = settings.clockPosition.textAlign(),
                style = MaterialTheme.typography.displayLarge.copy(shadow = shadow),
            )
        }
    }
}

private val BOTTOM_POSITIONS = setOf(ClockPosition.BOTTOM_LEFT, ClockPosition.BOTTOM_CENTER, ClockPosition.BOTTOM_RIGHT)

private fun ClockPosition.horizontalAlignment(): Alignment.Horizontal = when (this) {
    ClockPosition.TOP_LEFT, ClockPosition.CENTER_LEFT, ClockPosition.BOTTOM_LEFT -> Alignment.Start
    ClockPosition.TOP_CENTER, ClockPosition.CENTER, ClockPosition.BOTTOM_CENTER -> Alignment.CenterHorizontally
    ClockPosition.TOP_RIGHT, ClockPosition.CENTER_RIGHT, ClockPosition.BOTTOM_RIGHT -> Alignment.End
}

private fun ClockPosition.toAlignment(): Alignment = when (this) {
    ClockPosition.TOP_LEFT -> Alignment.TopStart
    ClockPosition.TOP_CENTER -> Alignment.TopCenter
    ClockPosition.TOP_RIGHT -> Alignment.TopEnd
    ClockPosition.CENTER_LEFT -> Alignment.CenterStart
    ClockPosition.CENTER -> Alignment.Center
    ClockPosition.CENTER_RIGHT -> Alignment.CenterEnd
    ClockPosition.BOTTOM_LEFT -> Alignment.BottomStart
    ClockPosition.BOTTOM_CENTER -> Alignment.BottomCenter
    ClockPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
}

private fun ClockPosition.textAlign(): TextAlign = when (this) {
    ClockPosition.TOP_LEFT, ClockPosition.CENTER_LEFT, ClockPosition.BOTTOM_LEFT -> TextAlign.Start
    ClockPosition.TOP_CENTER, ClockPosition.CENTER, ClockPosition.BOTTOM_CENTER -> TextAlign.Center
    ClockPosition.TOP_RIGHT, ClockPosition.CENTER_RIGHT, ClockPosition.BOTTOM_RIGHT -> TextAlign.End
}

@Composable
private fun BoxScope.LoadingIndicator() {
    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
}

@Composable
private fun BoxScope.EmptySlideshowMessage() {
    Text(
        text = stringResource(R.string.slideshow_empty),
        color = Color.White,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .align(Alignment.Center)
            .padding(32.dp),
    )
}

/** Tracks the system Battery Saver state, updating live if the user toggles it mid-slideshow. */
@Composable
private fun rememberPowerSaveMode(): Boolean {
    val context = LocalContext.current
    val powerManager = remember(context) { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    var powerSave by remember { mutableStateOf(powerManager.isPowerSaveMode) }
    DisposableEffect(powerManager) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                powerSave = powerManager.isPowerSaveMode
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        powerSave = powerManager.isPowerSaveMode
        onDispose { context.unregisterReceiver(receiver) }
    }
    return powerSave
}
