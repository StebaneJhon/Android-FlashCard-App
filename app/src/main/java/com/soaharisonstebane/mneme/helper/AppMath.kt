package com.soaharisonstebane.mneme.helper

class AppMath {
    fun normalize(value: Int, max: Int) = ((value.toFloat() / max.toFloat()) * 100).toInt()
}