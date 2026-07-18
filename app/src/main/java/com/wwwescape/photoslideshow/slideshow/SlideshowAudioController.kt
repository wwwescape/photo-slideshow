package com.wwwescape.photoslideshow.slideshow

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.wwwescape.photoslideshow.data.settings.SettingsRepository
import com.wwwescape.photoslideshow.data.settings.SoundSettings
import com.wwwescape.photoslideshow.data.settings.SoundSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val TAG = "SlideshowAudio"

/**
 * Loops the selected audio behind the slideshow — either a track bundled with the app, or a
 * single file the user picked from their own device — holding proper audio focus so it pauses
 * for calls and ducks out for other apps instead of talking over them. Android-SDK-only, no
 * Activity/Compose types — shared by [com.wwwescape.photoslideshow.ui.slideshow.SlideshowActivity]
 * and the DreamService the same way [SlideshowController] is. The caller owns [scope]'s lifetime
 * and must call [stop] when done with it.
 *
 * Uses a plain [MediaPlayer] rather than a full media framework (e.g. Media3/ExoPlayer) since all
 * this needs to do is loop one local track — [MediaPlayer.setLooping] can have a brief gap at the
 * loop point on some devices, which would be the reason to revisit this choice.
 *
 * Built up manually (`MediaPlayer()` + `setDataSource` + `prepareAsync()`) rather than via the
 * [MediaPlayer.create] factory: that factory returns an *already-prepared* player, and
 * [MediaPlayer.setAudioAttributes] is only honored when called before preparing. A player left
 * on the factory's implicit default attributes never resolves to a classified usage/content type
 * at the audio-policy level and can end up silently never actually starting. Preparing
 * asynchronously keeps a slow (e.g. cloud-backed) custom file from blocking the main thread.
 */
class SlideshowAudioController(
    private val scope: CoroutineScope,
    private val context: Context,
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false
    private var focusRequest: AudioFocusRequest? = null

    /** Whether the host is in the foreground — see [setActive]. */
    private var active = true

    /** Set while another app holds transient focus (e.g. a call), so we resume on regain. */
    private var pausedForFocusLoss = false

    /** Identifies what's currently loaded, so unrelated settings changes (e.g. volume) don't tear
     * down and restart playback. */
    private var appliedKey: String? = null

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            // Permanent loss: another app took over playback — let go of the decoder and focus
            // entirely rather than sit paused holding them.
            AudioManager.AUDIOFOCUS_LOSS -> releasePlayer()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pausedForFocusLoss = true
                mediaPlayer?.takeIf { isPrepared }?.pause()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                pausedForFocusLoss = false
                if (active) mediaPlayer?.takeIf { isPrepared }?.start()
            }
        }
    }

    fun start() {
        scope.launch {
            SettingsRepository.settingsFlow(context)
                .map { it.sound }
                .distinctUntilChanged()
                .collect { applySettings(it) }
        }
    }

    /** Pauses playback while the host is in the background and resumes it on return. */
    fun setActive(active: Boolean) {
        if (this.active == active) return
        this.active = active
        val player = mediaPlayer?.takeIf { isPrepared } ?: return
        if (active && !pausedForFocusLoss) player.start() else if (!active) player.pause()
    }

    fun stop() {
        releasePlayer()
    }

    private fun applySettings(sound: SoundSettings) {
        if (!sound.musicEnabled) {
            releasePlayer()
            return
        }

        val key = sound.playbackKey()
        if (key == null) {
            // CUSTOM_FILE selected but nothing picked yet — nothing to play.
            releasePlayer()
            return
        }

        if (mediaPlayer == null || appliedKey != key) {
            releasePlayer()
            preparePlayer(sound, key)
        }
        mediaPlayer?.setVolume(sound.volume, sound.volume)
    }

    private fun preparePlayer(sound: SoundSettings, key: String) {
        if (!requestAudioFocus()) {
            Log.w(TAG, "Audio focus request denied, not starting playback")
            return
        }
        runCatching {
            val player = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                when (sound.source) {
                    SoundSource.BUNDLED_TRACK -> {
                        context.resources.openRawResourceFd(sound.track.rawResId).use {
                            setDataSource(it.fileDescriptor, it.startOffset, it.length)
                        }
                    }
                    SoundSource.CUSTOM_FILE -> {
                        setDataSource(context, Uri.parse(sound.customTrackUri))
                    }
                }
                isLooping = true
                setVolume(sound.volume, sound.volume)
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error what=$what extra=$extra")
                    releasePlayer()
                    true
                }
                setOnPreparedListener { prepared ->
                    if (prepared !== mediaPlayer) return@setOnPreparedListener
                    isPrepared = true
                    if (active && !pausedForFocusLoss) prepared.start()
                }
            }
            mediaPlayer = player
            appliedKey = key
            player.prepareAsync()
        }.onFailure { error ->
            Log.e(TAG, "Failed to prepare audio (${sound.source})", error)
            releasePlayer()
        }
    }

    private fun requestAudioFocus(): Boolean {
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setOnAudioFocusChangeListener(focusListener)
            .build()
        focusRequest = request
        return audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
        pausedForFocusLoss = false
        appliedKey = null
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }
}

/** Null when [SoundSource.CUSTOM_FILE] is selected but no file has been picked yet. */
private fun SoundSettings.playbackKey(): String? = when (source) {
    SoundSource.BUNDLED_TRACK -> "bundled:${track.name}"
    SoundSource.CUSTOM_FILE -> customTrackUri?.let { "custom:$it" }
}
