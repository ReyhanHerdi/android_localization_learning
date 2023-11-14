package com.dicoding.picodiploma.loginwithanimation.di

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.data.local.room.StoryListRoomDatabase
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.utils.AppExecutors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService(user.token)
        val liveData = MutableLiveData<Boolean?>()
        val database = StoryListRoomDatabase.getDatabase(context)
        val dao = database.storyListDao()
        val appExecutors = AppExecutors()
        return UserRepository.getInstance(pref, apiService, dao, appExecutors, liveData)
    }
}