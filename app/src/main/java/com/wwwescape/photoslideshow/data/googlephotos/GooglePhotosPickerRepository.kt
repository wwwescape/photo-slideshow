package com.wwwescape.photoslideshow.data.googlephotos

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 * Google Photos Picker API (`photospicker.googleapis.com`) — the current Google-sanctioned way
 * to let a user choose specific photos from their Google Photos library without granting broad
 * library-read access. The user completes the actual picking at [PickerSession.pickerUri]
 * (meant to be opened on a phone/computer, not the TV); this device polls the session until
 * [PickerSession.mediaItemsSet] flips to `true`, then lists what was picked.
 *
 * [PickerSession.pickerUri]/[GooglePhotosMediaItem.baseUrl] field parsing here follows Google's
 * public REST reference as of implementation time — if Google has since changed field names,
 * the JSON parsing below is the first place to check against a live response.
 */
object GooglePhotosPickerRepository {

    private val BASE_URL = "https://photospicker.googleapis.com/v1".toHttpUrl()

    /** Picked items' `baseUrl`s are served from Google's user-content CDN; the bearer token is
     * only ever attached to requests for that domain, never to an arbitrary URL from a response. */
    private const val MEDIA_HOST_SUFFIX = ".googleusercontent.com"

    /** Upper bound on a single downloaded photo — requests are already sized to 2048px, so
     * anything far beyond this is not a photo and must not be allowed to fill the disk. */
    private const val MAX_DOWNLOAD_BYTES = 50L * 1024 * 1024
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val httpClient by lazy { OkHttpClient() }

    suspend fun createSession(context: Context): Result<PickerSession> = authorizedRequest(context) { token ->
        val request = Request.Builder()
            .url(BASE_URL.newBuilder().addPathSegment("sessions").build())
            .header("Authorization", "Bearer $token")
            .post("{}".toRequestBody(JSON_MEDIA_TYPE))
            .build()
        httpClient.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("Create session failed (${response.code})")
            JSONObject(text).toPickerSession()
        }
    }

    suspend fun getSession(context: Context, sessionId: String): Result<PickerSession> = authorizedRequest(context) { token ->
        val request = Request.Builder()
            .url(sessionUrl(sessionId))
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        httpClient.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("Get session failed (${response.code})")
            JSONObject(text).toPickerSession()
        }
    }

    suspend fun deleteSession(context: Context, sessionId: String) {
        authorizedRequest(context) { token ->
            val request = Request.Builder()
                .url(sessionUrl(sessionId))
                .header("Authorization", "Bearer $token")
                .delete()
                .build()
            httpClient.newCall(request).execute().close()
        }
    }

    /** Lists every media item selected in [sessionId], following pagination to completion. */
    suspend fun listSelectedMediaItems(context: Context, sessionId: String): Result<List<GooglePhotosMediaItem>> =
        authorizedRequest(context) { token ->
            val items = mutableListOf<GooglePhotosMediaItem>()
            var pageToken: String? = null
            do {
                val url = BASE_URL.newBuilder()
                    .addPathSegment("mediaItems")
                    .addQueryParameter("sessionId", sessionId)
                    .addQueryParameter("pageSize", "100")
                    .apply { if (pageToken != null) addQueryParameter("pageToken", pageToken) }
                    .build()
                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $token")
                    .get()
                    .build()
                val json = httpClient.newCall(request).execute().use { response ->
                    val text = response.body?.string().orEmpty()
                    if (!response.isSuccessful) throw IOException("List media items failed (${response.code})")
                    JSONObject(text)
                }
                val array = json.optJSONArray("mediaItems")
                if (array != null) {
                    for (i in 0 until array.length()) {
                        items += array.getJSONObject(i).toMediaItem()
                    }
                }
                pageToken = json.optString("nextPageToken", "").ifBlank { null }
            } while (pageToken != null)
            items
        }

    /** Downloads [mediaItem]'s image bytes to [destination] — must be called while the item's
     * picking session is still alive (Google only guarantees a picked item's `baseUrl` resolves
     * during that window; see [PhotoSourceType][com.wwwescape.photoslideshow.data.photos.PhotoSourceType]'s
     * doc comment for why this app downloads once up front rather than re-resolving on demand). */
    suspend fun downloadMediaItem(context: Context, mediaItem: GooglePhotosMediaItem, destination: File): Result<Unit> =
        authorizedRequest(context) { token ->
            val sizedUrl = "${mediaItem.baseUrl}=w2048-h2048".toHttpUrlOrNull()
            if (sizedUrl == null || !sizedUrl.isHttps || !sizedUrl.host.endsWith(MEDIA_HOST_SUFFIX)) {
                throw IOException("Unexpected download location for media item ${mediaItem.id}")
            }
            val request = Request.Builder()
                .url(sizedUrl)
                .header("Authorization", "Bearer $token")
                .build()
            httpClient.newCall(request).execute().use { response ->
                val body = response.body
                if (!response.isSuccessful || body == null) {
                    throw IOException("Download failed (${response.code}) for media item ${mediaItem.id}")
                }
                if (body.contentLength() > MAX_DOWNLOAD_BYTES) {
                    throw IOException("Media item ${mediaItem.id} is too large")
                }
                destination.outputStream().use { out ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var total = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            total += read
                            if (total > MAX_DOWNLOAD_BYTES) throw IOException("Media item ${mediaItem.id} is too large")
                            out.write(buffer, 0, read)
                        }
                    }
                }
            }
        }

    private fun sessionUrl(sessionId: String): HttpUrl =
        BASE_URL.newBuilder().addPathSegment("sessions").addPathSegment(sessionId).build()

    /** `pickerUri` is only guaranteed present on the [createSession] response — Google's API
     * omits it from later [getSession] polls, so it must be parsed as optional here and the
     * original value retained by the caller rather than re-read from every poll. */
    private fun JSONObject.toPickerSession(): PickerSession {
        val pollingConfig = optJSONObject("pollingConfig")
        return PickerSession(
            id = getString("id"),
            pickerUri = optString("pickerUri").ifBlank { null },
            pollIntervalSeconds = pollingConfig?.optString("pollInterval")?.toDurationSeconds() ?: 2.0,
            mediaItemsSet = optBoolean("mediaItemsSet", false),
        )
    }

    /** The Picker API nests file details under `mediaFile`; fall back to flat top-level fields
     * in case that shape differs from what's documented. */
    private fun JSONObject.toMediaItem(): GooglePhotosMediaItem {
        val mediaFile = optJSONObject("mediaFile")
        return GooglePhotosMediaItem(
            id = getString("id"),
            baseUrl = mediaFile?.optString("baseUrl") ?: optString("baseUrl"),
            mimeType = (mediaFile?.optString("mimeType") ?: optString("mimeType")).ifBlank { null },
            filename = (mediaFile?.optString("filename") ?: optString("filename")).ifBlank { null },
        )
    }

    /** Parses a `google.protobuf.Duration` JSON string (e.g. "2.5s") into seconds. */
    private fun String.toDurationSeconds(): Double? =
        if (endsWith("s")) dropLast(1).toDoubleOrNull() else null

    private suspend fun <T> authorizedRequest(context: Context, block: (accessToken: String) -> T): Result<T> {
        val token = GooglePhotosAuthRepository.getValidAccessToken(context)
            ?: return Result.failure(IllegalStateException("Not signed in to Google Photos"))
        return withContext(Dispatchers.IO) { runCatching { block(token) } }
    }
}
