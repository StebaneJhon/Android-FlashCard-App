package com.soaharisonstebane.mneme.settings

data class ThemeModel(
    val themeId: String,
    val theme: Int,
    var isSelected: Boolean = false
)
