package com.example.testapp.di.api

import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.Defaults

object ApiServices {
    private var userApiService: UserApiService? = null
    private var chatApiService: ChatApiService? = null
    private var messageApiService: MessageApiService? = null

    fun initialize(dataStoreUtil: DataStoreUtil) {
        userApiService = RetrofitFactory.createApiService(
            "${Defaults.baseUrl}:${Defaults.USER_SERVICE_PORT}/",
            UserApiService::class.java,
            dataStoreUtil
        )

        chatApiService = RetrofitFactory.createApiService(
            "${Defaults.baseUrl}:${Defaults.CHAT_SERVICE_PORT}/",
            ChatApiService::class.java,
            dataStoreUtil
        )

        messageApiService = RetrofitFactory.createApiService(
            "${Defaults.baseUrl}:${Defaults.MESSAGE_SERVICE_PORT}/",
            MessageApiService::class.java,
            dataStoreUtil
        )
    }

    fun userApiService(): UserApiService {
        return userApiService ?: throw IllegalStateException("ApiServices must be initialized before use")
    }

    fun chatApiService(): ChatApiService {
        return chatApiService ?: throw IllegalStateException("ApiServices must be initialized before use")
    }

    fun messageApiService(): MessageApiService {
        return messageApiService ?: throw IllegalStateException("ApiServices must be initialized before use")
    }
}