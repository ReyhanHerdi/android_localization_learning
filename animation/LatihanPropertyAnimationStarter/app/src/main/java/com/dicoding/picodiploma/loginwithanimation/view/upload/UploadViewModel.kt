package com.dicoding.picodiploma.loginwithanimation.view.upload

import androidx.lifecycle.ViewModel
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UploadViewModel(private val repository: UserRepository) : ViewModel() {
    suspend fun uploadStory(multipartBody: MultipartBody.Part, requestBody: RequestBody) = repository.uploadStory(multipartBody, requestBody)
}