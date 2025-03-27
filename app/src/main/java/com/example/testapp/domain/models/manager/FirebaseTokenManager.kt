package com.example.testapp.domain.models.manager

import android.util.Log
import com.example.testapp.di.api.NotificationApiService
import com.example.testapp.domain.models.user.UserToken
import com.example.testapp.utils.DataStoreUtil
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTokenManager @Inject constructor(
    private val notificationService: NotificationApiService,
    private val dataStore: DataStoreUtil
) {
    fun checkAndUpdateToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newToken = Tasks.await(FirebaseMessaging.getInstance().token)
                val userId = dataStore.getUserId().first()

                userId?.let { currentUserId ->
                    val savedToken = dataStore.getToken().first()

                    if (savedToken == null || savedToken != newToken) {
                        savedToken?.let {
                            notificationService.deleteToken(currentUserId, it)
                        }

                        notificationService.saveToken(currentUserId, UserToken(
                            userId = userId, token = newToken, createdAt = Instant.now()
                        ))
                        dataStore.saveToken(newToken)
                        Log.d("FirebaseTokenManager", "Token updated for user $currentUserId")
                    }
                }
            } catch (e: Exception) {
                Log.e("FirebaseTokenManager", "Error updating token", e)
            } finally {
                Log.d("FirebaseTokenManager", "Token: ${dataStore.getToken().first()}")
            }
        }
    }
}