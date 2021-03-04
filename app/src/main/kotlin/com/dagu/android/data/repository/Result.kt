package com.dagu.android.data.repository

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Result<out T> {

    data class Success<out T>(val data: T) : Result<T>()
    data class Loading(val loading: Boolean) : Result<Nothing>()
    data class Error(val exception: Exception) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<T> -> "Success[data=$data]"
            is Loading -> "Loading[loading=$loading]"
            is Error -> "Error[exception=$exception]"
        }
    }
}