package com.example.testapp.utils.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import javax.inject.Inject

class AvatarService @Inject constructor(
    storage: FirebaseStorage
) : StorageService(storage) {

    companion object {
        private const val AVATAR_PATH = "avatars"
        private const val AVATAR_SIZE = 200
        private const val AVATAR_QUALITY = 100
        private const val DEFAULT_KEPT_AVATARS = 3
    }

    suspend fun createUserAvatar(userId: String, firstName: String, lastName: String, userColor: String): String {
        val bitmap = createAvatarBitmap(firstName, lastName, userColor)
        val fileName = generateFileName()
        return uploadBitmap("$AVATAR_PATH/$userId", fileName, bitmap, AVATAR_QUALITY)
    }

    suspend fun updateUserAvatarFromUri(userId: String, uri: Uri, context: Context): String {
        val bitmap = getBitmapFromUri(uri, context)
        val fileName = generateFileName()
        return uploadBitmap("$AVATAR_PATH/$userId", fileName, bitmap, AVATAR_QUALITY)
    }

    private fun createAvatarBitmap(firstName: String, lastName: String, userColor: String): Bitmap {
        val bitmap = Bitmap.createBitmap(AVATAR_SIZE, AVATAR_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val backgroundPaint = Paint().apply {
            color = Color.parseColor(userColor)
        }
        canvas.drawRect(0f, 0f, AVATAR_SIZE.toFloat(), AVATAR_SIZE.toFloat(), backgroundPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = AVATAR_SIZE / 3f
            textAlign = Paint.Align.CENTER
        }
        val initials = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
        val x = AVATAR_SIZE / 2f
        val y = AVATAR_SIZE / 2f - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(initials, x, y, textPaint)

        return bitmap
    }

    suspend fun getLatestUserAvatar(userId: String): String? {
        return getLatestFile("$AVATAR_PATH/$userId")
    }

    suspend fun deleteOldAvatars(userId: String, keepCount: Int = DEFAULT_KEPT_AVATARS) {
        deleteOldFiles("$AVATAR_PATH/$userId", keepCount)
    }

    suspend fun getAvatarMetadata(userId: String, fileName: String): StorageMetadata? {
        return getFileMetadata("$AVATAR_PATH/$userId", fileName)
    }
}