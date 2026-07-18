package com.wwwescape.photoslideshow.data.photos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

private val Context.photoSourcesDataStore by preferencesDataStore(name = "photo_sources")

// Unit separator (U+001F): won't appear in a content:// Uri or a real filename, unlike '|' or ','.
private val RECORD_SEPARATOR = 0x1F.toChar()
private const val MAX_FOLDER_DEPTH = 4

object PhotoSourceRepository {

    private val SOURCES_KEY = stringSetPreferencesKey("sources")

    /** Every source the user has added, in no particular order. */
    fun sourcesFlow(context: Context): Flow<List<PhotoSource>> =
        context.photoSourcesDataStore.data.map { prefs ->
            (prefs[SOURCES_KEY] ?: emptySet()).mapNotNull { it.toSourceOrNull() }
        }

    /** Persists persistable read access for photos returned by the system Photo Picker. */
    suspend fun addPickedItems(context: Context, uris: List<Uri>) {
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        addRecords(context, uris.map { PhotoSource(it, PhotoSourceType.PICKED_ITEM, it.lastPathSegmentOrSelf()) })
    }

    /** Persists persistable access to a whole folder/album tree returned by SAF's document-tree
     * picker, covering local folders and any cloud provider exposing a documents provider. */
    suspend fun addFolderTree(context: Context, treeUri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val name = DocumentFile.fromTreeUri(context, treeUri)?.name ?: treeUri.lastPathSegmentOrSelf()
        addRecords(context, listOf(PhotoSource(treeUri, PhotoSourceType.FOLDER_TREE, name)))
    }

    /** Persists sources for photos downloaded from the Google Photos Picker API — each pair is
     * the local `file://` Uri the bytes were saved to (see
     * [com.wwwescape.photoslideshow.data.googlephotos.GooglePhotosPickerRepository.downloadMediaItem])
     * and the display name to show. No [android.content.ContentResolver] permission grant is
     * needed for these — they're ordinary app-private files, not a SAF/picker grant. */
    suspend fun addGooglePhotosItems(context: Context, downloadedItems: List<Pair<Uri, String>>) {
        addRecords(
            context,
            downloadedItems.map { (uri, displayName) ->
                PhotoSource(uri = uri, type = PhotoSourceType.GOOGLE_PHOTOS_ITEM, displayName = displayName)
            },
        )
    }

    suspend fun removeSource(context: Context, source: PhotoSource) {
        context.photoSourcesDataStore.edit { prefs ->
            val current = prefs[SOURCES_KEY] ?: emptySet()
            prefs[SOURCES_KEY] = current.filterNot { it.toSourceOrNull()?.uri == source.uri }.toSet()
        }
        when (source.type) {
            PhotoSourceType.GOOGLE_PHOTOS_ITEM -> {
                // Our own downloaded copy — the Uri-grant release below is a no-op for it, so
                // clean up the actual file ourselves instead of leaking storage.
                source.uri.path?.let { runCatching { File(it).delete() } }
            }
            else -> runCatching {
                context.contentResolver.releasePersistableUriPermission(source.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }

    /** Resolves every configured source down to concrete, directly-loadable image Uris —
     * individual picked items pass through as-is; folder trees are re-walked live (bounded
     * depth) every call, so newly added photos in an existing folder show up automatically
     * without re-adding the source. */
    suspend fun resolveImageUris(context: Context): List<Uri> = withContext(Dispatchers.IO) {
        sourcesFlow(context).first().flatMap { source ->
            when (source.type) {
                PhotoSourceType.PICKED_ITEM, PhotoSourceType.GOOGLE_PHOTOS_ITEM -> listOf(source.uri)
                PhotoSourceType.FOLDER_TREE -> runCatching {
                    val rootId = DocumentsContract.getTreeDocumentId(source.uri)
                    mutableListOf<Uri>().also { walkImages(context, source.uri, rootId, depth = 0, into = it) }
                }.getOrDefault(emptyList())
            }
        }
    }

    /** One `ContentResolver` query per directory, projecting only the id and MIME type —
     * [DocumentFile.listFiles] would instead issue a separate provider query for every child's
     * `isFile`/`type`/`isDirectory`, which is slow and expensive on large (or cloud-backed)
     * folders. */
    private fun walkImages(context: Context, treeUri: Uri, documentId: String, depth: Int, into: MutableList<Uri>) {
        if (depth > MAX_FOLDER_DEPTH) return
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
        val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_MIME_TYPE)
        val subdirectories = mutableListOf<String>()
        runCatching {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val childId = cursor.getString(0) ?: continue
                    val mimeType = cursor.getString(1).orEmpty()
                    when {
                        mimeType == DocumentsContract.Document.MIME_TYPE_DIR -> subdirectories += childId
                        mimeType.startsWith("image/") -> into += DocumentsContract.buildDocumentUriUsingTree(treeUri, childId)
                    }
                }
            }
        }
        subdirectories.forEach { walkImages(context, treeUri, it, depth + 1, into) }
    }

    private suspend fun addRecords(context: Context, sources: List<PhotoSource>) {
        context.photoSourcesDataStore.edit { prefs ->
            val current = prefs[SOURCES_KEY] ?: emptySet()
            prefs[SOURCES_KEY] = current + sources.map { it.toRecord() }
        }
    }

    private fun PhotoSource.toRecord(): String = "${type.name}$RECORD_SEPARATOR$uri$RECORD_SEPARATOR$displayName"

    private fun String.toSourceOrNull(): PhotoSource? {
        val parts = split(RECORD_SEPARATOR, limit = 3)
        if (parts.size != 3) return null
        val type = runCatching { PhotoSourceType.valueOf(parts[0]) }.getOrNull() ?: return null
        return PhotoSource(Uri.parse(parts[1]), type, parts[2])
    }

    private fun Uri.lastPathSegmentOrSelf(): String = lastPathSegment ?: toString()
}
