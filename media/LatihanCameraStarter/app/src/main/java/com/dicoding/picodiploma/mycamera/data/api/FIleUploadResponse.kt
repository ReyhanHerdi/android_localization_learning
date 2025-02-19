package com.dicoding.picodiploma.mycamera.data.api

import com.google.gson.annotations.SerializedName

data class FIleUploadResponse(
    @field:SerializedName("error")
    val error: Boolean,
    @field:SerializedName("message")
    val message: String
)
