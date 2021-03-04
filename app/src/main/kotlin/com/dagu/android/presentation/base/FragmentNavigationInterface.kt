package com.dagu.android.presentation.base

/**
 * Interface used by Fragments to set the ShakeShackNavigationListener (e.g., from MainActivity)
 */
interface FragmentNavigationInterface {
    var navigationListener: ShakeShackNavigationListener?

    fun setNavigationCallbackListener(listener: ShakeShackNavigationListener)
}