package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.model.LocalMediaEntry
import com.example.model.MediaCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackgroundAndMediaHelper(context: Context) {

    private val appContext = context.applicationContext
    private var cachedUri: String? = null
    private var cachedLowEnd: Boolean = false
    private var cachedBitmap: ImageBitmap? = null

    fun takePersistableReadPermission(uri: Uri): Boolean {
        return runCatching {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            appContext.contentResolver.takePersistableUriPermission(uri, flags)
            true
        }.getOrDefault(false)
    }

    suspend fun decodeCustomBackground(
        uriString: String,
        lowEndMode: Boolean
    ): Result<ImageBitmap> = withContext(Dispatchers.IO) {
        runCatching {
            if (cachedUri == uriString && cachedLowEnd == lowEndMode && cachedBitmap != null) {
                return@runCatching cachedBitmap!!
            }

            val uri = Uri.parse(uriString)
            val resolver = appContext.contentResolver
            val mimeType = resolver.getType(uri)?.lowercase() ?: ""
            if (mimeType.isNotBlank() &&
                !mimeType.contains("jpeg") &&
                !mimeType.contains("jpg") &&
                !mimeType.contains("png") &&
                !mimeType.contains("webp") &&
                !mimeType.startsWith("image/")
            ) {
                throw IllegalArgumentException("صيغة الملف غير مدعومة. يرجى اختيار صورة بصيغة JPEG أو PNG أو WebP.")
            }

            // Step 1: Read bounds without allocating pixel memory
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            } ?: throw IllegalStateException("تعذر فتح ملف الصورة المختار. تحقق من صلاحية الملف.")

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                throw IllegalArgumentException("ملف الصورة تالف أو غير صالح للعرض.")
            }

            // Conservative decoding target for weak/older TV devices
            val maxWidth = if (lowEndMode) 1280 else 1920
            val maxHeight = if (lowEndMode) 720 else 1080
            val sampleSize = calculateInSampleSize(
                boundsOptions.outWidth,
                boundsOptions.outHeight,
                maxWidth,
                maxHeight
            )

            // Step 2: Decode scaled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = if (lowEndMode) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
            }

            val decoded = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: throw IllegalStateException("فشل فك ترميز الصورة المختارة.")

            val imageBitmap = decoded.asImageBitmap()
            cachedUri = uriString
            cachedLowEnd = lowEndMode
            cachedBitmap = imageBitmap
            imageBitmap
        }
    }

    fun clearBackgroundCache() {
        cachedUri = null
        cachedBitmap = null
    }

    suspend fun resolveDocumentMetadata(
        uri: Uri,
        defaultCategory: MediaCategory
    ): LocalMediaEntry? = withContext(Dispatchers.IO) {
        runCatching {
            takePersistableReadPermission(uri)
            val resolver = appContext.contentResolver
            val mimeType = resolver.getType(uri) ?: "*/*"
            var displayName = uri.lastPathSegment ?: "ملف محلي"
            var sizeBytes = 0L

            resolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                        displayName = cursor.getString(nameIndex)
                    }
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        sizeBytes = cursor.getLong(sizeIndex)
                    }
                }
            }

            val inferredCategory = when {
                defaultCategory != MediaCategory.DOCUMENT_OR_FILE -> defaultCategory
                isSeriesFilename(displayName) -> MediaCategory.SERIES
                mimeType.startsWith("video/") -> MediaCategory.MOVIE
                else -> MediaCategory.DOCUMENT_OR_FILE
            }

            LocalMediaEntry(
                uriString = uri.toString(),
                title = displayName,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
                category = inferredCategory
            )
        }.getOrNull()
    }

    suspend fun queryAccessibleLocalVideos(): List<LocalMediaEntry> = withContext(Dispatchers.IO) {
        runCatching {
            val list = ArrayList<LocalMediaEntry>()
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED
            )
            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
            appContext.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                var count = 0
                while (cursor.moveToNext() && count < 60) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "فيديو محلي"
                    val mime = cursor.getString(mimeCol) ?: "video/*"
                    val size = cursor.getLong(sizeCol)
                    val contentUri = Uri.withAppendedPath(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )
                    val category = if (isSeriesFilename(name)) {
                        MediaCategory.SERIES
                    } else {
                        MediaCategory.MOVIE
                    }
                    list.add(
                        LocalMediaEntry(
                            uriString = contentUri.toString(),
                            title = name,
                            mimeType = mime,
                            sizeBytes = size,
                            category = category
                        )
                    )
                    count++
                }
            }
            list
        }.getOrDefault(emptyList())
    }

    fun openLocalMediaFile(entry: LocalMediaEntry): Result<Unit> {
        return runCatching {
            val uri = Uri.parse(entry.uriString)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, entry.mimeType.ifBlank { "*/*" })
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            appContext.startActivity(intent)
        }
    }

    private fun isSeriesFilename(name: String): Boolean {
        val lower = name.lowercase()
        return Regex("s\\d{1,2}e\\d{1,2}|ep\\s*\\d+|حلقة|مسلسل|season|episode").containsMatchIn(lower)
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }
}
