package com.dagu.android.presentation.base

import android.content.Context
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.dagu.android.databinding.ChangePasswordViewBinding
import com.dagu.android.presentation.authentication.CreateAccountViewModel
import com.dagu.android.util.hide
import com.dagu.android.util.show
import com.dagu.android.util.toggleRuleStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment(), FragmentNavigationInterface,
    FragmentAuthenticationInterface {
    override var authenticationListener: AuthenticationCallbackListener? = null
    override var navigationListener: ShakeShackNavigationListener? = null
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (requireActivity() is AuthenticationCallbackListener) {
            setAuthenticatorCallbackListener(requireActivity() as AuthenticationCallbackListener)
        }


        if (requireActivity() is ShakeShackNavigationListener) {
            setNavigationCallbackListener(requireActivity() as ShakeShackNavigationListener)
        }
    }

    override fun onResume() {
        super.onResume()

        setOnBackPressedCallback {  }
    }

    override fun onDetach() {
        super.onDetach()

        authenticationListener = null
        navigationListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()

        onBackPressedCallback?.isEnabled = false
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
            findNavController().popBackStack()
            lambda()
        }

        onBackPressedCallback?.isEnabled = true
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

    @ExperimentalCoroutinesApi
    fun initObservers(
        passwordView: ChangePasswordViewBinding,
        createAccountViewModel: CreateAccountViewModel
    ) {
        passwordView.apply {
            createAccountViewModel.digitRuleValidation.observe(viewLifecycleOwner, { value ->
                toggleRuleStatus(requireContext(), rulePasswordNumberText, value)
            })

            createAccountViewModel.lowerCaseRuleValidation.observe(viewLifecycleOwner, { value ->
                toggleRuleStatus(requireContext(), rulePasswordLowercaseText, value)
            })

            createAccountViewModel.upperCaseRuleValidation.observe(viewLifecycleOwner, { value ->
                toggleRuleStatus(requireContext(), rulePasswordUppercaseText, value)
            })

            createAccountViewModel.specialCharValidation.observe(viewLifecycleOwner, { value ->
                toggleRuleStatus(requireContext(), rulePasswordSpecialCharText, value)
            })

            createAccountViewModel.lengthValidation.observe(viewLifecycleOwner, { value ->
                toggleRuleStatus(requireContext(), rulePasswordLengthText, value)
            })

            createAccountViewModel.confirmPasswordValidation.observe(viewLifecycleOwner, { value ->
                toggleRuleStatus(requireContext(), rulePasswordMatchText, value)
            })

            createAccountViewModel.isValidPassword.observe(viewLifecycleOwner, { isValid ->
                if (isValid) {
                    hide(
                        rulePasswordNumberText,
                        rulePasswordLowercaseText,
                        rulePasswordUppercaseText,
                        rulePasswordSpecialCharText,
                        rulePasswordLengthText,
                        rulePasswordMatchText
                    )
                    show(validPasswordText)
                } else {
                    show(
                        rulePasswordNumberText,
                        rulePasswordLowercaseText,
                        rulePasswordUppercaseText,
                        rulePasswordSpecialCharText,
                        rulePasswordLengthText,
                        rulePasswordMatchText
                    )
                    hide(validPasswordText)
                }
            })
        }
    }
}