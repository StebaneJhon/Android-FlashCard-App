package com.example.flashcard.backend.Model

data class MatchQuizGameItemModel (
    val text: String,
    val match: String,
    var isActive: Boolean = false,
    var isMatched: Boolean = false,
)