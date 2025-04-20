package com.example.testapp.presentation.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.room.withTransaction
import com.example.testapp.data.local.AppRoomDatabase
import com.example.testapp.data.local.dao.ChatRestrictionDao
import com.example.testapp.data.local.entity.ChatRestrictionEntity
import com.example.testapp.data.local.entity.ChatRestrictionEntity.Companion.toEntity
import com.example.testapp.data.local.entity.ChatRestrictionEntity.Companion.toModel
import com.example.testapp.data.remote.ChatRestrictionMediator
import com.example.testapp.di.api.ChatApiService
import com.example.testapp.domain.dto.chat.ChatRestrictionUpdateRequest
import com.example.testapp.domain.dto.chat.FilterCriterion
import com.example.testapp.domain.dto.chat.RestrictionExpireType
import com.example.testapp.domain.dto.chat.SortBy
import com.example.testapp.domain.dto.chat.SortDirection
import com.example.testapp.domain.dto.chat.SortField
import com.example.testapp.domain.models.chat.ChatRestriction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
@HiltViewModel
class ChatRestrictionViewModel @Inject constructor(
    private val chatRepository: ChatApiService,
    private val chatRestrictionDao: ChatRestrictionDao,
    private val roomDatabase: AppRoomDatabase
): ViewModel() {

    private val _pagingDataMap = mutableMapOf<PagerKey, Flow<PagingData<ChatRestriction>>>()

    private data class PagerKey(
        val expireType: RestrictionExpireType,
        val fromUser: String?,
        val toUser: String?,
        val sortBy: SortBy
    )

    fun getChatRestrictions(
        chatId: String,
        expire: RestrictionExpireType,
        filters: List<FilterCriterion> = emptyList(),
        sortBy: SortBy = SortBy(SortField.Date, SortDirection.Descending)
    ): Flow<PagingData<ChatRestriction>> {
        val fromUser = filters.filterIsInstance<FilterCriterion.FromUser>().firstOrNull()
        val appliedTo = filters.filterIsInstance<FilterCriterion.AppliedTo>().firstOrNull()

        val pagerKey = PagerKey(
            expireType = expire,
            fromUser = fromUser?.userId,
            toUser = appliedTo?.userId,
            sortBy = sortBy
        )

        return _pagingDataMap.getOrPut(pagerKey) {
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = true,
                    prefetchDistance = 5,
                    initialLoadSize = 20
                ),
                remoteMediator = ChatRestrictionMediator(
                    roomDatabase = roomDatabase,
                    chatRepository = chatRepository,
                    chatRestrictionDao = chatRestrictionDao,
                    chatId = chatId,
                    expireType = expire,
                    fromUserId = fromUser?.userId,
                    appliedToUserId = appliedTo?.userId,
                    sortField = sortBy.field.value,
                    sortDirection = sortBy.direction.value
                ),
                pagingSourceFactory = {
                    when(expire) {
                        RestrictionExpireType.EXPIRED -> chatRestrictionDao.getExpiredRestrictionsPagingSource(
                            chatId, Instant.now(), fromUser?.userId, appliedTo?.userId,
                            sortBy.field.value, sortBy.direction.value
                        )
                        RestrictionExpireType.ACTIVE -> chatRestrictionDao.getActiveRestrictionsPagingSource(
                            chatId, Instant.now(), fromUser?.userId, appliedTo?.userId,
                            sortBy.field.value, sortBy.direction.value
                        )
                    }
                }
            ).flow
                .map { pagingData ->
                    pagingData.map { entity ->
                        entity.toModel()
                    }
                }.cachedIn(viewModelScope)
        }
    }

    fun updateRestriction(
        restrictionId: String,
        newDuration: Duration
    ) {
        viewModelScope.launch {
            try {
                val response = chatRepository.updateRestriction(
                    restrictionId,
                    ChatRestrictionUpdateRequest(newDuration.toString())
                )

                updateLocalRestriction(response.toEntity())
            } catch (e: Exception) {
                Log.e("ChatRestrictionViewModel", e.message ?: "Unknown Error")
            }
        }
    }

    fun updateLocalRestriction(
        updatedRestriction: ChatRestrictionEntity
    ) {
        viewModelScope.launch {
            roomDatabase.withTransaction {
                chatRestrictionDao.deleteRestriction(updatedRestriction.restrictionId)
                chatRestrictionDao.insert(updatedRestriction)
            }
        }
    }
}