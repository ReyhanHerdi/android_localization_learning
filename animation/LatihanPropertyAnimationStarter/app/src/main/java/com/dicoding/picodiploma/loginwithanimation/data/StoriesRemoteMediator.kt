package com.dicoding.picodiploma.loginwithanimation.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiService
import com.dicoding.picodiploma.loginwithanimation.data.api.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.RemoteKeysEntity
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.StoryListEntity
import com.dicoding.picodiploma.loginwithanimation.data.local.room.StoryListRoomDatabase
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.utils.AppExecutors
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalPagingApi::class)
class StoriesRemoteMediator(
    private val database: StoryListRoomDatabase,
    private val preference: UserPreference,
    private val appExecutors: AppExecutors
) : RemoteMediator<Int, ListStoryItem>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosesToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKet
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val token = preference.getSession().first().token
            val responseData = ApiConfig.getApiService(token).getStories(page, state.config.pageSize)
            // apiService.getStories(page, state.config.pageSize)
            val storyList = responseData.listStory as List<ListStoryItem>
            val storyArray = ArrayList<StoryListEntity>()

            val endOfPaginationReached = storyList.isEmpty()
            appExecutors.diskIO.execute {
                storyList.forEach { story ->
                    val stories = StoryListEntity(
                        story.id.toString(),
                        story.name.toString(),
                        story.description.toString(),
                        story.photoUrl.toString(),
                        story.createdAt.toString()
                    )
                    storyArray.add(stories)
                }
            }
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().deleteRemoteKeys()
                    database.storyListDao().deleteAll()
                }
                val prevKey = if (page == 1) null else page -1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = storyList.map {
                    RemoteKeysEntity(id = it.id.toString(), nextKey = nextKey, prevKet = prevKey)
                }
                database.remoteKeysDao().insertAll(keys)
                database.storyListDao().insertStory(storyArray)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ListStoryItem>): RemoteKeysEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id.toString())
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ListStoryItem>): RemoteKeysEntity? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id.toString())
        }
    }

    private suspend fun getRemoteKeyClosesToCurrentPosition(state: PagingState<Int, ListStoryItem>): RemoteKeysEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }

}