package com.dicoding.picodiploma.loginwithanimation.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiService
import com.dicoding.picodiploma.loginwithanimation.data.api.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import kotlinx.coroutines.flow.first

class StoriesPagingSource(private val preference: UserPreference) : PagingSource<Int, ListStoryItem>() {

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        val token = preference.getSession().first().token
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = ApiConfig.getApiService(token).getStories(position, params.loadSize)
            val storyList = responseData.listStory as List<ListStoryItem>

            LoadResult.Page(
                data = storyList,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (storyList.isEmpty()) null else position + 1
            )
        } catch (e: Exception) {
            Log.d("LOAD ERROR", e.message.toString())
            return LoadResult.Error(e)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}