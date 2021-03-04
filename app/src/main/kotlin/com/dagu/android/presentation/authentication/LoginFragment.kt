package com.dagu.android.presentation.authentication

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.dagu.android.R
import com.dagu.android.data.authentication.model.UserAuthentication
import com.dagu.android.data.manager.account.ShakeShackAccountGeneral
import com.dagu.android.data.repository.UIResult
import com.dagu.android.databinding.FragmentLoginBinding
import com.dagu.android.presentation.base.AccountAuthenticatorActivityListener
import com.dagu.android.util.afterTextChanged
import com.dagu.android.util.hideKeyboard
import com.dagu.android.util.makeLinks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONException
import java.util.*

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by activityViewModels()
    private val authenticationViewModel: AuthenticationViewModel by activityViewModels()

    private var passwordInputVisible = false

    private lateinit var accountAuthenticatorActivityListener: AccountAuthenticatorActivityListener

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var fbCallbackManager: CallbackManager

    companion object {
        const val GOOGLE_SIGN_IN_REQUEST_CODE = 1001
        const val GOOGLE_SIGN_IN_GRANT_TYPE = "implicit"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fbCallbackManager = CallbackManager.Factory.create()

        setFacebookLoginCallback()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AccountAuthenticatorActivityListener) {
            accountAuthenticatorActivityListener = context
        }
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            initObservers()

            setUpTextWatchers()

            setUpClickListeners()

            underlineCreateAccountAndForgotPassword()

            configureGoogleSignIn()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        fbCallbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                // use idToken for google sign in
                if (account != null && !account.idToken.isNullOrEmpty()) {
                    // get idToken
                    val idToken = account.idToken
                    if (!account.isExpired && idToken != null)
                        authenticationViewModel.setLoginData(account.email ?: "", "")
                        authenticationViewModel.googleSignIn(GOOGLE_SIGN_IN_GRANT_TYPE, idToken!!)
                }
            } catch (exception: ApiException) {
                Toast.makeText(
                    requireContext(),
                    "Google sign in error : $exception",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initObservers() {
        viewModel.loginFormState.observe(
            viewLifecycleOwner,
            Observer {
                val loginState = it ?: return@Observer

                binding.emailInputLayout.error =
                    if (loginState.usernameError != null)
                        getString(loginState.usernameError)
                    else
                        null

                binding.passwordTextInputLayout.error =
                    if (loginState.passwordError != null)
                        getString(loginState.passwordError)
                    else
                        null

                // Disable Sign In button unless both username and password are valid:
                binding.signInButton.isEnabled = loginState.isDataValid
            })

        authenticationViewModel.loginResult.observe(viewLifecycleOwner,
            { loginResult ->

                when (loginResult) {
                    is UIResult.Success -> {
                        binding.root.hideKeyboard()

                        val intentForFinish = createIntentForFinishLogin(loginResult.data)

                        accountAuthenticatorActivityListener.finishLoginAndCreateAccount(intentForFinish)
                    }
                    is UIResult.Loading -> {
                        // TODO update with animation in the login FAB
                        /* if (loginResult.loading) {
                            binding.loading.visibility = View.VISIBLE
                        } else {
                            binding.loading.visibility = View.GONE
                        } */
                    }
                    is UIResult.Error -> {
                        binding.generalErrorTextView.text = loginResult.error
                        binding.generalErrorTextView.visibility = View.VISIBLE
                    }
                }
            })

        authenticationViewModel.googleLoginResult.observe(
            viewLifecycleOwner,
            { googleLoginResult ->

                when (googleLoginResult) {
                    is UIResult.Success -> {
                        binding.root.hideKeyboard()
                        // set access token and refresh token
                        val intentForFinish = createIntentForFinishLogin(googleLoginResult.data)

                        accountAuthenticatorActivityListener.finishLoginAndCreateAccount(intentForFinish)
                    }
                    is UIResult.Loading -> {
                    }
                    is UIResult.Error -> {
                        binding.generalErrorTextView.text = googleLoginResult.error
                        binding.generalErrorTextView.visibility = View.VISIBLE
                    }
                }
            })

        authenticationViewModel.facebookLoginResult.observe(
            viewLifecycleOwner,
            { facebookLoginResult ->
                when (facebookLoginResult) {
                    is UIResult.Success -> {
                        binding.root.hideKeyboard()

                        val intentForFinish = createIntentForFinishLogin(facebookLoginResult.data)

                        accountAuthenticatorActivityListener.finishLoginAndCreateAccount(intentForFinish)
                    }
                    is UIResult.Error -> {
                        binding.generalErrorTextView.text = facebookLoginResult.error
                        binding.generalErrorTextView.visibility = View.VISIBLE
                    }
                    else -> {
                    }
                }
            })
    }

    private fun setUpTextWatchers() {
        binding.emailEditText.afterTextChanged {
            viewModel.onLoginInputChanged(
                binding.emailEditText.text.toString(),
                binding.passwordEditText.text.toString()
            )
        }

        binding.passwordEditText.apply {
            afterTextChanged {
                viewModel.onLoginInputChanged(
                    binding.emailEditText.text.toString(),
                    binding.passwordEditText.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        binding.signInButton.callOnClick()
                }
                false
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun setUpClickListeners() {
        binding.apply {
            passwordShowLink.setOnClickListener {
                passwordInputVisible = !passwordInputVisible
                togglePasswordVisibility(
                    passwordEditText,
                    passwordShowLink,
                    passwordInputVisible
                )
            }

            signInButton.setOnClickListener {
                generalErrorTextView.visibility = View.GONE

                authenticationViewModel.login(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }

            facebookSignInButton.setOnClickListener {
                LoginManager.getInstance()
                    .logInWithReadPermissions(this@LoginFragment, listOf("email"))
            }

            googleSignInButton.setOnClickListener {
                val signInIntent: Intent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
            }

            closeButton.setOnClickListener {
                accountAuthenticatorActivityListener.closeBottomSheet()
            }
        }
    }

    private fun createIntentForFinishLogin(result: UserAuthentication): Intent {
        val data = Bundle()
        data.putString(
            AccountManager.KEY_ACCOUNT_NAME,
            authenticationViewModel.email.value
        )
        data.putString(
            AccountManager.KEY_ACCOUNT_TYPE,
            ShakeShackAccountGeneral.ACCOUNT_TYPE // TODO change
        )
        data.putString(AccountManager.KEY_AUTHTOKEN, result.accessToken)

        data.putString(
            ShakeShackAuthenticatorActivity.ARG_PARAM_USER_REFRESH_TOKEN,
            result.refreshToken
        )

        return Intent().putExtras(data)
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

        passwordEditText.setSelection(passwordEditText.length())
    }

    private fun underlineCreateAccountAndForgotPassword() {
        binding.apply {
            createAccountTextView.makeLinks(
                Pair(
                    "Create one now",
                    View.OnClickListener {
                        Toast.makeText(
                            requireContext(),
                            "Show create account",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToCreateAccountFragment())
                    })
            )

            forgotPasswordTextView.makeLinks(
                Pair("forget your password", View.OnClickListener {
                    Toast.makeText(
                        requireContext(),
                        "Show forgot password",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment())
                })
            )
        }
    }

    private fun configureGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)
    }

    private fun setFacebookLoginCallback() {
        LoginManager.getInstance()
            .registerCallback(fbCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    val graphRequest = GraphRequest.newMeRequest(
                        result?.accessToken
                    ) { _, response ->
                        try {
                            Log.i("FBResponse", response.toString())

                            val accountEmail = response?.jsonObject?.getString("email")

                            authenticationViewModel.setLoginData(accountEmail ?: "", "")
                            authenticationViewModel.loginWithFacebook(
                                accessToken = result?.accessToken?.token ?: ""
                            )

                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(
                                requireContext(),
                                "Error occurred while fetching user email from FB: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    val parameters = Bundle()
                    parameters.putString("fields", "email")
                    graphRequest.parameters = parameters
                    graphRequest.executeAsync()
                }

                override fun onCancel() {
                    Toast.makeText(requireContext(), "Facebook login canceled", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onError(error: FacebookException?) {
                    if (error is FacebookAuthorizationException) {
                        if (AccessToken.isCurrentAccessTokenActive()) {
                            LoginManager.getInstance().logOut()
                        }
                    }

                    Toast.makeText(
                        requireContext(),
                        "Facebook login error: ${error?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    error?.printStackTrace()
                }
            })
    }
}
