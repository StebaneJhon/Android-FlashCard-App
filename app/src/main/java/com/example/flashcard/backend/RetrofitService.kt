package com.example.flashcard.backend

import com.example.flashcard.backend.Model.QuizQuestions
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {

    @GET("api.php")
    suspend fun getQuizQuestionMultiple(
        @Query("amount") amount: String,
        @Query("category") category: String,
        @Query("difficulty") difficulty: String,
        @Query("type") type: String,
    ): Response<QuizQuestions>

}