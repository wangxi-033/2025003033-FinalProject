package com.example.diary.data.repository

import com.example.diary.data.network.QuoteApiService
import com.example.diary.data.network.QuoteDto

class QuoteRepository(private val apiService: QuoteApiService) {

    suspend fun getRandomQuote(): Result<QuoteDto> {
        return try {
            val response = apiService.getRandomQuote()
            if (response.isNotEmpty()) {
                Result.success(response.first())
            } else {
                Result.failure(Exception("暂无数据"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
