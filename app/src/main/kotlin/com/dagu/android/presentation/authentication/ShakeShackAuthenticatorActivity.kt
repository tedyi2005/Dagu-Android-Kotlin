package com.dagu.android.presentation.authentication

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.dagu.android.R
import com.dagu.android.data.manager.account.ShakeShackAccountGeneral
import com.dagu.android.databinding.ActivityLoginBinding
import com.dagu.android.presentation.base.AccountAuthenticatorActivityListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@AndroidEntryPoint
class ShakeShackAuthenticatorActivity : AppCompatActivity(), AccountAuthenticatorActivityListener {

    private var accountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    private var resultBundle: Bundle? = null

    private val authenticationViewModel: AuthenticationViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    private lateinit var loginNavController: NavController

    private lateinit var accountManager: AccountManager

    private var authTokenType: String? = null
    private var accountType: String? = null

    val currentLoginNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.login_nav_host_fragment)?.childFragmentManager?.fragments?.first()

    companion object {
        const val ARG_ACCOUNT_NAME = "ACCOUNT_NAME"
        const val ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE"
        const val ARG_AUTH_TYPE = "AUTH_TYPE"

        const val ARG_IS_ADDING_NEW_ACCOUNT = "is_adding_new_account"
        const val ARG_PARAM_USER_REFRESH_TOKEN = "user_refresh_token"
    }

    @ExperimentalCoroutinesApi
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginNavHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.login_nav_host_fragment) as NavHostFragment
        loginNavController = loginNavHostFragment.findNavController()

        loginNavController.addOnDestinationChangedListener { _, destination, arguments ->
            when (destination.id) {
                R.id.login_fragment -> {
                }
                R.id.create_account_fragment -> {
                }
                R.id.forgot_password_fragment -> {
                }
            }
        }

        initBottomSheetBehavior()

        accountManager = AccountManager.get(this@ShakeShackAuthenticatorActivity)

        // Get as much as we can in onCreate() time, but most of the info we need to actually
        // perform  a login will come via onNewIntent(), sent from the authenticator class.
        getDataFromIntent(intent)
    }

    @ExperimentalCoroutinesApi
    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)

        // Most of the time we'll be ready to login after receiving info from the authenticator
        // via a new intent:
        getDataFromIntent(newIntent)

        // We should be able to login at this point, after getting the new info:
        authenticationViewModel.login(
            authenticationViewModel.email.value!!,
            authenticationViewModel.password.value!!
        )
    }

    private fun getDataFromIntent(newIntent: Intent?) {
        newIntent?.let {
            accountAuthenticatorResponse =
                newIntent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)

            accountAuthenticatorResponse?.onRequestContinued()

            val accountName = newIntent.getStringExtra(ARG_ACCOUNT_NAME)
            authTokenType = newIntent.getStringExtra(ARG_AUTH_TYPE)
            accountType = newIntent.getStringExtra(ARG_ACCOUNT_TYPE)

            if (authTokenType == null) {
                authTokenType = ShakeShackAccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS
            }

            if (accountName != null) {
                /* TODO set email field (maybe we don't need this)
                binding.emailEditText.setText(accountName)
                 */
            }

            intent = newIntent
        }
    }

    private fun initBottomSheetBehavior() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.container)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED,
                    BottomSheetBehavior.STATE_COLLAPSED,
                    BottomSheetBehavior.STATE_DRAGGING,
                    BottomSheetBehavior.STATE_HALF_EXPANDED,
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        sendAccountAuthenticatorResponse()
                        finish()
                        //Cancels animation on finish()
                        overridePendingTransition(0, 0)
                    }
                    else -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        // Expanded by default:
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onBackPressed() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.container)
        // Expanded by default
        when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_COLLAPSED -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            BottomSheetBehavior.STATE_EXPANDED -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    override fun finishLoginAndCreateAccount(intentForFinish: Intent) {
        val accountName = intentForFinish.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        val account =
            Account(accountName, intentForFinish.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE))
        val refreshToken = intentForFinish.getStringExtra(ARG_PARAM_USER_REFRESH_TOKEN)
        if (intent.getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            val authToken = intentForFinish.getStringExtra(AccountManager.KEY_AUTHTOKEN)
            val authTokenType: String = authTokenType!!

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            accountManager.addAccountExplicitly(account, refreshToken, null)
            accountManager.setAuthToken(account, authTokenType, authToken)
        } else {
            // We use the method `setPassword` from the AccountManager to store the refresh token
            // since there's no available method to store this data
            accountManager.setPassword(account, refreshToken)
        }

        intentForFinish.extras?.let {
            setAccountAuthenticatorResult(it)
        }

        sendAccountAuthenticatorResponse()

        closeBottomSheet()
    }

    // AccountAuthenticatorActivity methods

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     *
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    private fun setAccountAuthenticatorResult(result: Bundle) {
        resultBundle = result
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    fun sendAccountAuthenticatorResponse() {
        if (accountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (resultBundle != null) {
                accountAuthenticatorResponse!!.onResult(resultBundle)
            } else {
                accountAuthenticatorResponse!!.onError(
                    AccountManager.ERROR_CODE_CANCELED,
                    "canceled"
                )
            }
            accountAuthenticatorResponse = null
        }
    }

    override fun closeBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.container)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }
}
