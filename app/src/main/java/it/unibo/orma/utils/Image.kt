package it.unibo.orma.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun saveImageToGallery(ctx: Context, sourceUri: Uri): Boolean =
    withContext(Dispatchers.IO) {
        val resolver = ctx.contentResolver
        val isScopedStorage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "orma_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (isScopedStorage) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/Orma Mater"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val target = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return@withContext false

        try {
            resolver.openOutputStream(target)?.use { output ->
                resolver.openInputStream(sourceUri)?.use { input -> input.copyTo(output) }
                    ?: return@withContext false
            } ?: return@withContext false
        } catch (_: Exception) {
            resolver.delete(target, null, null)
            return@withContext false
        }

        if (isScopedStorage) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(target, values, null, null)
        }
        true
    }

suspend fun copyToInternalStorage(ctx: Context, sourceUri: Uri, fileName: String): Uri? =
    withContext(Dispatchers.IO) {
        try {
            val dir = File(ctx.filesDir, "photos").apply { mkdirs() }
            val file = File(dir, fileName)
            ctx.contentResolver.openInputStream(sourceUri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
            FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
        } catch (_: Exception) {
            null
        }
    }
