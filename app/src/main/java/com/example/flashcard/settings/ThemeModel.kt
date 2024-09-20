package com.example.flashcard.settings

data class ThemeModel(
    val themeId: String,
    val theme: Int,
    var isSelected: Boolean = false
)
