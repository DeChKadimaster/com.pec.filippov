package com.pec.filippov.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pec.filippov.api.StudentApi
import com.pec.filippov.data.LoginRequest
import com.pec.filippov.data.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

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

    fun uploadAvatar(context: Context, uri: Uri, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val studentCode = _student.value?.hash ?: _student.value?.id ?: run {
                    _error.value = "Студент не авторизован"
                    return@launch
                }

                val imagePart = withContext(Dispatchers.IO) {
                    // 1. Get dimensions first to prevent OOM
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    context.contentResolver.openInputStream(uri)?.use { 
                        BitmapFactory.decodeStream(it, null, options)
                    }

                    // 2. Calculate sample size (target max 1200px)
                    options.inSampleSize = calculateInSampleSize(options, 1200, 1200)
                    options.inJustDecodeBounds = false

                    // 3. Decode scaled bitmap
                    val bitmap = context.contentResolver.openInputStream(uri)?.use { 
                        BitmapFactory.decodeStream(it, null, options)
                    } ?: return@withContext null

                    // 4. Compress to JPEG under 1MB
                    val stream = ByteArrayOutputStream()
                    var quality = 85
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                    
                    while (stream.size() > 1024 * 1024 && quality > 10) {
                        stream.reset()
                        quality -= 10
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                    }

                    val requestFile = stream.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("file", "avatar.jpg", requestFile)
                }

                if (imagePart == null) {
                    _error.value = "Не удалось обработать изображение"
                    return@launch
                }

                val response = api.uploadAvatar(studentCode, imagePart)
                if (response.isSuccessful) {
                    _student.value = response.body()
                    onComplete()
                } else {
                    _error.value = "Ошибка загрузки: ${response.message()}"
                }
            } catch (e: Throwable) {
                _error.value = "Ошибка при загрузке: ${e.localizedMessage ?: "Неизвестная ошибка"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun logout() {
        _student.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
