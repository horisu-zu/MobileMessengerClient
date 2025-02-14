package com.example.testapp.di

import com.example.testapp.BuildConfig
import com.example.testapp.di.api.AIService
import com.example.testapp.domain.repository.GeminiService
import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {
    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.API_KEY
        )
    }

    @Provides
    @Singleton
    fun provideAIService(generativeModel: GenerativeModel): AIService {
        return GeminiService(generativeModel)
    }
}