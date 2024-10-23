package com.ssoaharison.recall.backend

class OpenTriviaRepository constructor(private val retrofitClient: RetrofitClient) {
    suspend fun getOpenTriviaQuestion(
        amount: String,
        category: String,
        difficulty: String,
        type: String
        ) = retrofitClient
            .instance
            .getQuizQuestionMultiple(amount, category, difficulty, type)
}