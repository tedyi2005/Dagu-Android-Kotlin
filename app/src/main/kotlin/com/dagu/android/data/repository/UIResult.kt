package com.dagu.android.data.repository

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class UIResult<out T> {

    data class Success<out T>(val data: T) : UIResult<T>()
    data class Loading(val loading: Boolean) : UIResult<Nothing>()
    data class Error(val error: String) : UIResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<T> -> "Success[data=$data]"
            is Loading -> "Loading[loading=$loading]"
            is Error -> "Error[exception=$error]"
        }
    }
}
