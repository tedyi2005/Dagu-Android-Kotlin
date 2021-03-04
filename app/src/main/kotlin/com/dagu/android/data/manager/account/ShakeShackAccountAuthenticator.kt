package com.dagu.android.data.manager.account

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.dagu.android.data.authentication.AccountPreferencesRepository
import com.dagu.android.data.authentication.AuthenticationRepository
import com.dagu.android.data.repository.Result
import com.dagu.android.presentation.authentication.ShakeShackAuthenticatorActivity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ShakeShackAccountAuthenticator(val context: Context?) :
    AbstractAccountAuthenticator(context) {

    private lateinit var authenticationRepository: AuthenticationRepository
    private lateinit var accountPreferencesRepository: AccountPreferencesRepository

    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle? {
        return null
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        val accountName = options?.getString(ShakeShackAuthenticatorActivity.ARG_ACCOUNT_NAME)

        val intent = Intent(context, ShakeShackAuthenticatorActivity::class.java)
        intent.putExtra(ShakeShackAuthenticatorActivity.ARG_ACCOUNT_NAME, accountName)
        intent.putExtra(ShakeShackAuthenticatorActivity.ARG_ACCOUNT_TYPE, accountType)
        intent.putExtra(ShakeShackAuthenticatorActivity.ARG_AUTH_TYPE, authTokenType)
        intent.putExtra(ShakeShackAuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT, true)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        val am = AccountManager.get(context)

        var authToken = am.peekAuthToken(account, authTokenType)

        var refreshToken: String?

        // we get the refreshToken from the Account Preferences DataStore
        runBlocking {
            refreshToken = accountPreferencesRepository.refreshToken.first()
        }

        if (authToken.isNullOrEmpty()) {

            if (!refreshToken.isNullOrEmpty()) {

                runBlocking {

                    val job = launch {
                        authenticationRepository.refreshToken(
                            "refresh_token",
                            refreshToken!!
                        )
                            .catch {
                                Log.e(
                                    "AccountAuthenticator",
                                    "AccountAuthenticator error: ${it.message}"
                                )
                            }.collect { result ->
                                when (result) {
                                    is Result.Success -> {
                                        authToken = result.data.accessToken
                                        refreshToken = result.data.refreshToken
                                    }
                                    is Result.Error -> {
                                        Log.e(
                                            "AccountAuthenticator",
                                            "AccountAuthenticator error: ${result.exception.message}"
                                        )
                                    }
                                }
                            }
                    }

                    job.join()
                }

            }
        }

        if (!authToken.isNullOrEmpty()) {
            val result = Bundle()
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account?.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account?.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            result.putString(
                ShakeShackAuthenticatorActivity.ARG_PARAM_USER_REFRESH_TOKEN,
                refreshToken
            )

            return result
        }

        val intent = Intent(context, ShakeShackAuthenticatorActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        intent.putExtra(ShakeShackAuthenticatorActivity.ARG_ACCOUNT_TYPE, account?.type)
        intent.putExtra(ShakeShackAuthenticatorActivity.ARG_AUTH_TYPE, authTokenType)

        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun getAuthTokenLabel(authTokenType: String?): String {
        return if (ShakeShackAccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS == authTokenType) {
            ShakeShackAccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS_LABEL
        } else {
            "$authTokenType (Label)"
        }
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        val result = Bundle()
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
        return result
    }

    fun setAuthenticationRepository(authenticationRepository: AuthenticationRepository) {
        this.authenticationRepository = authenticationRepository
    }

    fun setAccountPreferencesRepository(accountPreferencesRepository: AccountPreferencesRepository) {
        this.accountPreferencesRepository = accountPreferencesRepository
    }

}