package com.pec.filippov.api

import com.pec.filippov.data.LoginRequest
import com.pec.filippov.data.Student
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface StudentApi {
    @POST("api/qr/login")
    suspend fun login(@Body request: LoginRequest): Response<Student>

    @GET("api/qr/student/{code}")
    suspend fun getStudent(@Path("code") code: String): Response<Student>

    @GET("api/qr/db-check")
    suspend fun checkDb(): Response<Map<String, Any>>
}
