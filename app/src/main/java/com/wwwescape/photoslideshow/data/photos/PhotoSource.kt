package com.wwwescape.photoslideshow.data.photos

import android.net.Uri

enum class PhotoSourceType { PICKED_ITEM, FOLDER_TREE, GOOGLE_PHOTOS_ITEM }

/**
 * A single user-selected photo (from the system Photo Picker, which already surfaces
 * Google-Photos-backed cloud images alongside local ones), a whole folder/album tree (from
 * the Storage Access Framework, which also covers other cloud providers — Drive, Dropbox,
 * OneDrive — that expose a documents provider), or a photo picked through the Google Photos
 * Picker API ([PhotoSourceType.GOOGLE_PHOTOS_ITEM]). Google Photos items are downloaded once at
 * import time to app-private storage (Google's Picker API only guarantees a picked item's bytes
 * are resolvable while its picking session is still alive — see
 * [com.wwwescape.photoslideshow.data.googlephotos.GooglePhotosPickerRepository.downloadMediaItem]),
 * so their [uri] is an ordinary local `file://` one from then on, same as any other source.
 */
data class PhotoSource(
    val uri: Uri,
    val type: PhotoSourceType,
    val displayName: String,
)
