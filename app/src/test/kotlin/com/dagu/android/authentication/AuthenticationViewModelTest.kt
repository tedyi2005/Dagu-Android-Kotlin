package com.dagu.android.authentication

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asFlow
import app.cash.turbine.test
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.dagu.android.BaseCoroutineTest
import com.dagu.android.data.authentication.AccountPreferencesRepository
import com.dagu.android.data.authentication.AuthenticationRepository
import com.dagu.android.data.authentication.model.*
import com.dagu.android.data.repository.Result
import com.dagu.android.data.repository.UIResult
import com.dagu.android.presentation.authentication.AuthenticationViewModel
import com.dagu.android.util.getApiErrorMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class AuthenticationViewModelTest : BaseCoroutineTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `loginResult Live Data should emit UISuccess after AuthenticationRepository return a Success Result`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val userAuth = UserAuthentication("test", "testToken", 1L, "test", "test")
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val successResult = Result.Success(userAuth)
                    onBlocking { login("password", "test", "test") } doReturn flowOf(successResult)
                }

                val mockAuth = mock<AccountPreferencesRepository>()

                val authenticationViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAuth,
                    coroutineTestRule.dispatcher
                )
                authenticationViewModel.login("test", "test")
                authenticationViewModel.loginResult.asFlow().test {
                    val expectedResult = UIResult.Success(userAuth)
                    assertThat(expectItem(), equalTo(expectedResult))
                    expectNoEvents()
                }
            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }
    }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `loginResult Live Data should emit UILoading Result after AuthenticationRepository return a Loading Result`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            val mockAuthenticationRepository = mock<AuthenticationRepository> {
                val loadingResult = Result.Loading(true)
                val notLoadingResult = Result.Loading(false)
                onBlocking { login("password", "test", "test") } doReturn flowOf(
                    loadingResult,
                    notLoadingResult
                )
            }

            val mockAuth = mock<AccountPreferencesRepository>()

            val authenticationViewModel = AuthenticationViewModel(
                mockAuthenticationRepository,
                mockAuth,
                coroutineTestRule.dispatcher
            )
            authenticationViewModel.login("test", "test")
            authenticationViewModel.loginResult.asFlow().test {
                val notLoadingUiResult = UIResult.Loading(false)
                assertThat(expectItem(), equalTo(notLoadingUiResult))
                expectNoEvents()
            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `loginResult Live Data should emit UIError Result after AuthenticationRepository return a Error Result`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val exception = Exception("test")
                    val errorResult = Result.Error(exception)
                    onBlocking { login("password", "test", "test") } doReturn flowOf(errorResult)
                }

                val mockAuth = mock<AccountPreferencesRepository>()

                val authenticationViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAuth,
                    coroutineTestRule.dispatcher
                )
                authenticationViewModel.login("test", "test")

                val errorUIResult = UIResult.Error("Login failed: test")

                authenticationViewModel.loginResult.asFlow().test {
                    assertThat(expectItem(), equalTo(errorUIResult))
                    expectNoEvents()
                }
            }

            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `authToken Live Data should be equal to auth token string passed in to setAuthTokenFromAccountManager`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()

                val mockAuth = mock<AuthenticationRepository>()
                val mainViewModel = AuthenticationViewModel(
                    mockAuth,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                mainViewModel.setAuthTokenFromAccountManager("test")
                mainViewModel.authToken.asFlow().test {
                    assertThat(
                        expectItem(),
                        CoreMatchers.equalTo("test")
                    )
                    expectNoEvents()
                }

            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }
    }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `authToken Live Data should be empty on removeUserDataFromAccountManager called`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()

                val mockAuth = mock<AuthenticationRepository>()
                val mainViewModel = AuthenticationViewModel(
                    mockAuth,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                mainViewModel.removeUserDataFromAccountManager()
                mainViewModel.authToken.asFlow().test {
                    assertThat(
                        expectItem(),
                        CoreMatchers.equalTo("")
                    )
                    expectNoEvents()
                }

            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }
    }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `userData Live Data should emit UISuccess after AuthenticationRepository return a Success Result for getUserData`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val userData = UserData(
                    "11/23/1993",
                    "o+V/r0VwG0/PJnlDEn+4+A==",
                    "32",
                    "Male",
                    "password",
                    1,
                    false,
                    false,
                    false,
                    false,
                    false,
                    "",
                    "12",
                    null,
                    null,
                    "",
                    false,
                    "",
                    false,
                    false,
                    "",
                    "",
                    "",
                    UserData.User("fakeemail@gmail.com", "Fake", "Email", "fakename"),
                    null,
                    "60612"
                )
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val successResult = Result.Success(userData)
                    onBlocking { getUserData("Bearer test") } doReturn flowOf(successResult)
                }

                val mainViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                mainViewModel.getUserData("test")
                mainViewModel.userData.asFlow().test {
                    val expectedResult = UIResult.Success(userData)
                    assertThat(expectItem(), CoreMatchers.equalTo(expectedResult))
                    expectNoEvents()
                }

            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }
    }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `userData Live Data should emit UILoading Result after AuthenticationRepository return a Loading Result for getUserData`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val loadingResult = Result.Loading(true)
                    val notLoadingResult = Result.Loading(false)
                    onBlocking { getUserData("Bearer test") } doReturn flowOf(
                        loadingResult,
                        notLoadingResult
                    )
                }

                val mainViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                mainViewModel.getUserData("test")
                mainViewModel.userData.asFlow().test {
                    val notLoadingUiResult = UIResult.Loading(false)
                    assertThat(expectItem(), CoreMatchers.equalTo(notLoadingUiResult))
                    expectNoEvents()
                }
            }

            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `userData Live Data should emit UIError Result after AuthenticationRepository return a Error Result for getUserData`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val exception = Exception("test")
                    val errorResult = Result.Error(exception)
                    onBlocking { getUserData("Bearer test") } doReturn flowOf(errorResult)
                }

                val mainViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                mainViewModel.getUserData("test")
                val errorUIResult = UIResult.Error("Error fetching user data: test")
                mainViewModel.userData.asFlow().test {
                    assertThat(expectItem(), CoreMatchers.equalTo(errorUIResult))
                    expectNoEvents()
                }
            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `oloUserData Live Data should emit UISuccess after AuthenticationRepository return a Success Result for getOloAuthToken`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val oloData = OloAuthenticationResponse(
                    "password",
                    "FCMs9EEz08NFqejEzxaEQmD5Yq52cyHT",
                    "12ABC",
                    "111-222-3333",
                    "fakeemail@gmail.com",
                    "",
                    "Fake",
                    "Email",
                    "fuzz-sm",
                    "zCiyqNOyPbPf8EhOfil0YAtKlea5rR",
                    "jlbounteous11@gmail.com",
                    "jfdsklfklj23rkljflksjdl"
                )
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()

                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val oloAuthRequest = OloAuthenticationRequest("fuzz-sm", "test", "test")
                    val successResult = Result.Success(oloData)
                    onBlocking {
                        getOloToken(
                            "test",
                            oloAuthRequest
                        )
                    } doReturn flowOf(successResult)
                }

                val mainViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                mainViewModel.getOloAuthToken("test", "test", "test")
                mainViewModel.oloUserData.asFlow().test {
                    val expectedResult = UIResult.Success(oloData)
                    assertThat(expectItem(), CoreMatchers.equalTo(expectedResult))
                    expectNoEvents()
                }

            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }
    }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `oloUserData Live Data should emit UILoading Result after AuthenticationRepository return a Loading Result for getOloAuthToken`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val loadingResult = Result.Loading(true)
                    val notLoadingResult = Result.Loading(false)
                    val oloAuthRequest = OloAuthenticationRequest("fuzz-sm", "test", "test")
                    onBlocking { getOloToken("test", oloAuthRequest) } doReturn flowOf(
                        loadingResult,
                        notLoadingResult
                    )
                }

                val mainViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                mainViewModel.getOloAuthToken("test", "test", "test")
                mainViewModel.oloUserData.asFlow().test {
                    val notLoadingUiResult = UIResult.Loading(false)
                    assertThat(expectItem(), CoreMatchers.equalTo(notLoadingUiResult))
                    expectNoEvents()
                }
            }

            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `oloUserData Live Data should emit UIError Result after AuthenticationRepository return a Error Result for getOloAuthToken`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val exception = Exception("test")
                    val errorResult = Result.Error(exception)
                    val oloAuthRequest = OloAuthenticationRequest("fuzz-sm", "test", "test")
                    onBlocking { getOloToken("test", oloAuthRequest) } doReturn flowOf(errorResult)
                }

                val mainViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                mainViewModel.getOloAuthToken("test", "test", "test")
                val errorUIResult = UIResult.Error("Error fetching olo user data: test")
                mainViewModel.oloUserData.asFlow().test {
                    assertThat(expectItem(), CoreMatchers.equalTo(errorUIResult))
                    expectNoEvents()
                }
            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `oloUserContactData Live Data should emit UISuccess after AuthenticationRepository return a Success Result for getOloUserData`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val oloData = OloUserContactDetailsResponse("7084332222")
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()

                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val successResult = Result.Success(oloData)
                    onBlocking { getOloUserContactDetails("test", "test") } doReturn flowOf(
                        successResult
                    )
                }

                val mainViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                mainViewModel.getOloUserData("test", "test")
                mainViewModel.oloUserContactData.asFlow().test {
                    val expectedResult = UIResult.Success(oloData)
                    assertThat(expectItem(), CoreMatchers.equalTo(expectedResult))
                    expectNoEvents()
                }

            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }
    }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `oloUserContactData Live Data should emit UILoading Result after AuthenticationRepository return a Loading Result for getOloUserData`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val loadingResult = Result.Loading(true)
                    val notLoadingResult = Result.Loading(false)

                    onBlocking { getOloUserContactDetails("test", "test") } doReturn flowOf(
                        loadingResult,
                        notLoadingResult
                    )
                }

                val mainViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                mainViewModel.getOloUserData("test", "test")
                mainViewModel.oloUserContactData.asFlow().test {
                    val notLoadingUiResult = UIResult.Loading(false)
                    assertThat(expectItem(), CoreMatchers.equalTo(notLoadingUiResult))
                    expectNoEvents()
                }
            }

            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `oloUserContactData Live Data should emit UIError Result after AuthenticationRepository return a Error Result for getOloUserData`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val exception = Exception("test")
                    val errorResult = Result.Error(exception)
                    onBlocking { getOloUserContactDetails("test", "test") } doReturn flowOf(
                        errorResult
                    )
                }

                val mainViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                mainViewModel.getOloUserData("test", "test")
                val errorUIResult = UIResult.Error("Error fetching olo user data: test")
                mainViewModel.oloUserContactData.asFlow().test {
                    assertThat(expectItem(), CoreMatchers.equalTo(errorUIResult))
                    expectNoEvents()
                }
            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `facebookLoginResult Live Data should emit UISuccess after AuthenticationRepository return a Success Result`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val userAuth = UserAuthentication("test", "testToken", 1L, "test", "test")
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val successResult = Result.Success(userAuth)
                    onBlocking { loginWithFacebook("access_token") } doReturn flowOf(successResult)
                }

                val mockAuth = mock<AccountPreferencesRepository>()

                val authenticationViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAuth,
                    coroutineTestRule.dispatcher
                )
                authenticationViewModel.loginWithFacebook("access_token")
                authenticationViewModel.facebookLoginResult.asFlow().test {
                    val expectedResult = UIResult.Success(userAuth)
                    assertThat(expectItem(), equalTo(expectedResult))
                    expectNoEvents()
                }
            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }
    }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `facebookLoginResult Live Data should emit UILoading Result after AuthenticationRepository return a Loading Result`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            val mockAuthenticationRepository = mock<AuthenticationRepository> {
                val loadingResult = Result.Loading(true)
                val notLoadingResult = Result.Loading(false)
                onBlocking { loginWithFacebook("access_token") } doReturn flowOf(
                    loadingResult,
                    notLoadingResult
                )
            }

            val mockAuth = mock<AccountPreferencesRepository>()

            val authenticationViewModel = AuthenticationViewModel(
                mockAuthenticationRepository,
                mockAuth,
                coroutineTestRule.dispatcher
            )
            authenticationViewModel.loginWithFacebook("access_token")
            authenticationViewModel.facebookLoginResult.asFlow().test {
                val notLoadingUiResult = UIResult.Loading(false)
                assertThat(expectItem(), equalTo(notLoadingUiResult))
                expectNoEvents()
            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `facebookLoginResult Live Data should emit UIError Result after AuthenticationRepository return a Error Result`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val exception = Exception("test")
                    val errorResult = Result.Error(exception)
                    onBlocking { loginWithFacebook("access_token") } doReturn flowOf(errorResult)
                }

                val mockAuth = mock<AccountPreferencesRepository>()

                val authenticationViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAuth,
                    coroutineTestRule.dispatcher
                )
                authenticationViewModel.loginWithFacebook("access_token")

                val errorUIResult = UIResult.Error("Login with Facebook failed: test")

                authenticationViewModel.facebookLoginResult.asFlow().test {
                    assertThat(expectItem(), equalTo(errorUIResult))
                    expectNoEvents()
                }
            }

            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `googleLoginResult Live Data should emit UISuccess after AuthenticationRepository return a Success Result`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val userAuth = UserAuthentication("test", "testToken", 1L, "test", "test")
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val successResult = Result.Success(userAuth)
                    onBlocking { googleSignIn("implicit", "token_id") } doReturn flowOf(
                        successResult
                    )
                }

                val mockAuth = mock<AccountPreferencesRepository>()

                val authenticationViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAuth,
                    coroutineTestRule.dispatcher
                )
                authenticationViewModel.googleSignIn("implicit", "token_id")
                authenticationViewModel.googleLoginResult.asFlow().test {
                    val expectedResult = UIResult.Success(userAuth)
                    assertThat(expectItem(), equalTo(expectedResult))
                    expectNoEvents()
                }
            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }
    }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `googleLoginResult Live Data should emit UILoading Result after AuthenticationRepository return a Loading Result`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            val mockAuthenticationRepository = mock<AuthenticationRepository> {
                val loadingResult = Result.Loading(true)
                val notLoadingResult = Result.Loading(false)
                onBlocking { googleSignIn("implicit", "token_id") } doReturn flowOf(
                    loadingResult,
                    notLoadingResult
                )
            }

            val mockAuth = mock<AccountPreferencesRepository>()

            val authenticationViewModel = AuthenticationViewModel(
                mockAuthenticationRepository,
                mockAuth,
                coroutineTestRule.dispatcher
            )
            authenticationViewModel.googleSignIn("implicit", "token_id")
            authenticationViewModel.googleLoginResult.asFlow().test {
                val notLoadingUiResult = UIResult.Loading(false)
                assertThat(expectItem(), equalTo(notLoadingUiResult))
                expectNoEvents()
            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `googleLoginResult Live Data should emit UIError Result after AuthenticationRepository return a Error Result`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val exception = Exception("test")
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val errorResult = Result.Error(exception)
                    onBlocking { googleSignIn("implicit", "token_id") } doReturn flowOf(errorResult)
                }

                val mockAuth = mock<AccountPreferencesRepository>()

                val authenticationViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAuth,
                    coroutineTestRule.dispatcher
                )
                authenticationViewModel.googleSignIn("implicit", "token_id")

                val errorUIResult = UIResult.Error(exception.getApiErrorMessage())

                authenticationViewModel.googleLoginResult.asFlow().test {
                    assertThat(expectItem(), equalTo(errorUIResult))
                    expectNoEvents()
                }
            }

            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `createUserResult Live Data should emit UISuccess after AuthenticationRepository return a Success Result for createSSMAUser`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val userData = UserData(
                    "11/23/1993",
                    "o+V/r0VwG0/PJnlDEn+4+A==",
                    "32",
                    "Male",
                    "password",
                    1,
                    false,
                    false,
                    false,
                    false,
                    false,
                    "",
                    "12",
                    null,
                    null,
                    "",
                    false,
                    "",
                    false,
                    false,
                    "",
                    "",
                    "",
                    UserData.User("fakeemail@gmail.com", "Fake", "Email", "fakeemail@gmail.com"),
                    null,
                    "60612"
                )
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val successResult = Result.Success(userData)
                    onBlocking {
                        createSSMAUser(
                            "fakeemail@gmail.com",
                            "fake_password",
                            "Fake",
                            "Name"
                        )
                    } doReturn flowOf(successResult)
                }

                val authenticationViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                authenticationViewModel.createAccount(
                    "fakeemail@gmail.com",
                    "Fake",
                    "Name",
                    "fake_password",
                )
                authenticationViewModel.createUserResult.asFlow().test {
                    val expectedResult = UIResult.Success(userData)
                    assertThat(expectItem(), equalTo(expectedResult))
                    expectNoEvents()
                }

            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }
    }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `createUserResult Live Data should emit UILoading Result after AuthenticationRepository return a Loading Result for getUserData`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val loadingResult = Result.Loading(true)
                    val notLoadingResult = Result.Loading(false)
                    onBlocking {
                        createSSMAUser(
                            "fakeemail@gmail.com",
                            "fake_password",
                            "Fake",
                            "Name"
                        )
                    } doReturn flowOf(
                        loadingResult,
                        notLoadingResult
                    )
                }

                val mainViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                mainViewModel.createAccount(
                    "fakeemail@gmail.com",
                    "Fake",
                    "Name",
                    "fake_password",
                )
                mainViewModel.createUserResult.asFlow().test {
                    val notLoadingUiResult = UIResult.Loading(false)
                    assertThat(expectItem(), equalTo(notLoadingUiResult))
                    expectNoEvents()
                }
            }

            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `createUserResult Live Data should emit UIError Result after AuthenticationRepository return a Error Result for getUserData`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockAccountPreferencesRepository = mock<AccountPreferencesRepository>()
                val mockAuthenticationRepository = mock<AuthenticationRepository> {
                    val exception = Exception("test")
                    val errorResult = Result.Error(exception)
                    onBlocking {
                        createSSMAUser(
                            "fakeemail@gmail.com",
                            "fake_password",
                            "Fake",
                            "Name"
                        )
                    } doReturn flowOf(errorResult)
                }

                val mainViewModel = AuthenticationViewModel(
                    mockAuthenticationRepository,
                    mockAccountPreferencesRepository,
                    coroutineTestRule.dispatcher
                )
                mainViewModel.createAccount(
                    "fakeemail@gmail.com",
                    "Fake",
                    "Name",
                    "fake_password",
                )
                val errorUIResult = UIResult.Error("Create SSMA user failed: test")
                mainViewModel.createUserResult.asFlow().test {
                    assertThat(expectItem(), equalTo(errorUIResult))
                    expectNoEvents()
                }
            }
            coroutineTestRule.dispatcher.resumeDispatcher()
        }
}
