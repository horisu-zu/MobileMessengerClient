package com.example.testapp.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.testapp.data.local.entity.ChatRestrictionEntity
import java.time.Instant

@Dao
interface ChatRestrictionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(restrictions: List<ChatRestrictionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(restriction: ChatRestrictionEntity)

    @Update
    suspend fun updateRestriction(restriction: ChatRestrictionEntity)

    @Query("DELETE FROM chat_restriction WHERE restrictionId = :restrictionId")
    suspend fun deleteRestriction(restrictionId: String)

    @Query("SELECT * FROM chat_restriction WHERE chatId = :chatId AND (expiresAt IS NULL OR expiresAt > :currentTime) ORDER BY createdAt DESC")
    fun getActiveRestrictionsPagingSource(chatId: String, currentTime: Instant): PagingSource<Int, ChatRestrictionEntity>

    @Query("SELECT * FROM chat_restriction WHERE chatId = :chatId AND expiresAt IS NOT NULL AND expiresAt <= :currentTime ORDER BY expiresAt DESC")
    fun getExpiredRestrictionsPagingSource(chatId: String, currentTime: Instant): PagingSource<Int, ChatRestrictionEntity>

    @Query("SELECT * FROM chat_restriction WHERE restrictionId = :restrictionId")
    suspend fun getRestrictionById(restrictionId: String): ChatRestrictionEntity?
}