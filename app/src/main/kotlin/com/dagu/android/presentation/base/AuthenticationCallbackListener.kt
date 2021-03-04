package com.dagu.android.presentation.base

import android.accounts.Account

/**
 * Interface for communication between Fragments and Activities using AccountManager methods
 */
interface AuthenticationCallbackListener {
    fun getTokenForAccount(account: Account, authTokenType: String)

    fun getTokenForAccountCreateIfNeeded(
        accountName: String? = null,
        accountType: String,
        authTokenType: String
    )

    fun removeAccount(account: Account)
}