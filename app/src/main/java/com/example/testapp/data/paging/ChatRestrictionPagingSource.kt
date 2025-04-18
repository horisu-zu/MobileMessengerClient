package com.example.testapp.data.paging

/*
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.testapp.di.api.ChatApiService
import com.example.testapp.domain.dto.chat.RestrictionExpireType
import com.example.testapp.domain.models.chat.ChatRestriction
import javax.inject.Inject

class ChatRestrictionPagingSource @Inject constructor(
    private val chatRepository: ChatApiService,
    private val chatId: String,
    private val expire: RestrictionExpireType
): PagingSource<Int, ChatRestriction>() {
    override fun getRefreshKey(state: PagingState<Int, ChatRestriction>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChatRestriction> {
        val page = params.key ?: 0

        return try {
            val response = chatRepository.getChatRestrictions(
                chatId = chatId,
                expireType = expire,
                page = page,
                size = params.loadSize
            )

            val hasMoreData = response.size == params.loadSize

            LoadResult.Page(
                data = response,
                prevKey = if (page > 0) page - 1 else null,
                nextKey = if (hasMoreData) page + 1 else null
            )
        } catch (e: Exception) {
            Log.e("ChatRestrictionPagingSource", "Error loading page $page: ${e.message}")
            LoadResult.Error(e)
        }
    }
}
*/
