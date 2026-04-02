package com.pec.filippov.data

import com.google.gson.annotations.SerializedName

data class Student(
    val id: String,
    @SerializedName("full_name") val fullName: String,
    val organization: String,
    @SerializedName("issue_date") val issueDate: String,
    val specialty: String,
    val course: String,
    val hash: String? = null,
    @SerializedName("avatar_base64") val avatarBase64: String? = null
)

data class LoginRequest(
    val code: String,
    val mood: String = "neutral"
)
