package com.dagu.android.presentation.base

import android.content.Context
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

abstract class BaseFragment : Fragment(), FragmentAuthenticationInterface,
    FragmentNavigationInterface {
    override var authenticationListener: AuthenticationCallbackListener? = null
    override var navigationListener: ShakeShackNavigationListener? = null
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setOnBackPressedCallback { }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (requireActivity() is AuthenticationCallbackListener) {
            setAuthenticatorCallbackListener(requireActivity() as AuthenticationCallbackListener)
        }

        if (requireActivity() is ShakeShackNavigationListener) {
            setNavigationCallbackListener(requireActivity() as ShakeShackNavigationListener)
        }
    }

    override fun onDetach() {
        super.onDetach()

        authenticationListener = null
        navigationListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()

        toggleOnBackPressedCallback(false)
        onBackPressedCallback?.remove()
    }

    /**
     * Method used to set `OnBackPressedCallback` needed for navigation between fragments to work
     *
     * @param lambda    a custom function passed to the callback to be executed right after
     * `findNavController().popBackStack()`
     * */
    fun setOnBackPressedCallback(lambda: () -> Unit) {
        onBackPressedCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigateUp()
            lambda()
        }

        toggleOnBackPressedCallback(true)
    }

    /**
     * Sets the `AuthenticationCallbackListener` to this fragment
     *
     * @param listener  The `AuthenticationCallbackListener` to be set
     */
    override fun setAuthenticatorCallbackListener(listener: AuthenticationCallbackListener) {
        this.authenticationListener = listener
    }

    /**
     * Sets the `ShakeShackNavigationListener` to this fragment
     *
     * @param listener  The `ShakeShackNavigationListener` to be set
     */
    override fun setNavigationCallbackListener(listener: ShakeShackNavigationListener) {
        this.navigationListener = listener
    }

    /**
     * Sets up the toolbar from the current fragment
     *
     * @param   toolbar The toolbar to be set up
     */
    fun setUpToolbarFromFragment(toolbar: Toolbar) {
        navigationListener?.setUpToolbar(toolbar)
    }

    /**
     * Sets up the back navigation action
     */
    fun onBackPressed() {
        navigationListener?.onBackPressed()
    }

    /**
     * Toggles onBackPressedCallback to enabled/disabled for this fragment
     */
    fun toggleOnBackPressedCallback(enable: Boolean) {
        onBackPressedCallback?.isEnabled = enable
    }

}