package com.dagu.android.presentation.base

import android.content.Intent

interface AccountAuthenticatorActivityListener {
    fun finishLoginAndCreateAccount(intentForFinish: Intent)
    fun closeBottomSheet()
}