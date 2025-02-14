package com.example.testapp.di.module

import android.content.Context
import com.example.testapp.di.InstantAdapter
import com.example.testapp.presentation.viewmodel.user.AuthInterceptor
import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.Defaults
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideDataStoreUtil(@ApplicationContext context: Context): DataStoreUtil {
        return DataStoreUtil(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(dataStoreUtil: DataStoreUtil): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(dataStoreUtil))
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(InstantAdapter())
            .build()
    }

    @Provides
    @Singleton
    @Named("UserRetrofit")
    fun provideUserRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl("${Defaults.baseUrl}:${Defaults.USER_SERVICE_PORT}/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named("ChatRetrofit")
    fun provideChatRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl("${Defaults.baseUrl}:${Defaults.CHAT_SERVICE_PORT}/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named("MessageRetrofit")
    fun provideMessageRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl("${Defaults.baseUrl}:${Defaults.MESSAGE_SERVICE_PORT}/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named("ReactionRetrofit")
    fun provideReactionRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl("${Defaults.baseUrl}:${Defaults.REACTION_SERVICE_PORT}/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
