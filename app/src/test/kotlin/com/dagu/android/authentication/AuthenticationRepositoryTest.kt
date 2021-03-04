package com.dagu.android.authentication

import app.cash.turbine.test
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.dagu.android.BaseCoroutineTest
import com.dagu.android.BuildConfig
import com.dagu.android.data.authentication.AuthenticationRepository
import com.dagu.android.data.authentication.api.AuthenticationApi
import com.dagu.android.data.authentication.api.OloUserAccountApi
import com.dagu.android.data.authentication.api.UserAccountApi
import com.dagu.android.data.authentication.model.*
import com.dagu.android.data.repository.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.Credentials
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import java.io.IOException
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class AuthenticationRepositoryTest : BaseCoroutineTest() {
    private val basicAuthHeader: String by lazy {
        Credentials.basic(BuildConfig.SSMA_CLIENT_ID, BuildConfig.SSMA_CLIENT_SECRET)
    }

    @ExperimentalTime
    @Test
    fun `userProfileLookup should emit a sequence of loading results and a success result of UserProfileResponse`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val userProfileLookupResponse = UserProfileLookupResponse("Test", "password", null)
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val successResult = Result.Success(userProfileLookupResponse)
            val mockUserApi = mock<UserAccountApi> {
                onBlocking {
                    userProfileLookup(
                        basicAuthHeader,
                        UserProfileLookupRequest("test")
                    )
                } doReturn userProfileLookupResponse
            }
            val authenticationRepository =
                AuthenticationRepository(mock(), mockUserApi, mock(), coroutineTestRule.dispatcher)
            val flow: Flow<Result<UserProfileLookupResponse>> =
                authenticationRepository.userProfileLookup("test")
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                Assert.assertEquals(expectItem(), successResult)
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `userProfileLookup should emit a sequence of loading results and a Error when an exception occurs on the API call`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val exception = IOException()
            val mockUserApi = mock<UserAccountApi> {
                onBlocking {
                    userProfileLookup(
                        basicAuthHeader,
                        UserProfileLookupRequest("test")
                    )
                } doAnswer {
                    throw exception
                }
            }
            val authenticationRepository =
                AuthenticationRepository(mock(), mockUserApi, mock(), coroutineTestRule.dispatcher)
            val flow: Flow<Result<UserProfileLookupResponse>> =
                authenticationRepository.userProfileLookup("test")
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                val resultError = expectItem() as Result.Error
                assertThat(
                    resultError.exception,
                    CoreMatchers.sameInstance(exception)
                )
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `login should emit a sequence of loading results and a success result of UserAuthentication Response`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val userAuth = UserAuthentication("test", "testToken", 1L, "test", "test")
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val successResult = Result.Success(userAuth)
            val mockAuthenticationApi = mock<AuthenticationApi> {
                onBlocking {
                    getToken(
                        basicAuthHeader,
                        "password",
                        "test",
                        "test"
                    )
                } doReturn userAuth
            }
            val authenticationRepository = AuthenticationRepository(
                mockAuthenticationApi,
                mock(),
                mock(),
                coroutineTestRule.dispatcher
            )
            val flow: Flow<Result<UserAuthentication>> =
                authenticationRepository.login("password", "test", "test")
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                Assert.assertEquals(expectItem(), successResult)
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `login should emit a sequence of loading results and a Error when an exception occurs on the API call`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val exception = IOException()
            val mockAuthenticationApi = mock<AuthenticationApi> {
                onBlocking { getToken(basicAuthHeader, "password", "test", "test") } doAnswer {
                    throw exception
                }
            }
            val authenticationRepository = AuthenticationRepository(
                mockAuthenticationApi,
                mock(),
                mock(),
                coroutineTestRule.dispatcher
            )
            val flow: Flow<Result<UserAuthentication>> =
                authenticationRepository.login("password", "test", "test")
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                val resultError = expectItem() as Result.Error
                assertThat(
                    resultError.exception,
                    CoreMatchers.sameInstance(exception)
                )
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `refreshToken should emit a sequence of loading results and a success result of UserAuthentication Response`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val userAuth = UserAuthentication("test", "testToken", 1L, "test", "test")
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val successResult = Result.Success(userAuth)
            val mockAuthenticationApi = mock<AuthenticationApi> {
                onBlocking {
                    getRefreshToken(
                        basicAuthHeader,
                        "password",
                        "password"
                    )
                } doReturn userAuth
            }
            val authenticationRepository = AuthenticationRepository(
                mockAuthenticationApi,
                mock(),
                mock(),
                coroutineTestRule.dispatcher
            )
            val flow: Flow<Result<UserAuthentication>> =
                authenticationRepository.refreshToken("password", "password")
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                Assert.assertEquals(expectItem(), successResult)
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `refreshToken should emit a sequence of loading results and a Error when an exception occurs on the API call`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val exception = IOException()
            val mockAuthenticationApi = mock<AuthenticationApi> {
                onBlocking { getRefreshToken(basicAuthHeader, "password", "password") } doAnswer {
                    throw exception
                }
            }
            val authenticationRepository = AuthenticationRepository(
                mockAuthenticationApi,
                mock(),
                mock(),
                coroutineTestRule.dispatcher
            )
            val flow: Flow<Result<UserAuthentication>> =
                authenticationRepository.refreshToken("password", "password")
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                val resultError = expectItem() as Result.Error
                assertThat(
                    resultError.exception,
                    CoreMatchers.sameInstance(exception)
                )
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `getUserData should emit a sequence of loading results and a success result of UserData`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val userData = UserData(
                dob = "11/23/1993",
                encryptedMagicalImmutable = "o+V/r0VwG0/PJnlDEn+4+A==",
                favItemChainId = "32",
                gender = "Male",
                grantFlowType = "password",
                id = 1,
                isAreaManager = false,
                isStaff = false,
                isStoreManager = false,
                isSuperuser = false,
                isTeamMember = false,
                kids = "",
                kiosk = "12",
                locations = null,
                loyaltyProviders = null,
                magicalImmutableIv = "",
                needsToEnterNewPassword = false,
                oloProviderToken = "",
                optoutEmail = false,
                optoutMessages = false,
                pets = "",
                referralCode = "",
                type = "",
                user = UserData.User("fakeemail@gmail.com", "Fake", "Email", "fake_name"),
                userprofileAllergens = null,
                zip = "60612"
            )
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val successResult = Result.Success(userData)
            val mockUserApi = mock<UserAccountApi> {
                onBlocking { getUserData(basicAuthHeader) } doReturn userData
            }
            val authenticationRepository =
                AuthenticationRepository(mock(), mockUserApi, mock(), coroutineTestRule.dispatcher)
            val flow: Flow<Result<UserData>> = authenticationRepository.getUserData(basicAuthHeader)
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                Assert.assertEquals(expectItem(), successResult)
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `getUserData should emit a sequence of loading results and a Error when an exception occurs on the API call`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val exception = IOException()
            val mockUserApi = mock<UserAccountApi> {
                onBlocking { getUserData(basicAuthHeader) } doAnswer {
                    throw exception
                }
            }
            val authenticationRepository =
                AuthenticationRepository(mock(), mockUserApi, mock(), coroutineTestRule.dispatcher)
            val flow: Flow<Result<UserData>> = authenticationRepository.getUserData(basicAuthHeader)
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                val resultError = expectItem() as Result.Error
                assertThat(
                    resultError.exception,
                    CoreMatchers.sameInstance(exception)
                )
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `getOloToken should emit a sequence of loading results and a success result of OloAuthenticationResponse`() {
        coroutineTestRule.dispatcher.runBlockingTest {
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
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val successResult = Result.Success(oloData)
            val mockOloApi = mock<OloUserAccountApi> {
                onBlocking {
                    getOrCreateOloUser(
                        "",
                        OloAuthenticationRequest("", "", "")
                    )
                } doReturn oloData
            }
            val authenticationRepository =
                AuthenticationRepository(mock(), mock(), mockOloApi, coroutineTestRule.dispatcher)
            val flow: Flow<Result<OloAuthenticationResponse>> =
                authenticationRepository.getOloToken("", OloAuthenticationRequest("", "", ""))
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                Assert.assertEquals(expectItem(), successResult)
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `getOloToken should emit a sequence of loading results and a Error when an exception occurs on the API call`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val exception = IOException()
            val mockOloApi = mock<OloUserAccountApi> {
                onBlocking {
                    getOrCreateOloUser(
                        "",
                        OloAuthenticationRequest("", "", "")
                    )
                } doAnswer {
                    throw exception
                }
            }
            val authenticationRepository =
                AuthenticationRepository(mock(), mock(), mockOloApi, coroutineTestRule.dispatcher)
            val flow: Flow<Result<OloAuthenticationResponse>> =
                authenticationRepository.getOloToken("", OloAuthenticationRequest("", "", ""))
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                val resultError = expectItem() as Result.Error
                assertThat(
                    resultError.exception,
                    CoreMatchers.sameInstance(exception)
                )
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `updateOloUserContactDetail should emit a sequence of loading results and a success result of OloUserContactDetailsResponse`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val oloData = OloUserContactDetailsResponse("")
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val successResult = Result.Success(oloData)
            val mockOloApi = mock<OloUserAccountApi> {
                onBlocking {
                    updateOloContactDetails(
                        "",
                        "",
                        UpdateOloContactDetailsRequest("")
                    )
                } doReturn oloData
            }
            val authenticationRepository =
                AuthenticationRepository(mock(), mock(), mockOloApi, coroutineTestRule.dispatcher)
            val flow: Flow<Result<OloUserContactDetailsResponse>> =
                authenticationRepository.updateOloUserContactDetail("", "", "")
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                Assert.assertEquals(expectItem(), successResult)
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `updateOloUserContactDetail should emit a sequence of loading results and a Error when an exception occurs on the API call`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val exception = IOException()
            val mockOloApi = mock<OloUserAccountApi> {
                onBlocking {
                    updateOloContactDetails(
                        "",
                        "",
                        UpdateOloContactDetailsRequest("")
                    )
                } doAnswer {
                    throw exception
                }
            }
            val authenticationRepository =
                AuthenticationRepository(mock(), mock(), mockOloApi, coroutineTestRule.dispatcher)
            val flow: Flow<Result<OloUserContactDetailsResponse>> =
                authenticationRepository.updateOloUserContactDetail("", "", "")
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                val resultError = expectItem() as Result.Error
                assertThat(
                    resultError.exception,
                    CoreMatchers.sameInstance(exception)
                )
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `getOloUserContactDetail should emit a sequence of loading results and a success result of OloUserContactDetailsResponse`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val oloData = OloUserContactDetailsResponse("")
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val successResult = Result.Success(oloData)
            val mockOloApi = mock<OloUserAccountApi> {
                onBlocking { getOloContactDetails("", "") } doReturn oloData
            }
            val authenticationRepository =
                AuthenticationRepository(mock(), mock(), mockOloApi, coroutineTestRule.dispatcher)
            val flow: Flow<Result<OloUserContactDetailsResponse>> =
                authenticationRepository.getOloUserContactDetails("", "")
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                Assert.assertEquals(expectItem(), successResult)
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `getOloUserContactDetail should emit a sequence of loading results and a Error when an exception occurs on the API call`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val exception = IOException()
            val mockOloApi = mock<OloUserAccountApi> {
                onBlocking { getOloContactDetails("", "") } doAnswer {
                    throw exception
                }
            }
            val authenticationRepository =
                AuthenticationRepository(mock(), mock(), mockOloApi, coroutineTestRule.dispatcher)
            val flow: Flow<Result<OloUserContactDetailsResponse>> =
                authenticationRepository.getOloUserContactDetails("", "")
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                val resultError = expectItem() as Result.Error
                assertThat(
                    resultError.exception,
                    CoreMatchers.sameInstance(exception)
                )
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `createSSMAUser should emit a sequence of loading results and a success result of UserData`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val createSSMAUserRequest = CreateSSMAUserRequest(
                user = SSMAUser(
                    "Fake",
                    "Email",
                    "fakeemail@gmail.com",
                    "fakeemail@gmail.com",
                    "password"
                )
            )
            val userData = UserData(
                dob = "11/23/1993",
                encryptedMagicalImmutable = "o+V/r0VwG0/PJnlDEn+4+A==",
                favItemChainId = "32",
                gender = "Male",
                grantFlowType = "password",
                id = 1,
                isAreaManager = false,
                isStaff = false,
                isStoreManager = false,
                isSuperuser = false,
                isTeamMember = false,
                kids = "",
                kiosk = "12",
                locations = null,
                loyaltyProviders = null,
                magicalImmutableIv = "",
                needsToEnterNewPassword = false,
                oloProviderToken = "",
                optoutEmail = false,
                optoutMessages = false,
                pets = "",
                referralCode = "",
                type = "",
                user = UserData.User("fakeemail@gmail.com", "Fake", "Email", "fake_name"),
                userprofileAllergens = null,
                zip = "60612"
            )
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val successResult = Result.Success(userData)
            val mockUserApi = mock<UserAccountApi> {
                onBlocking {
                    createSSMAUser(
                        basicAuthHeader,
                        createSSMAUserBody = createSSMAUserRequest
                    )
                } doReturn userData
            }
            val authenticationRepository =
                AuthenticationRepository(mock(), mockUserApi, mock(), coroutineTestRule.dispatcher)
            val flow: Flow<Result<UserData>> = authenticationRepository.createSSMAUser(
                "fakeemail@gmail.com",
                "password",
                "Fake",
                "Email"
            )
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                Assert.assertEquals(expectItem(), successResult)
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `createSSMAUser should emit a sequence of loading results and a Error when an exception occurs on the API call`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val createSSMAUserRequest = CreateSSMAUserRequest(
                user = SSMAUser(
                    "Fake",
                    "Email",
                    "fakeemail@gmail.com",
                    "fakeemail@gmail.com",
                    "password"
                )
            )

            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val exception = IOException()
            val mockUserApi = mock<UserAccountApi> {
                onBlocking {
                    createSSMAUser(
                        basicAuthHeader,
                        createSSMAUserBody = createSSMAUserRequest
                    )
                } doAnswer {
                    throw exception
                }
            }
            val authenticationRepository =
                AuthenticationRepository(mock(), mockUserApi, mock(), coroutineTestRule.dispatcher)
            val flow: Flow<Result<UserData>> =
                authenticationRepository.createSSMAUser(
                    "fakeemail@gmail.com",
                    "password",
                    "Fake",
                    "Email"
                )
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                val resultError = expectItem() as Result.Error
                assertThat(
                    resultError.exception,
                    CoreMatchers.sameInstance(exception)
                )
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `loginWithFacebook should emit a sequence of loading results and a success result of UserAuthentication`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val userAuth = UserAuthentication("test", "testToken", 1L, "test", "test")
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val successResult = Result.Success(userAuth)
            val mockAuthenticationApi = mock<AuthenticationApi> {
                onBlocking {
                    loginWithFacebook(
                        basicAuthHeader,
                        accessToken = "access_token"
                    )
                } doReturn userAuth
            }
            val authenticationRepository =
                AuthenticationRepository(
                    mockAuthenticationApi,
                    mock(),
                    mock(),
                    coroutineTestRule.dispatcher
                )
            val flow: Flow<Result<UserAuthentication>> =
                authenticationRepository.loginWithFacebook("access_token")
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                Assert.assertEquals(expectItem(), successResult)
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `loginWithFacebook should emit a sequence of loading results and a Error when an exception occurs on the API call`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val exception = IOException()
            val mockAuthenticationApi = mock<AuthenticationApi> {
                onBlocking {
                    loginWithFacebook(
                        basicAuthHeader,
                        accessToken = "access_token"
                    )
                } doAnswer {
                    throw exception
                }
            }
            val authenticationRepository =
                AuthenticationRepository(
                    mockAuthenticationApi,
                    mock(),
                    mock(),
                    coroutineTestRule.dispatcher
                )
            val flow: Flow<Result<UserAuthentication>> =
                authenticationRepository.loginWithFacebook("access_token")
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                val resultError = expectItem() as Result.Error
                assertThat(
                    resultError.exception,
                    CoreMatchers.sameInstance(exception)
                )
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `googleSignIn should emit a sequence of loading results and a success result of UserAuthentication`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val userAuth = UserAuthentication("test", "testToken", 1L, "test", "test")
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val successResult = Result.Success(userAuth)
            val mockAuthenticationApi = mock<AuthenticationApi> {
                onBlocking {
                    googleSignIn(
                        basicAuthHeader,
                        grantType = "implicit",
                        tokenId = "token_id"
                    )
                } doReturn userAuth
            }
            val authenticationRepository =
                AuthenticationRepository(
                    mockAuthenticationApi,
                    mock(),
                    mock(),
                    coroutineTestRule.dispatcher
                )
            val flow: Flow<Result<UserAuthentication>> =
                authenticationRepository.googleSignIn(
                    grantType = "implicit",
                    tokenId = "token_id"
                )
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                Assert.assertEquals(expectItem(), successResult)
                expectComplete()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `googleSignIn should emit a sequence of loading results and a Error when an exception occurs on the API call`() {
        coroutineTestRule.dispatcher.runBlockingTest {
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)
            val exception = IOException()
            val mockAuthenticationApi = mock<AuthenticationApi> {
                onBlocking {
                    googleSignIn(
                        basicAuthHeader,
                        grantType = "implicit",
                        tokenId = "token_id"
                    )
                } doAnswer {
                    throw exception
                }
            }
            val authenticationRepository =
                AuthenticationRepository(
                    mockAuthenticationApi,
                    mock(),
                    mock(),
                    coroutineTestRule.dispatcher
                )
            val flow: Flow<Result<UserAuthentication>> =
                authenticationRepository.googleSignIn(
                    grantType = "implicit",
                    tokenId = "token_id"
                )
            flow.test {
                Assert.assertEquals(expectItem(), loadingResult)
                Assert.assertEquals(expectItem(), notLoadingResult)
                val resultError = expectItem() as Result.Error
                assertThat(
                    resultError.exception,
                    CoreMatchers.sameInstance(exception)
                )
                expectComplete()
            }
        }
    }

}

