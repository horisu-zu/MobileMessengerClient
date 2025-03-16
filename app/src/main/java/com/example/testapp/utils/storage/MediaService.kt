package com.example.testapp.utils.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatMediaService @Inject constructor(
    storage: FirebaseStorage
) : StorageService(storage) {

    companion object {
        private const val CHAT_MEDIA_PATH = "chat_media"
        private const val DEFAULT_IMAGE_QUALITY = 85
        private const val DEFAULT_KEPT_FILES = 100
    }

    suspend fun uploadChatImage(
        chatId: String,
        messageId: String,
        imageUri: Uri,
        context: Context,
        quality: Int = DEFAULT_IMAGE_QUALITY
    ): String {
        val bitmap = getBitmapFromUri(imageUri, context)
        val fileName = generateFileName()
        return uploadBitmap("$CHAT_MEDIA_PATH/$chatId/$messageId", fileName, bitmap, quality)
    }

    suspend fun uploadChatFile(
        chatId: String,
        messageId: String,
        fileUri: Uri
    ): String {
        val fileName = generateFileName()
        return uploadFile("$CHAT_MEDIA_PATH/$chatId/$messageId", fileName, fileUri)
    }

    suspend fun uploadChatVoiceMessage(
        chatId: String,
        messageId: String,
        audioUri: Uri,
        context: Context
    ): String {
        val fileName = generateFileName().replace(".jpg", ".mp3")
        return uploadFile("$CHAT_MEDIA_PATH/$chatId/$messageId", fileName, audioUri)
    }

    suspend fun getMessageMedia(chatId: String, messageId: String): List<String> {
        val mediaRef = storage.reference.child("$CHAT_MEDIA_PATH/$chatId/$messageId")
        return try {
            val result = mediaRef.listAll().await()
            result.items.mapNotNull { it.downloadUrl.await().toString() }
        } catch (e: Exception) {
            Log.e("ChatMediaService", "Failed to get media for message $messageId", e)
            emptyList()
        }
    }

    suspend fun deleteMessageMedia(chatId: String, messageId: String) {
        val mediaRef = storage.reference.child("$CHAT_MEDIA_PATH/$chatId/$messageId")
        try {
            val result = mediaRef.listAll().await()
            result.items.forEach { it.delete().await() }
        } catch (e: Exception) {
            Log.e("ChatMediaService", "Failed to delete media for message $messageId", e)
        }
    }

    suspend fun cleanupOldChatMedia(chatId: String, keepCount: Int = DEFAULT_KEPT_FILES) {
        deleteOldFiles("$CHAT_MEDIA_PATH/$chatId", keepCount)
    }

    suspend fun getChatMediaSize(chatId: String): Long {
        val chatRef = storage.reference.child("$CHAT_MEDIA_PATH/$chatId")
        var totalSize = 0L
        try {
            val folders = chatRef.listAll().await()
            for (folder in folders.prefixes) {
                val files = folder.listAll().await()
                for (file in files.items) {
                    val metadata = file.metadata.await()
                    totalSize += metadata.sizeBytes
                }
            }
            return totalSize
        } catch (e: Exception) {
            Log.e("ChatMediaService", "Failed to calculate chat media size", e)
            return -1
        }
    }
}