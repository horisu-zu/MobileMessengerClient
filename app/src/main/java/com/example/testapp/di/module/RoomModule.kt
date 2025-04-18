package com.example.testapp.di.module

import android.content.Context
import androidx.room.Room
import com.example.testapp.data.local.AppRoomDatabase
import com.example.testapp.data.local.dao.ChatRestrictionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppRoomDatabase {
        return Room.databaseBuilder(
            context,
            AppRoomDatabase::class.java,
            "app_room_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideChatRestrictionDao(database: AppRoomDatabase): ChatRestrictionDao {
        return database.chatRestrictionDao()
    }
}