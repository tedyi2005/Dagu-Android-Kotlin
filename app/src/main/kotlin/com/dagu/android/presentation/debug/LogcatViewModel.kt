package com.dagu.android.presentation.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers

class LogcatViewModel : ViewModel() {
    fun logcatOkHttpOutput() = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        val tagNameToFilter = "OkHttp"
        Runtime.getRuntime().exec("logcat $tagNameToFilter:D *:S")
            .inputStream
            .bufferedReader()
            .useLines { lines ->
                lines.forEach { line -> emit(line) }
            }
    }
}