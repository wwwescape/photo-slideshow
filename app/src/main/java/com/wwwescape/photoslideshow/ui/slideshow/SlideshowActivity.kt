package com.wwwescape.photoslideshow.ui.slideshow

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wwwescape.photoslideshow.ui.theme.PhotoSlideshowTheme

/** Manual-trigger, full-screen immersive slideshow — the "Start Slideshow" entry point from
 * Home. Tapping anywhere exits back to the app on phone; a TV remote's Back button already
 * finishes the activity via the platform default, and DPAD_CENTER/Enter is wired here as the
 * remote's equivalent of "tap anywhere". */
class SlideshowActivity : ComponentActivity() {

    private val viewModel: SlideshowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val focusRequester = remember { FocusRequester() }

            PhotoSlideshowTheme {
                SlideshowContent(
                    uiState = uiState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = { finish() })
                        .focusRequester(focusRequester)
                        .focusable()
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyUp &&
                                (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                    event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
                            ) {
                                finish()
                                true
                            } else {
                                false
                            }
                        },
                )
            }
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }

    // Stop the advance timer and music whenever the slideshow is off screen (Home pressed, screen
    // turned off) instead of letting them run until the activity is finally destroyed.
    override fun onStart() {
        super.onStart()
        viewModel.setActive(true)
    }

    override fun onStop() {
        viewModel.setActive(false)
        super.onStop()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
