package com.ssoaharison.recall.backend

import android.app.Application
import com.ssoaharison.recall.helper.AppThemeHelper

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

    override fun onCreate() {
        super.onCreate()
        AppThemeHelper.applyTheme(this)
    }
}