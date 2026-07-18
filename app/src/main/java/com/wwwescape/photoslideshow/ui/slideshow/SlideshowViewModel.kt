package com.wwwescape.photoslideshow.ui.slideshow

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wwwescape.photoslideshow.slideshow.SlideshowAudioController
import com.wwwescape.photoslideshow.slideshow.SlideshowController
import com.wwwescape.photoslideshow.slideshow.SlideshowUiState
import kotlinx.coroutines.flow.StateFlow

class SlideshowViewModel(application: Application) : AndroidViewModel(application) {

    private val controller = SlideshowController(viewModelScope, application)
    private val audioController = SlideshowAudioController(viewModelScope, application)
    val uiState: StateFlow<SlideshowUiState> = controller.uiState

    init {
        controller.start()
        audioController.start()
    }

    /** Pauses auto-advance and music while [SlideshowActivity] is not visible. */
    fun setActive(active: Boolean) {
        controller.setActive(active)
        audioController.setActive(active)
    }

    override fun onCleared() {
        controller.stop()
        audioController.stop()
    }
}
