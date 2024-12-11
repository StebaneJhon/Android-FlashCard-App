package com.ssoaharison.recall.backend.models

data class MatchQuizGameItemModel (
    val text: String,
    val match: String,
    var isActive: Boolean = false,
    var isMatched: Boolean = false,
)