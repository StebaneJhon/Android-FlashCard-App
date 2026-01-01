package com.ssoaharison.recall.helper

import kotlinx.coroutines.flow.Flow

class AppMath {
    fun normalize(value: Int, max: Int) = ((value.toFloat() / max.toFloat()) * 100).toInt()
}