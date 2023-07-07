package com.example.flashcard.quiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.flashcard.R

class BaseFlashCardGame : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_flash_card_game)
    }
}