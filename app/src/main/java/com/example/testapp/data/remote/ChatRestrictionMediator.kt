package com.example.testapp.data.remote

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.testapp.data.local.AppRoomDatabase
import com.example.testapp.data.local.dao.ChatRestrictionDao
import com.example.testapp.data.local.entity.ChatRestrictionEntity
import com.example.testapp.data.local.entity.ChatRestrictionEntity.Companion.toEntity
import com.example.testapp.di.api.ChatApiService
import com.example.testapp.domain.dto.chat.RestrictionExpireType
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject


@OptIn(ExperimentalPagingApi::class)
class ChatRestrictionMediator @Inject constructor(
    private val roomDatabase: AppRoomDatabase,
    private val chatRepository: ChatApiService,
    private val chatRestrictionDao: ChatRestrictionDao,
    private val chatId: String,
    private val expireType: RestrictionExpireType
): RemoteMediator<Int, ChatRestrictionEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ChatRestrictionEntity>
    ): MediatorResult {
        return try {
            val page = when(loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)

                    state.pages.size
                }
            }

            val response = chatRepository.getChatRestrictions(
                chatId = chatId,
                expireType = expireType,
                page = page,
                size = state.config.pageSize
            )

            val endOfPaginationReached = response.isEmpty() || response.size < state.config.pageSize
            val entities = response.map { it.toEntity() }

            roomDatabase.withTransaction {
                chatRestrictionDao.insertAll(entities)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            Log.e("ChatRestrictionMediator", "Error loading data: ${e.message}")
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            Log.e("ChatRestrictionMediator", "Error loading data: ${e.message}")
            MediatorResult.Error(e)
        } catch (e: Exception) {
            Log.e("ChatRestrictionMediator", "Error loading data: ${e.message}")
            MediatorResult.Error(e)
        }
    }
}