package com.example.testapp.presentation.viewmodel.user

import android.util.Log
import com.example.testapp.utils.DataStoreUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val dataStoreUtil: DataStoreUtil) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val accessToken = runBlocking { dataStoreUtil.getAccessToken().first() }

        if (originalRequest.url.encodedPath.startsWith("/api/users/auth/")) {
            return chain.proceed(originalRequest)
        }

        val newRequest = if (accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        Log.d("AuthInterceptor", "New Request URL: ${newRequest.url}")
        Log.d("AuthInterceptor", "New Request Headers: ${newRequest.headers}")

        val response = chain.proceed(newRequest)

        return response
    }
}
