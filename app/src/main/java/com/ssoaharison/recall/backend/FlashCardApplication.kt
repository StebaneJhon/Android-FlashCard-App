package com.ssoaharison.recall.backend

import android.app.Application

class FlashCardApplication: Application() {
    private val database by lazy {
        FlashCardDatabase.getDatabase(this)
    }
    val repository by lazy {
        FlashCardRepository(database.flashCardDao())
    }
    val openTriviaRepository by lazy {
        OpenTriviaRepository(RetrofitClient)
    }
}