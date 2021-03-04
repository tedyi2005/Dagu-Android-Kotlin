package com.dagu.android.authentication

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asFlow
import app.cash.turbine.test
import com.dagu.android.BaseCoroutineTest
import com.dagu.android.R
import com.dagu.android.presentation.authentication.LoginFormState
import com.dagu.android.presentation.authentication.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class LoginViewModelTest : BaseCoroutineTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalTime
    @Test
    fun `loginFormState pass Data valid when username and test are not empty`() {
        val authenticationViewModel = LoginViewModel()
        coroutineTestRule.dispatcher.runBlockingTest {
            authenticationViewModel.onLoginInputChanged("test", "test")
            authenticationViewModel.loginFormState.asFlow().test {
                val state = LoginFormState(isDataValid = true)
                assertThat(expectItem(), equalTo(state))
                expectNoEvents()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `loginFormState display invalid password when password is blank`() {
        val authenticationViewModel = LoginViewModel()

        coroutineTestRule.dispatcher.runBlockingTest {
            authenticationViewModel.onLoginInputChanged("test", "")
            authenticationViewModel.loginFormState.asFlow().test {
                val state = LoginFormState(passwordError = R.string.invalid_password)
                assertThat(expectItem(), equalTo(state))
                expectNoEvents()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `loginFormState display invalid username error when username is blank`() {
        val authenticationViewModel = LoginViewModel()

        coroutineTestRule.dispatcher.runBlockingTest {
            authenticationViewModel.onLoginInputChanged("", "test")
            authenticationViewModel.loginFormState.asFlow().test {
                val state = LoginFormState(usernameError = R.string.invalid_username)
                assertThat(expectItem(), equalTo(state))
                expectNoEvents()
            }
        }
    }
}