package com.example.testapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AvatarService @Inject constructor(
    private val storage: FirebaseStorage
) {
    private val formatter = DateTimeFormatter.ISO_INSTANT

    suspend fun createUserAvatar(userId: String, firstName: String, lastName: String, userColor: String): String {
        val bitmap = createAvatarBitmap(firstName, lastName, userColor)
        val fileName = generateFileName()
        return uploadAvatar(userId, fileName, bitmap)
    }

    private fun generateFileName(): String {
        val timestamp = Instant.now()
        val uuid = UUID.randomUUID()
        return "${formatter.format(timestamp)}_${uuid}.jpg"
    }

    suspend fun updateUserAvatarFromUri(userId: String, uri: Uri, context: Context): String {
        val bitmap = getBitmapFromUri(uri, context)
        val fileName = generateFileName()
        return uploadAvatar(userId, fileName, bitmap)
    }

    private suspend fun getBitmapFromUri(uri: Uri, context: Context): Bitmap {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            } ?: throw IOException("Could not open input stream for URI")
        }
    }

    private fun createAvatarBitmap(firstName: String, lastName: String, userColor: String): Bitmap {
        val size = 200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val backgroundPaint = Paint().apply {
            color = Color.parseColor(userColor)
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), backgroundPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = size / 3f
            textAlign = Paint.Align.CENTER
        }
        val initials = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
        val x = size / 2f
        val y = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(initials, x, y, textPaint)

        return bitmap
    }

    private suspend fun uploadAvatar(userId: String, fileName: String, bitmap: Bitmap): String = suspendCoroutine { continuation ->
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val avatarRef = storage.reference.child("avatars/$userId/$fileName")
        avatarRef.putBytes(data)
            .addOnSuccessListener { taskSnapshot ->
                avatarRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        continuation.resume(uri.toString())
                    }
                    .addOnFailureListener { e ->
                        Log.e("AvatarUpload", "Failed to get download URL", e)
                        continuation.resumeWithException(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AvatarUpload", "Failed to upload avatar", e)
                continuation.resumeWithException(e)
            }
    }

    suspend fun getLatestUserAvatar(userId: String): String? {
        val userAvatarsRef = storage.reference.child("avatars/$userId")
        return try {
            val result = userAvatarsRef.listAll().await()
            val latestAvatar = result.items.maxByOrNull { it.name.substringBefore('_') }
            latestAvatar?.downloadUrl?.await()?.toString()
        } catch (e: Exception) {
            Log.e("AvatarService", "Failed to get latest user avatar", e)
            null
        }
    }

    suspend fun deleteOldAvatars(userId: String, keepCount: Int = 3) {
        val userAvatarsRef = storage.reference.child("avatars/$userId")
        try {
            val result = userAvatarsRef.listAll().await()
            val sortedAvatars = result.items.sortedByDescending { it.name.substringBefore('_') }
            sortedAvatars.drop(keepCount).forEach { it.delete().await() }
        } catch (e: Exception) {
            Log.e("AvatarService", "Failed to delete old avatars", e)
        }
    }

    suspend fun getAvatarMetadata(userId: String, fileName: String): StorageMetadata? {
        val avatarRef = storage.reference.child("avatars/$userId/$fileName")
        return try {
            avatarRef.metadata.await()
        } catch (e: Exception) {
            Log.e("AvatarService", "Failed to get avatar metadata", e)
            null
        }
    }
}