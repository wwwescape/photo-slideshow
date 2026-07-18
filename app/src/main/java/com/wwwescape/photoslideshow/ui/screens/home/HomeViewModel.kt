package com.wwwescape.photoslideshow.ui.screens.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wwwescape.photoslideshow.data.photos.PhotoSourceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val sourceCount: Int = 0,
    val previewUris: List<Uri> = emptyList(),
    val isLoadingPreview: Boolean = true,
)

private const val PREVIEW_LIMIT = 24

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = PhotoSourceRepository.sourcesFlow(application)
        .flatMapLatest { sources ->
            flow {
                emit(HomeUiState(sourceCount = sources.size, isLoadingPreview = true))
                val preview = PhotoSourceRepository.resolveImageUris(application).shuffled().take(PREVIEW_LIMIT)
                emit(HomeUiState(sourceCount = sources.size, previewUris = preview, isLoadingPreview = false))
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())
}
