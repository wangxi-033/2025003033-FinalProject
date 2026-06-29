package com.example.diary.data.network

import com.google.gson.annotations.SerializedName

data class QuoteDto(
    @SerializedName("q") val content: String,
    @SerializedName("a") val author: String,
    @SerializedName("h") val html: String = ""
)
