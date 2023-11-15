package com.dicoding.picodiploma.loginwithanimation.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiService
import com.dicoding.picodiploma.loginwithanimation.data.api.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.api.ListStoryResponse
import com.dicoding.picodiploma.loginwithanimation.data.api.LoginResponse
import com.dicoding.picodiploma.loginwithanimation.data.api.RegisterResponse
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.StoryListEntity
import com.dicoding.picodiploma.loginwithanimation.data.local.room.StoryListDao
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.utils.AppExecutors
import com.dicoding.picodiploma.loginwithanimation.data.api.UploadNewStoryResponse
import com.dicoding.picodiploma.loginwithanimation.data.local.room.StoryListRoomDatabase
import com.dicoding.picodiploma.loginwithanimation.data.paging.StoriesPagingSource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
    private val storyListDao: StoryListDao,
    private val appExecutors: AppExecutors,
    private val storyListRoomDatabase: StoryListRoomDatabase,
    val liveData: MutableLiveData<Boolean?>
) {
    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun getToken(): String {
        val token = userPreference.getSession().first().token
        Log.d("token in rep", token)
        return token
    }

    fun registerUser(name: String, email: String, password: String) {

        try {
            val successResponse = apiService.register(
                name,
                email,
                password
            )
            successResponse.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    successResponse: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Log.d("Request",
                            "Nama: $name, email: $email, Password: $password")
                    } else {
                        Log.d("Request", "Tidak sukses")
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d("onFailure: ", "${t.message}")
                }

            })
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
            Log.d("API Response", errorResponse.toString())
        }
        Log.d("Tes", "Jalan")

    }

    suspend fun login(email: String, password: String ) {


        try {
            val successResponse = apiService.login(
                email,
                password
            )

            successResponse.enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            val userToken = responseBody?.loginResult?.token

                            CoroutineScope(Dispatchers.Main).launch {
                                userPreference.login(userToken.toString())
                            }
                            Log.d("User Token", userToken.toString())
                            Log.d("Status", "Login Berhasil")
                            liveData.postValue(true)

                        } else {
                            Log.d("Status", "Login Gagal")
                            liveData.postValue(false)

                        }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.d("onFailure", "${t.message}")
                }

            })
        } catch (e : HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, LoginResponse::class.java)
            Log.d("API Response", errorResponse.toString())
        }
    }

    suspend fun logout() {
        liveData.postValue(null)
        userPreference.logout()
    }

    /*
    suspend fun showStoryList(): LiveData<Result<List<StoryListEntity>>> {
        val result = MediatorLiveData<Result<List<StoryListEntity>>>()
        result.value = Result.Loading
        val userToken = userPreference.getSession().first().token
        Log.d("User tonek", userToken)
        val client = ApiConfig.getApiService(userToken).getStories()
        client.enqueue(object : Callback<ListStoryResponse> {
            override fun onResponse(
                call: Call<ListStoryResponse>,
                response: Response<ListStoryResponse>
            ) {
                if (response.isSuccessful) {
                    val story = response.body()?.listStory
                    val storyList = ArrayList<StoryListEntity>()

                    appExecutors.diskIO.execute {
                        story?.forEach { story ->
                            val stories = StoryListEntity(
                                story?.id.toString(),
                                story?.name.toString(),
                                story?.description.toString(),
                                story?.photoUrl.toString(),
                                story?.createdAt.toString()
                            )
                            storyList.add(stories)
                        }
                        storyListDao.insertStory(storyList)
                    }
                    val localData = storyListDao.getAllStory()
                    result.addSource(localData) { storyData: List<StoryListEntity> ->
                        result.value = Result.Success(storyData)
                    }
                }
            }

            override fun onFailure(call: Call<ListStoryResponse>, t: Throwable) {
                result.value = Result.Error(t.message.toString())
            }

        })
        return result
    }

     */

    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoriesRemoteMediator(storyListRoomDatabase, apiService, appExecutors),
            pagingSourceFactory = {
                //StoriesPagingSource(apiService)
                storyListRoomDatabase.storyListDao().getAllStory()
            }
        ).liveData
    }

    suspend fun uploadStory(multipartBody: MultipartBody.Part, requestBody: RequestBody): LiveData<Result<Boolean>> {
        val result = MediatorLiveData<Result<Boolean>>()
        result.value = Result.Loading
        val token = userPreference.getSession().first().token
        val apiService = ApiConfig.getApiService(token)
        val successResponse = apiService.uploadImage(multipartBody, requestBody)
        successResponse.enqueue(object : Callback<UploadNewStoryResponse> {
            override fun onResponse(
                call: Call<UploadNewStoryResponse>,
                response: Response<UploadNewStoryResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d("Upload", "Upload berhasil")
                    result.value = Result.Success(true)

                } else {
                    Log.d("Upload", "Upload gagal")
                    result.value = Result.Error("Terjadi Kesalahan")
                }
            }

            override fun onFailure(call: Call<UploadNewStoryResponse>, t: Throwable) {
                Log.d("Failure", "${t.message}")
                result.value = Result.Error(t.message.toString())
            }

        })
        return result
    }

    suspend fun storyLocation(): LiveData<StoryPlace> {
        val storyPlace = MediatorLiveData<StoryPlace>()
        val token = userPreference.getSession().first().token
        try {
            val client = ApiConfig.getApiService(token).getStroriesLocation()
            client.enqueue(object : Callback<ListStoryResponse> {
                override fun onResponse(
                    call: Call<ListStoryResponse>,
                    response: Response<ListStoryResponse>
                ) {
                    if (response.isSuccessful) {
                        val story = response.body()?.listStory
                        story?.forEach{ story ->
                            storyPlace.value = StoryPlace(
                                story?.name.toString(),
                                story?.description.toString(),
                                story?.lat.toString().toDouble(),
                                story?.lat.toString().toDouble()
                            )

                        }
                    } else {
                        Log.d("MAPS", "Gagal")
                    }
                }

                override fun onFailure(call: Call<ListStoryResponse>, t: Throwable) {
                    Log.d("FAILURE", t.message.toString())
                }

            })
        } catch (e: Exception) {
            Log.d("FAILURE", e.message.toString())
        }

        return storyPlace

    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService,
            storyListDao: StoryListDao,
            appExecutors: AppExecutors,
            storyListRoomDatabase: StoryListRoomDatabase,
            liveData: MutableLiveData<Boolean?>
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService, storyListDao, appExecutors, storyListRoomDatabase, liveData)
            }.also { instance = it }
    }
}