package com.ssoaharison.recall.backend.Model


import com.google.gson.annotations.SerializedName

data class QuizQuestions(
    @SerializedName("response_code")
    val responseCode: Int,
    @SerializedName("results")
    val results: List<OpenTriviaQuestion>
)