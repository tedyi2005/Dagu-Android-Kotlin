package com.dagu.android.presentation.base

/**
 * Interface used by Fragments to set the AuthenticationCallbackListener (e.g., from MainActivity)
 */
interface FragmentAuthenticationInterface {
    var authenticationListener: AuthenticationCallbackListener?

    fun setAuthenticatorCallbackListener(listener: AuthenticationCallbackListener)
}