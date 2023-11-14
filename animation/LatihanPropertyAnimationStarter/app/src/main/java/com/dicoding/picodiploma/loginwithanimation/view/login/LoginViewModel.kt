package com.dicoding.picodiploma.loginwithanimation.view.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    val liveDataStatus: MutableLiveData<Boolean?> = repository.liveData

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    suspend fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            repository.login(email, password)
        }
    }
}