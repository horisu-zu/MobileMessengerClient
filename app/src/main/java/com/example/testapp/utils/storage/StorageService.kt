package com.example.testapp.utils.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

abstract class StorageService (protected val storage: FirebaseStorage) {
    private val formatter = DateTimeFormatter.ISO_INSTANT

    protected open fun generateFileName(fileUri: Uri? = null, context: Context? = null): String {
        val timestamp = Instant.now()
        val uuid = UUID.randomUUID()
        return "${formatter.format(timestamp)}_${uuid}"
    }

    protected suspend fun getBitmapFromUri(uri: Uri, context: Context): Bitmap {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            } ?: throw IOException("Could not open input stream for URI")
        }
    }

    protected suspend fun uploadBitmap(
        path: String,
        fileName: String,
        bitmap: Bitmap,
        quality: Int = 100
    ): String = suspendCoroutine { continuation ->
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        val data = baos.toByteArray()

        val fileRef = storage.reference.child("$path/$fileName")
        fileRef.putBytes(data)
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        continuation.resume(uri.toString())
                    }
                    .addOnFailureListener { e ->
                        Log.e("StorageService", "Failed to get download URL", e)
                        continuation.resumeWithException(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("StorageService", "Failed to upload file", e)
                continuation.resumeWithException(e)
            }
    }

    protected suspend fun uploadFile(
        path: String,
        fileName: String,
        fileUri: Uri
    ): String = suspendCoroutine { continuation ->
        val fileRef = storage.reference.child("$path/$fileName")

        val uploadTask = fileRef.putFile(fileUri)
        uploadTask
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        continuation.resume(uri.toString())
                    }
                    .addOnFailureListener { e ->
                        Log.e("StorageService", "Failed to get download URL", e)
                        continuation.resumeWithException(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("StorageService", "Failed to upload file", e)
                continuation.resumeWithException(e)
            }
    }

    suspend fun getLatestFile(path: String): String? {
        val filesRef = storage.reference.child(path)
        return try {
            val result = filesRef.listAll().await()
            val latestFile = result.items.maxByOrNull { it.name.substringBefore('_') }
            latestFile?.downloadUrl?.await()?.toString()
        } catch (e: Exception) {
            Log.e("StorageService", "Failed to get latest file from $path", e)
            null
        }
    }

    suspend fun deleteOldFiles(path: String, keepCount: Int) {
        val filesRef = storage.reference.child(path)
        try {
            val result = filesRef.listAll().await()
            val sortedFiles = result.items.sortedByDescending { it.name.substringBefore('_') }
            sortedFiles.drop(keepCount).forEach { it.delete().await() }
        } catch (e: Exception) {
            Log.e("StorageService", "Failed to delete old files from $path", e)
        }
    }

    suspend fun getFileMetadata(path: String, fileName: String): StorageMetadata? {
        val fileRef = storage.reference.child("$path/$fileName")
        return try {
            fileRef.metadata.await()
        } catch (e: Exception) {
            Log.e("StorageService", "Failed to get metadata for $path/$fileName", e)
            null
        }
    }
}