package com.wwwescape.photoslideshow.ui.screens.sources

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wwwescape.photoslideshow.data.photos.PhotoSource
import com.wwwescape.photoslideshow.data.photos.PhotoSourceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SourcesViewModel(application: Application) : AndroidViewModel(application) {

    val sources: StateFlow<List<PhotoSource>> = PhotoSourceRepository.sourcesFlow(application)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addPickedItems(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch { PhotoSourceRepository.addPickedItems(getApplication(), uris) }
    }

    fun addFolderTree(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch { PhotoSourceRepository.addFolderTree(getApplication(), uri) }
    }

    fun removeSource(source: PhotoSource) {
        viewModelScope.launch { PhotoSourceRepository.removeSource(getApplication(), source) }
    }
}
