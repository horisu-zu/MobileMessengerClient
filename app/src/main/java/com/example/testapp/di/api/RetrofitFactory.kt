package com.example.testapp.di.api

import com.example.testapp.di.InstantAdapter
import com.example.testapp.presentation.viewmodel.user.AuthInterceptor
import com.example.testapp.utils.DataStoreUtil
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitFactory {
    private fun createOkHttpClient(dataStoreUtil: DataStoreUtil): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(dataStoreUtil))
            .build()
    }

    private fun createMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(InstantAdapter())
            .build()
    }

    fun <T> createApiService(
        baseUrl: String,
        serviceClass: Class<T>,
        dataStoreUtil: DataStoreUtil
    ): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient(dataStoreUtil))
            .addConverterFactory(MoshiConverterFactory.create(createMoshi()))
            .build()

        return retrofit.create(serviceClass)
    }
}
