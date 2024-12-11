package com.ssoaharison.recall.backend

import com.ssoaharison.recall.backend.models.QuizQuestions
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