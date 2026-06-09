package com.example.queues.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.queues.api.ApiFactory
import com.example.queues.auth.TokenManager
import com.example.queues.dto.AuthDto
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    val isLoading = MutableLiveData(false)
    val message = MutableLiveData<String>()
    val authSuccess = MutableLiveData(false)

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            message.value = "Введите логин и пароль"
            return
        }

        viewModelScope.launch {
            isLoading.value = true

            try {
                val response = ApiFactory.usersApi.login(AuthDto(username, password))
                Log.d("LOGIN", "code = ${response.code()}")
                Log.d("LOGIN", "isSuccessful = ${response.isSuccessful}")
                Log.d("LOGIN", "body = ${response.body()}")
                Log.d("LOGIN", "error = ${response.errorBody()?.string()}")
                val token = response.body()?.token

                if (response.isSuccessful && !token.isNullOrBlank()) {
                    TokenManager.saveToken(token)
                    authSuccess.value = true
                } else {
                    message.value = "Неверный логин или пароль"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                message.value = "Ошибка входа"
            }

            isLoading.value = false
        }
    }

    fun register(username: String, password: String, repeatPassword: String) {
        if (username.isBlank() || password.isBlank() || repeatPassword.isBlank()) {
            message.value = "Заполните все поля"
            return
        }

        if (password != repeatPassword) {
            message.value = "Пароли не совпадают"
            return
        }

        viewModelScope.launch {
            isLoading.value = true

            try {
                val response = ApiFactory.usersApi.register(AuthDto(username, password))
                Log.d("REGISTER", "code = ${response.code()}")
                Log.d("REGISTER", "isSuccessful = ${response.isSuccessful}")
                Log.d("REGISTER", "body = ${response.body()}")
                Log.d("REGISTER", "error = ${response.errorBody()?.string()}")
                val token = response.body()?.token

                if (response.isSuccessful && !token.isNullOrBlank()) {
                    TokenManager.saveToken(token)
                    authSuccess.value = true
                } else {
                    message.value = "Не удалось зарегистрироваться"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                message.value = "Ошибка регистрации"
            }

            isLoading.value = false
        }
    }
}
