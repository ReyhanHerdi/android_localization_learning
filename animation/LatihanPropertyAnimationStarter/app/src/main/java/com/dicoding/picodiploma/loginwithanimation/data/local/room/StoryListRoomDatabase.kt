package com.dicoding.picodiploma.loginwithanimation.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.RemoteKeysEntity
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.StoryListEntity

@Database(entities = [StoryListEntity::class, RemoteKeysEntity::class],
    version = 2)
abstract class StoryListRoomDatabase : RoomDatabase() {
    abstract fun storyListDao(): StoryListDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        @Volatile
        private var INSTANCE: StoryListRoomDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): StoryListRoomDatabase {
            if (INSTANCE == null) {
                synchronized(StoryListRoomDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                    StoryListRoomDatabase::class.java, "Story List Database")
                        .fallbackToDestructiveMigration()
                        .build()
                        .also { INSTANCE = it }
                }
            }

            return INSTANCE as StoryListRoomDatabase
        }
    }

}