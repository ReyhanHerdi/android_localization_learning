package com.dicoding.picodiploma.loginwithanimation.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.StoryListEntity

@Dao
interface StoryListDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertStory(story: List<StoryListEntity>)

    @Update
    fun updateStory(story: List<StoryListEntity>)

    @Delete
    fun deleteStory(story: List<StoryListEntity>)

    @Query("SELECT * FROM story ORDER BY createdAt DESC")
    fun getAllStory(): LiveData<List<StoryListEntity>>
}