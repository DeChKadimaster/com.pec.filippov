package com.pec.filippov.data

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    val message: String,
    @SerializedName("matched_count") val matchedCount: Int,
    @SerializedName("modified_count") val modifiedCount: Int,
    @SerializedName("avatar_preview") val avatarPreview: String? = null
)
