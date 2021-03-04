package com.dagu.android.presentation.authentication

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialContainerTransform
import com.dagu.android.BuildConfig
import com.dagu.android.R
import com.dagu.android.data.manager.account.ShakeShackAccountGeneral
import com.dagu.android.data.repository.UIResult
import com.dagu.android.databinding.FragmentCreateAccountBinding
import com.dagu.android.presentation.base.AccountAuthenticatorActivityListener
import com.dagu.android.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CreateAccountFragment : Fragment() {
    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateAccountViewModel by activityViewModels()
    private val authenticationViewModel: AuthenticationViewModel by activityViewModels()

    private var passwordInputVisible = false
    private var passwordConfirmationInputVisible = false

    private lateinit var accountAuthenticatorActivityListener: AccountAuthenticatorActivityListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AccountAuthenticatorActivityListener) {
            accountAuthenticatorActivityListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            emailEditText.apply {
                afterTextChanged {
                    viewModel.setEmail(it)
                    viewModel.validateForm()
                }
            }

            firstNameEditText.apply {
                afterTextChanged {
                    viewModel.setFirstName(it)
                    viewModel.validateForm()
                }
            }

            lastNameEditText.apply {
                afterTextChanged {
                    viewModel.setLastName(it)
                    viewModel.validateForm()
                }
            }

            phoneNumberEditText.apply {
                addTextChangedListener(PhoneNumberFormattingTextWatcher())
                afterTextChanged {
                    viewModel.setPhoneNumber(it)
                    viewModel.validateForm()
                }
            }

            passwordView.passwordEditText.apply {
                afterTextChanged {
                    viewModel.setPassword(it)
                    viewModel.validateForm()
                }
            }

            passwordView.confirmPasswordEditText.apply {
                afterTextChanged {
                    viewModel.setConfirmPassword(it)
                    viewModel.validateForm()
                }
            }

            passwordView.passwordShowLink.setOnClickListener {
                passwordInputVisible = !passwordInputVisible
                togglePasswordVisibility(
                    passwordView.passwordEditText,
                    passwordView.passwordShowLink,
                    passwordInputVisible
                )
            }

            passwordView.confirmPasswordShowLink.setOnClickListener {
                passwordConfirmationInputVisible = !passwordConfirmationInputVisible
                togglePasswordVisibility(
                    passwordView.confirmPasswordEditText,
                    passwordView.confirmPasswordShowLink,
                    passwordConfirmationInputVisible
                )
            }

            // Set up the wave animation for loading state
            sineView.addWave(0.25f, 0.5f, 0f, 0, 0f) // Fist wave is for the shape of other waves.
            sineView.addWave(
                0.25f, 5f, 0.5f, ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                ), 6f
            )
            sineView.startAnimation()

            enterEmailNextButton.setOnClickListener { view ->
                view.hideKeyboard()

                resetFormErrors()

                val transform = MaterialContainerTransform().apply {
                    // Manually tell the container transform which Views to transform between.
                    startView = view
                    endView = loadingFabButton

                    // Ensure the container transform only runs on a single target
                    endView?.let { addTarget(it) }

                    duration = 300L

                    // Since View to View transforms often are not transforming into full screens,
                    // remove the transition's scrim.
                    scrimColor = Color.TRANSPARENT
                }

                // Begin the transition by changing properties on the start and end views or
                // removing/adding them from the hierarchy.
                TransitionManager.beginDelayedTransition(
                    relativeLayout,
                    transform
                )
                view.visibility = View.GONE
                loadingFabButton.visibility =
                    View.VISIBLE
                sineView.visibility = View.VISIBLE

                authenticationViewModel.createAccount(
                    emailEditText.text.toString(),
                    firstNameEditText.text.toString(),
                    lastNameEditText.text.toString(),
                    passwordView.passwordEditText.text.toString(),
                )
            }

            closeButton.setOnClickListener {
                accountAuthenticatorActivityListener.closeBottomSheet()
            }

            contactInfoDescription.makeLinks(
                Pair("Sign in", View.OnClickListener { findNavController().popBackStack() }),
                Pair(
                    "forgotten your password",
                    View.OnClickListener {
                        findNavController().navigate(
                            CreateAccountFragmentDirections
                                .actionCreateAccountFragmentToForgotPasswordFragment()
                        )
                    })
            )

        }

        initObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun togglePasswordVisibility(
        passwordEditText: EditText,
        passwordTextView: TextView,
        toggle: Boolean
    ) {
        if (toggle) {
            passwordEditText.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            passwordTextView.text = getString(R.string.hide_password)
            passwordTextView.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_eye_visibility_hidden,
                0
            )
        } else {
            passwordEditText.transformationMethod =
                PasswordTransformationMethod.getInstance()
            passwordTextView.text = getString(R.string.show_password)
            passwordTextView.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_eye_visibility_visible,
                0
            )
        }

        passwordEditText.setSelection(passwordEditText.length());
    }


    private fun resetFormErrors() {
        binding.apply {
            emailTextInputLayout.error = null
        }
    }

    private fun initObservers() {
        binding.passwordView.apply {
            viewModel.digitRuleValidation.observe(viewLifecycleOwner, { value ->
                toggleRuleStatus(requireActivity(), rulePasswordNumberText, value)
            })

            viewModel.lowerCaseRuleValidation.observe(viewLifecycleOwner, { value ->
                toggleRuleStatus(requireActivity(), rulePasswordLowercaseText, value)
            })

            viewModel.upperCaseRuleValidation.observe(viewLifecycleOwner, { value ->
                toggleRuleStatus(requireActivity(), rulePasswordUppercaseText, value)
            })

            viewModel.specialCharValidation.observe(viewLifecycleOwner, { value ->
                toggleRuleStatus(requireActivity(), rulePasswordSpecialCharText, value)
            })

            viewModel.lengthValidation.observe(viewLifecycleOwner, { value ->
                toggleRuleStatus(requireActivity(), rulePasswordLengthText, value)
            })

            viewModel.confirmPasswordValidation.observe(viewLifecycleOwner, { value ->
                toggleRuleStatus(requireActivity(), rulePasswordMatchText, value)
            })

            viewModel.isValidPassword.observe(viewLifecycleOwner, { value ->
                binding.passwordView.apply {
                    if (value) {
                        hide(
                            rulePasswordNumberText,
                            rulePasswordLowercaseText,
                            rulePasswordUppercaseText,
                            rulePasswordSpecialCharText,
                            rulePasswordMatchText,
                            rulePasswordLengthText
                        )
                        show(validPasswordText)
                    } else {
                        show(
                            rulePasswordNumberText,
                            rulePasswordLowercaseText,
                            rulePasswordUppercaseText,
                            rulePasswordSpecialCharText,
                            rulePasswordMatchText,
                            rulePasswordLengthText
                        )
                        hide(validPasswordText)
                    }
                }

            })
        }
        viewModel.isValidForm.observe(viewLifecycleOwner, { value ->
            binding.apply {
                enterEmailNextButton.isEnabled = value
            }
        })

        authenticationViewModel.createUserResult.observe(viewLifecycleOwner, { createUserResult ->
            when (createUserResult) {
                is UIResult.Success -> {
                    if (createUserResult.data.oloProviderToken != null
                        && createUserResult.data.user != null
                        && createUserResult.data.user.email != null
                    ) {
                        authenticationViewModel.getOloAuthToken(
                            createUserResult.data.oloProviderToken,
                            BuildConfig.OLO_KEY,
                            createUserResult.data.user.email
                        )
                    }
                }
                is UIResult.Loading -> {
                    // TODO add code if needed
                }
                is UIResult.Error -> {
                    binding.apply {
                        emailTextInputLayout.error = createUserResult.error

                        loadingFabButton.visibility = View.GONE
                        sineView.visibility = View.GONE
                        enterEmailNextButton.visibility = View.VISIBLE
                    }
                }
            }
        })

        authenticationViewModel.oloUserData.observe(
            viewLifecycleOwner,
            { result ->
                when (result) {
                    is UIResult.Success -> {
                        result.data.authToken?.let {
                            authenticationViewModel.updateOloUserData(
                                it,
                                BuildConfig.OLO_KEY,
                                binding.phoneNumberEditText.text.toString()
                            )
                        }
                    }
                    is UIResult.Loading -> {
                        // TODO add code if needed
                    }
                    is UIResult.Error -> {
                        binding.apply {
                            emailTextInputLayout.error = result.error

                            loadingFabButton.visibility = View.GONE
                            sineView.visibility = View.GONE
                            enterEmailNextButton.visibility = View.VISIBLE
                        }
                    }
                }
            })

        authenticationViewModel.oloUserContactData.observe(
            viewLifecycleOwner,
            { result ->
                when (result) {
                    is UIResult.Success -> {
                        Log.d("MainActivity", "Created olo user contact data")
                        authenticationViewModel.login(
                            binding.emailEditText.text.toString(),
                            binding.passwordView.passwordEditText.text.toString()
                        )
                    }
                    is UIResult.Loading -> {
                        // TODO add code if needed
                    }
                    is UIResult.Error -> {
                        binding.apply {
                            emailTextInputLayout.error = result.error

                            loadingFabButton.visibility = View.GONE
                            sineView.visibility = View.GONE
                            enterEmailNextButton.visibility = View.VISIBLE
                        }
                    }
                }
            })

        authenticationViewModel.loginResult.observe(
            viewLifecycleOwner,
            { loginResult ->

                when (loginResult) {
                    is UIResult.Success -> {
                        binding.root.hideKeyboard()

                        val data = Bundle()
                        data.putString(
                            AccountManager.KEY_ACCOUNT_NAME,
                            authenticationViewModel.email.value
                        )
                        data.putString(
                            AccountManager.KEY_ACCOUNT_TYPE,
                            ShakeShackAccountGeneral.ACCOUNT_TYPE
                        )
                        data.putString(AccountManager.KEY_AUTHTOKEN, loginResult.data.accessToken)

                        data.putString(
                            ShakeShackAuthenticatorActivity.ARG_PARAM_USER_REFRESH_TOKEN,
                            loginResult.data.refreshToken
                        )

                        val intentForFinish = Intent().putExtras(data)

                        accountAuthenticatorActivityListener.finishLoginAndCreateAccount(intentForFinish)
                    }
                    is UIResult.Loading -> {
                        // TODO add code if needed
                    }
                    is UIResult.Error -> Toast.makeText(
                        requireContext(),
                        loginResult.error,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

}