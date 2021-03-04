package com.dagu.android.data.manager.account

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.dagu.android.data.authentication.AccountPreferencesRepository
import com.dagu.android.data.authentication.AuthenticationRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShakeShackAuthenticationService : Service() {

    @Inject
    lateinit var authenticationRepository: AuthenticationRepository
    @Inject
    lateinit var accountPreferencesRepository: AccountPreferencesRepository

    override fun onBind(intent: Intent): IBinder? {
        val authenticator = ShakeShackAccountAuthenticator(this)
        authenticator.setAuthenticationRepository(authenticationRepository)
        authenticator.setAccountPreferencesRepository(accountPreferencesRepository)
        return authenticator.iBinder
    }
}