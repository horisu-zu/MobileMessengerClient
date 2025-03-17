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
        private const val DEFAULT_KEPT_FILES = 100
    }

    suspend fun uploadChatFile(
        chatId: String,
        messageId: String,
        fileUri: Uri
    ): String {
        val fileName = generateFileName()
        return uploadFile("$CHAT_MEDIA_PATH/$chatId/$messageId", fileName, fileUri)
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