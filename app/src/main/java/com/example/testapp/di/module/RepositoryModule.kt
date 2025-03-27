package com.example.testapp.di.module

import com.example.testapp.di.api.ChatApiService
import com.example.testapp.di.api.MessageApiService
import com.example.testapp.di.api.NotificationApiService
import com.example.testapp.di.api.ReactionApiService
import com.example.testapp.di.api.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserApiService(@Named("UserRetrofit") retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideChatApiService(@Named("ChatRetrofit") retrofit: Retrofit): ChatApiService =
        retrofit.create(ChatApiService::class.java)

    @Provides
    @Singleton
    fun provideMessageApiService(@Named("MessageRetrofit") retrofit: Retrofit): MessageApiService =
        retrofit.create(MessageApiService::class.java)

    @Provides
    @Singleton
    fun provideReactionApiService(@Named("ReactionRetrofit") retrofit: Retrofit): ReactionApiService =
        retrofit.create(ReactionApiService::class.java)

    @Provides
    @Singleton
    fun provideNotificationApiService(@Named("NotificationRetrofit") retrofit: Retrofit): NotificationApiService =
        retrofit.create(NotificationApiService::class.java)
}
