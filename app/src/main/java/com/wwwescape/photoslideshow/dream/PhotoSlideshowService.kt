package com.wwwescape.photoslideshow.dream

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import android.service.dreams.DreamService
import com.wwwescape.photoslideshow.data.settings.AppSettings
import com.wwwescape.photoslideshow.data.settings.SettingsRepository
import com.wwwescape.photoslideshow.data.settings.ThemeMode
import com.wwwescape.photoslideshow.slideshow.SlideshowAudioController
import com.wwwescape.photoslideshow.slideshow.SlideshowController
import com.wwwescape.photoslideshow.ui.slideshow.SlideshowContent
import com.wwwescape.photoslideshow.ui.theme.PhotoSlideshowTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Hosts the slideshow as Android's native screen saver ("Daydream"). The user opts in once from
 * system Settings > Display > Screen saver — deep-linked from our own Settings screen via
 * [android.provider.Settings.ACTION_DREAM_SETTINGS] — where they also choose the
 * charging/docked/either start condition; this service only needs to render once the system
 * decides to start it, it never polls charging state itself.
 *
 * A DreamService isn't a ComponentActivity/Fragment, so it isn't a [LifecycleOwner],
 * [ViewModelStoreOwner], or [SavedStateRegistryOwner] out of the box, and Compose requires all
 * three on the view tree — this wires up minimal versions of each by hand, scoped to the dream's
 * own on/off lifecycle.
 */
class PhotoSlideshowService : DreamService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    override val viewModelStore = ViewModelStore()

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var dreamScope: CoroutineScope? = null
    private var controller: SlideshowController? = null
    private var audioController: SlideshowAudioController? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        isInteractive = false
        isFullscreen = true

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        dreamScope = scope
        val slideshowController = SlideshowController(scope, applicationContext)
        controller = slideshowController
        slideshowController.start()

        val slideshowAudioController = SlideshowAudioController(scope, applicationContext)
        audioController = slideshowAudioController
        slideshowAudioController.start()

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setViewTreeLifecycleOwner(this@PhotoSlideshowService)
            setViewTreeViewModelStoreOwner(this@PhotoSlideshowService)
            setViewTreeSavedStateRegistryOwner(this@PhotoSlideshowService)
            setContent {
                val uiState by slideshowController.uiState.collectAsState()
                val settings by remember { SettingsRepository.settingsFlow(applicationContext) }
                    .collectAsState(initial = AppSettings())
                val systemInDarkTheme = isSystemInDarkTheme()
                val darkTheme = when (settings.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> systemInDarkTheme
                }

                PhotoSlideshowTheme(
                    darkTheme = darkTheme,
                    dynamicColor = settings.useDynamicColor,
                    colorTheme = settings.colorTheme,
                    themeContrast = settings.themeContrast,
                    pureDark = settings.pureDark,
                    absoluteDark = settings.absoluteDark,
                ) {
                    SlideshowContent(uiState = uiState, modifier = Modifier.fillMaxSize())
                }
            }
        }
        setContentView(composeView)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onDreamingStopped() {
        tearDown()
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        tearDown()
        super.onDetachedFromWindow()
    }

    /** Idempotent: the system doesn't always deliver both callbacks, so either one fully releases
     * the advance timer, the media player/audio focus and the composition's ViewModels. */
    private fun tearDown() {
        controller?.stop()
        controller = null
        audioController?.stop()
        audioController = null
        dreamScope?.cancel()
        dreamScope = null
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            viewModelStore.clear()
        }
    }
}
