package com.dicoding.picodiploma.loginwithanimation.view.maps

import androidx.lifecycle.ViewModel
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository

class MapsViewModel(private val repository: UserRepository) : ViewModel() {
    suspend fun getStoryLocation() = repository.storyLocation()
}