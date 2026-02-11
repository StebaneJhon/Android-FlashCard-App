package com.soaharisonstebane.mneme.util

sealed class Async<out T> {

    object Loading: Async<Nothing>()

    data class Error(val errorMessage: Boolean = false): Async<Nothing>()

    data class Success<out T>(val data: T): Async<T>()

}