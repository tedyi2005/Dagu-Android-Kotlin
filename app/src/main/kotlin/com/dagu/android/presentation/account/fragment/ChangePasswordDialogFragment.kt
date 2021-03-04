package com.dagu.android.presentation.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.dagu.android.R
import com.dagu.android.data.repository.UIResult
import com.dagu.android.databinding.FragmentChangePasswordBinding
import com.dagu.android.presentation.account.viewmodel.AccountOverviewViewModel
import com.dagu.android.presentation.authentication.AuthenticationViewModel
import com.dagu.android.presentation.authentication.CreateAccountViewModel
import com.dagu.android.presentation.base.BaseBottomSheetDialogFragment
import com.dagu.android.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ChangePasswordDialogFragment : BaseBottomSheetDialogFragment() {
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val accountOverviewViewModel: AccountOverviewViewModel by activityViewModels()
    private val authenticationViewModel: AuthenticationViewModel by activityViewModels()
    private val createAccountViewModel: CreateAccountViewModel by activityViewModels()

    private var currentPasswordInputVisible = false
    private var newPasswordInputVisible = false
    private var newPasswordConfirmationInputVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        resetChangePasswordModelView()
        setUpViewModels()
        initObservers(binding.passwordView, createAccountViewModel)
    }

    private fun setUpViews() {

        binding.apply {
            backButton.setOnClickListener {
                navigationListener?.onBackPressed()
            }
            closeButton.setOnClickListener {
                navigationListener?.onBackPressed()
            }

            currentPasswordEditText.apply {
                afterTextChanged {
                    createAccountViewModel.setCurrentPassword(it)
                    createAccountViewModel.validateChangePasswordForm()
                }
            }

            currentPasswordShowLink.setOnClickListener {
                currentPasswordInputVisible = !currentPasswordInputVisible
                togglePasswordVisibility(
                    requireActivity(), currentPasswordEditText,
                    currentPasswordShowLink, currentPasswordInputVisible
                )
            }

            passwordView.apply {
                passwordEditText.apply {
                    afterTextChanged {
                        createAccountViewModel.setPassword(it)
                        createAccountViewModel.validateChangePasswordForm()
                    }
                }
                passwordShowLink.setOnClickListener {
                    newPasswordInputVisible = !newPasswordInputVisible
                    togglePasswordVisibility(
                        requireActivity(), passwordEditText,
                        passwordShowLink, newPasswordInputVisible
                    )
                }

                confirmPasswordEditText.apply {
                    afterTextChanged {
                        createAccountViewModel.setConfirmPassword(it)
                        createAccountViewModel.validateChangePasswordForm()
                    }
                }
                confirmPasswordShowLink.setOnClickListener {
                    newPasswordConfirmationInputVisible = !newPasswordConfirmationInputVisible
                    togglePasswordVisibility(
                        requireActivity(),
                        confirmPasswordEditText,
                        confirmPasswordShowLink,
                        newPasswordConfirmationInputVisible
                    )
                }

                saveChangePassword.setOnClickListener {
                    accountOverviewViewModel.userProfile.observe(
                        viewLifecycleOwner, { userProfile ->
                            val currentPassword = binding.currentPasswordEditText.text
                            // validate user
                            if (!currentPassword.isNullOrEmpty() && !userProfile.email.isNullOrEmpty())
                                authenticationViewModel.login(
                                    userProfile.email,
                                    currentPassword.toString()
                                )
                        })
                }
            }
        }
    }

    private fun resetChangePasswordModelView() {
        accountOverviewViewModel.updateNameLiveData.value = UIResult.Success(false)
        authenticationViewModel.currentPasswordLiveData.postValue(UIResult.Success(false))
        createAccountViewModel.setConfirmPassword("")
        createAccountViewModel.setCurrentPassword("")
        createAccountViewModel.setPassword("")
        createAccountViewModel.validateChangePasswordForm()
        binding.apply {
            passwordView.passwordEditText.text?.clear()
            passwordView.confirmPasswordEditText.text?.clear()
            currentPasswordEditText.text?.clear()
        }
    }

    private fun setUpViewModels() {

        createAccountViewModel.isValidChangPasswordForm.observe(viewLifecycleOwner, { isValid ->
            binding.saveChangePassword.isEnabled = isValid
        })

        authenticationViewModel.currentPasswordLiveData.observe(
            this@ChangePasswordDialogFragment, { result ->
                // if User Verified
                val confirmPassword = binding.passwordView.confirmPasswordEditText.text
                if (result is UIResult.Success && result.data && !confirmPassword.isNullOrEmpty()) {
                    accountOverviewViewModel.changePassword(confirmPassword.toString())
                }
            })

        authenticationViewModel.loginResult.observe(
            this@ChangePasswordDialogFragment, { result ->
                // current password is invalid
                if (result is UIResult.Error)
                    showShortToast(
                        requireActivity(), resources.getString(R.string.invalid_current_password)
                    )
            })
        accountOverviewViewModel.updateNameLiveData.observe(viewLifecycleOwner, { result ->
            if (result is UIResult.Success && result.data) {
                showLongBubbledToast(
                    requireActivity(),
                    resources.getString(R.string.password_update_success_message)
                )
                resetChangePasswordModelView()
                dismiss()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
