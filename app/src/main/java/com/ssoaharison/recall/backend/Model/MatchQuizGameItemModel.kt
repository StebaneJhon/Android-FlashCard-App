package com.ssoaharison.recall.backend.Model

data class MatchQuizGameItemModel (
    val text: String,
    val match: String,
    var isActive: Boolean = false,
    var isMatched: Boolean = false,
)