package com.example.testapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.testapp.data.local.converter.InstantConverter
import com.example.testapp.data.local.dao.ChatRestrictionDao
import com.example.testapp.data.local.entity.ChatRestrictionEntity

@Database(
    entities = [ChatRestrictionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(InstantConverter::class)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun chatRestrictionDao(): ChatRestrictionDao
}