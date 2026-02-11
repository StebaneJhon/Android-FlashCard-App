package com.soaharisonstebane.mneme.backend.models

data class MatchQuizGameItemModel (
    val text: String,
    val match: String,
    var isActive: Boolean = false,
    var isMatched: Boolean = false,
)