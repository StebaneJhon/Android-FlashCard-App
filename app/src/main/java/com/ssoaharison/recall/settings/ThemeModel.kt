package com.ssoaharison.recall.settings

data class ThemeModel(
    val themeId: String,
    val theme: Int,
    var isSelected: Boolean = false
)
