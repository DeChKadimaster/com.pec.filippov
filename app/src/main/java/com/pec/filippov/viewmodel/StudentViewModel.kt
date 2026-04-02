package com.pec.filippov.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pec.filippov.api.StudentApi
import com.pec.filippov.data.LoginRequest
import com.pec.filippov.data.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StudentViewModel : ViewModel() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://qr-app-server-g8wx.onrender.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(StudentApi::class.java)

    private val _student = MutableStateFlow<Student?>(null)
    val student: StateFlow<Student?> = _student

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // If code is empty, don't even try
                if (code.isBlank()) {
                    _error.value = "Введите код доступа"
                    return@launch
                }
                
                val response = api.login(LoginRequest(code))
                if (response.isSuccessful) {
                    _student.value = response.body()
                } else {
                    _error.value = "Неверный код доступа"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка сети: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        _student.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
