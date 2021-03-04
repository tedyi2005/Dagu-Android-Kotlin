package com.dagu.android.presentation.account

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asFlow
import app.cash.turbine.test
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.dagu.android.BaseCoroutineTest
import com.dagu.android.data.authentication.AuthenticationRepository
import com.dagu.android.data.authentication.model.*
import com.dagu.android.data.repository.Result
import com.dagu.android.presentation.account.viewmodel.AccountOverviewViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class AccountOverviewViewModelTest : BaseCoroutineTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `getUserData() gets user data and asks Olo for contact details, user profile updated from both sources`() {

        // Note: Using `anyString()` to cover for `BuildConfig.OLO_KEY`.

        // Prepare test subject...

        val authToken = "authToken"

        val userDataResponse = buildUserData("John", "Doe")
        val userDataSuccessResult = Result.Success(userDataResponse)

        val oloAuthRequest = OloAuthenticationRequest(
            "fuzz-sm",
            userDataResponse.oloProviderToken,
            userDataResponse.user!!.email
        )
        val oloAuthenticationResponse = buildOloAuthenticationResponse()
        val oloAuthenticationSuccessResult = Result.Success(oloAuthenticationResponse)

        val oloContactDetailsResponse = buildOloContactDetailsResponse()
        val oloContactDetailsSuccessResult = Result.Success(oloContactDetailsResponse)

        val mockAuthenticationRepository = mock<AuthenticationRepository> {
            onBlocking {
                getUserData("Bearer $authToken")
            } doReturn flowOf(userDataSuccessResult)

            onBlocking {
                getOloToken(anyString(), eq(oloAuthRequest))
            } doReturn flowOf(oloAuthenticationSuccessResult)

            onBlocking {
                getOloUserContactDetails(eq("oloAuthToken"), anyString())
            } doReturn flowOf(oloContactDetailsSuccessResult)
        }

        val subject = AccountOverviewViewModel(
            mockAuthenticationRepository,
            mock()
        )

        // Use a coroutine-proof block:
        coroutineTestRule.dispatcher.runBlockingTest {

            // Call method to test:
            subject.getUserData(authToken)

            // Verify method calls...
            //1. Get user data from our server.
            //2. (Use Olo key from [1] to) Get Olo token from Olo server.
            //3. (Use Olo token from [2] to) Get user contact details (phone number).
            verify(mockAuthenticationRepository).getUserData("Bearer $authToken")
            verify(mockAuthenticationRepository).getOloToken(
                anyString(),
                eq(oloAuthRequest)
            )
            verify(mockAuthenticationRepository).getOloUserContactDetails(
                eq("oloAuthToken"),
                anyString()
            )

            // Check data was updated as expected...
            subject.userProfile.asFlow().test {
                val newProfile = expectItem()

                // From our user data request...
                assertThat(newProfile.id, equalTo(345))
                assertThat(newProfile.firstName, equalTo("John"))
                assertThat(newProfile.lastName, equalTo("Doe"))
                assertThat(newProfile.email, equalTo("an@email.com"))
                assertThat(newProfile.receiveMarketingEmails, equalTo(true))
                assertThat(newProfile.receiveSmsFromShakeShack, equalTo(false))
                assertThat(newProfile.genderSelection?.itemName, equalTo("Female"))
                assertThat(newProfile.kidsSelection?.itemName, equalTo("Nope"))
                assertThat(newProfile.petsSelection?.size, equalTo(2))
                assertThat(newProfile.petsSelection?.get(0)?.itemName, equalTo("Cat"))
                assertThat(newProfile.petsSelection?.get(1)?.itemName, equalTo("Fish"))

                // From Olo contact details request...
                //Same number from Olo response, but US phone format applied:
                assertThat(newProfile.phoneNumber, equalTo("(333) 333-4444"))

                // No other updates to this LiveData:
                expectNoEvents()
            }
            subject.addresses.asFlow().test {
                val newAddresses = expectItem()

                // From our user data request...
                assertThat(newAddresses.size, equalTo(1))
                assertThat(newAddresses[0].firstLine, equalTo("60612"))

                // No other updates to this LiveData:
                expectNoEvents()
            }
            subject.allergens.asFlow().test {
                val newAllergens = expectItem()

                // From our user data request...
                assertThat(newAllergens.size, equalTo(3))
                assertThat(newAllergens[0].itemName, equalTo("Soy"))
                assertThat(newAllergens[1].itemName, equalTo("Egg"))
                assertThat(newAllergens[2].itemName, equalTo("Peanut"))

                // No other updates to this LiveData:
                expectNoEvents()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `changePassword() calls repository and updates profile if got a success response`() {

        // Note: Using `anyString()` to cover for `BuildConfig.OLO_KEY`.

        // Prepare test subject...

        val authToken = "authToken"

        //Responses for initial user data fetching...
        val originalUserDataResponse = buildUserData("OriginalFirst", "OriginalLast")
        val originalUserDataSuccessResult = Result.Success(originalUserDataResponse)
        val oloAuthRequest = OloAuthenticationRequest(
            "fuzz-sm",
            originalUserDataResponse.oloProviderToken,
            originalUserDataResponse.user!!.email
        )
        val oloAuthenticationResponse = buildOloAuthenticationResponse()
        val oloAuthenticationSuccessResult = Result.Success(oloAuthenticationResponse)
        val oloContactDetailsResponse = buildOloContactDetailsResponse()
        val oloContactDetailsSuccessResult = Result.Success(oloContactDetailsResponse)

        //Responses for changePassword()...
        val updatedUserDataResponse = buildUserData("UpdatedFirst", "UpdatedLast")
        val updatedUserDataSuccessResult = Result.Success(updatedUserDataResponse)

        val updateUserNameRequest = UpdateBasicUserDataRequest(
            firstName = "requestFirst",
            lastName = "requestLast",
            newPassword = "requestPassword"
        )
        val mockAuthenticationRepository = mock<AuthenticationRepository> {

            //Responses for initial user data fetching...
            onBlocking {
                getUserData("Bearer $authToken")
            } doReturn flowOf(originalUserDataSuccessResult)

            onBlocking {
                getOloToken(anyString(), eq(oloAuthRequest))
            } doReturn flowOf(oloAuthenticationSuccessResult)

            onBlocking {
                getOloUserContactDetails(eq("oloAuthToken"), anyString())
            } doReturn flowOf(oloContactDetailsSuccessResult)

            //Responses for changePassword()...
            onBlocking {
                updateUserName("authToken", 345, updateUserNameRequest)
            } doReturn flowOf(updatedUserDataSuccessResult)
        }

        val subject = AccountOverviewViewModel(
            mockAuthenticationRepository,
            mock()
        )
        subject.oloToken = "oloAuthToken"

        // Use a coroutine-proof block:
        coroutineTestRule.dispatcher.runBlockingTest {

            // Request initial user data:
            subject.getUserData(authToken)

            // Check original profile data...
            subject.userProfile.asFlow().test {
                val newProfile = expectItem()

                // Data to be updated...
                assertThat(newProfile.firstName, equalTo("OriginalFirst"))
                assertThat(newProfile.lastName, equalTo("OriginalLast"))
                assertThat(newProfile.phoneNumber, equalTo("(333) 333-4444"))

                // Rest of original data...
                assertThat(newProfile.id, equalTo(345))
                assertThat(newProfile.email, equalTo("an@email.com"))
                assertThat(newProfile.receiveMarketingEmails, equalTo(true))
                assertThat(newProfile.receiveSmsFromShakeShack, equalTo(false))
                assertThat(newProfile.genderSelection?.itemName, equalTo("Female"))
                assertThat(newProfile.kidsSelection?.itemName, equalTo("Nope"))
                assertThat(newProfile.petsSelection?.size, equalTo(2))
                assertThat(newProfile.petsSelection?.get(0)?.itemName, equalTo("Cat"))
                assertThat(newProfile.petsSelection?.get(1)?.itemName, equalTo("Fish"))
            }

            // Call method to test.
            // Note that these parameters are used for requests and method calls, but the ones
            // we would store at the end come from the response.
            subject.changePassword(newPassword = "requestPassword")

            // Verify method calls...
            verify(mockAuthenticationRepository)
                .updateUserName(
                    authHeader = authToken,
                    id = 345, // Using the id from the original user data we had
                    updateUserNameRequest
                )
            verify(mockAuthenticationRepository).updateOloUserContactDetail(
                eq("oloAuthToken"),
                anyString(),
                eq("0000000000")
            )

            // Check profile data was updated as expected...
            subject.userProfile.asFlow().test {
                val newProfile = expectItem()

                // Updated data...
                assertThat(newProfile.firstName, equalTo("UpdatedFirst"))
                assertThat(newProfile.lastName, equalTo("UpdatedLast"))
                assertThat(newProfile.phoneNumber, equalTo("(666) 999-6666"))

                // Rest of data unchanged...
                assertThat(newProfile.id, equalTo(345))
                assertThat(newProfile.email, equalTo("an@email.com"))
                assertThat(newProfile.receiveMarketingEmails, equalTo(true))
                assertThat(newProfile.receiveSmsFromShakeShack, equalTo(false))
                assertThat(newProfile.genderSelection?.itemName, equalTo("Female"))
                assertThat(newProfile.kidsSelection?.itemName, equalTo("Nope"))
                assertThat(newProfile.petsSelection?.size, equalTo(2))
                assertThat(newProfile.petsSelection?.get(0)?.itemName, equalTo("Cat"))
                assertThat(newProfile.petsSelection?.get(1)?.itemName, equalTo("Fish"))

                // No other updates to this LiveData:
                expectNoEvents()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `changePassword() calls repository and does not update profile if got an error response`() {

        // Note: Using `anyString()` to cover for `BuildConfig.OLO_KEY`.

        // Prepare test subject...

        val authToken = "authToken"

        //Responses for initial user data fetching...
        val originalUserDataResponse = buildUserData("OriginalFirst", "OriginalLast")
        val originalUserDataSuccessResult = Result.Success(originalUserDataResponse)
        val oloAuthRequest = OloAuthenticationRequest(
            "fuzz-sm",
            originalUserDataResponse.oloProviderToken,
            originalUserDataResponse.user!!.email
        )
        val oloAuthenticationResponse = buildOloAuthenticationResponse()
        val oloAuthenticationSuccessResult = Result.Success(oloAuthenticationResponse)
        val oloContactDetailsResponse = buildOloContactDetailsResponse()
        val oloContactDetailsSuccessResult = Result.Success(oloContactDetailsResponse)

        //Responses for updateContactInformation()...
        val updatedUserDataErrorResult = Result.Error(Exception("Could not update user data"))
        val updatedContactDetailErrorResult =
            Result.Error(Exception("Could not update contact details"))

        val updateUserNameRequest = UpdateBasicUserDataRequest(
            firstName = "requestFirst",
            lastName = "requestLast",
            newPassword = "requestPassword"
        )

        val mockAuthenticationRepository = mock<AuthenticationRepository> {

            //Responses for initial user data fetching...
            onBlocking {
                getUserData("Bearer $authToken")
            } doReturn flowOf(originalUserDataSuccessResult)

            onBlocking {
                getOloToken(anyString(), eq(oloAuthRequest))
            } doReturn flowOf(oloAuthenticationSuccessResult)

            //Responses for updateContactInformation()...
            onBlocking {
                updateUserName("authToken", 345, updateUserNameRequest)
            } doReturn flowOf(updatedUserDataErrorResult)
            onBlocking {
                updateOloUserContactDetail(anyString(), anyString(), anyString())
            } doReturn flowOf(updatedContactDetailErrorResult)
        }

        val subject = AccountOverviewViewModel(
            mockAuthenticationRepository,
            mock()
        )
        subject.oloToken = "oloAuthToken"

        // Use a coroutine-proof block:
        coroutineTestRule.dispatcher.runBlockingTest {

            // Request initial user data:
            subject.getUserData(authToken)

            // Check original profile data...
            subject.userProfile.asFlow().test {
                val newProfile = expectItem()

                // Data to (not) be updated...
                assertThat(newProfile.firstName, equalTo("OriginalFirst"))
                assertThat(newProfile.lastName, equalTo("OriginalLast"))
                assertThat(newProfile.phoneNumber, equalTo("(333) 333-4444"))

                // Rest of original data...
                assertThat(newProfile.id, equalTo(345))
                assertThat(newProfile.email, equalTo("an@email.com"))
                assertThat(newProfile.receiveMarketingEmails, equalTo(true))
                assertThat(newProfile.receiveSmsFromShakeShack, equalTo(false))
                assertThat(newProfile.genderSelection?.itemName, equalTo("Female"))
                assertThat(newProfile.kidsSelection?.itemName, equalTo("Nope"))
                assertThat(newProfile.petsSelection?.size, equalTo(2))
                assertThat(newProfile.petsSelection?.get(0)?.itemName, equalTo("Cat"))
                assertThat(newProfile.petsSelection?.get(1)?.itemName, equalTo("Fish"))
            }

            // Call method to test.
            // Note that these parameters are used for requests and method calls, but the ones
            // we would store at the end come from the response.
            subject.changePassword(newPassword = "requestPassword")

            // Verify method calls...
            verify(mockAuthenticationRepository)
                .updateUserName(
                    authHeader = authToken,
                    id = 345, // Using the id from the original user data we had
                    updateUserNameRequest
                )
            verify(mockAuthenticationRepository).updateOloUserContactDetail(
                eq("oloAuthToken"),
                anyString(),
                eq("0000000000")
            )

            // Check profile data was updated as expected...
            subject.userProfile.asFlow().test {
                val newProfile = expectItem()

                // Contact Information not updated...
                assertThat(newProfile.firstName, equalTo("OriginalFirst"))
                assertThat(newProfile.lastName, equalTo("OriginalLast"))
                assertThat(newProfile.phoneNumber, equalTo("(333) 333-4444"))

                // Rest of data unchanged...
                assertThat(newProfile.id, equalTo(345))
                assertThat(newProfile.email, equalTo("an@email.com"))
                assertThat(newProfile.receiveMarketingEmails, equalTo(true))
                assertThat(newProfile.receiveSmsFromShakeShack, equalTo(false))
                assertThat(newProfile.genderSelection?.itemName, equalTo("Female"))
                assertThat(newProfile.kidsSelection?.itemName, equalTo("Nope"))
                assertThat(newProfile.petsSelection?.size, equalTo(2))
                assertThat(newProfile.petsSelection?.get(0)?.itemName, equalTo("Cat"))
                assertThat(newProfile.petsSelection?.get(1)?.itemName, equalTo("Fish"))

                // No other updates to this LiveData:
                expectNoEvents()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `updateContactInformation() calls repository and updates profile if got a success response`() {

        // Note: Using `anyString()` to cover for `BuildConfig.OLO_KEY`.

        // Prepare test subject...

        val authToken = "authToken"

        //Responses for initial user data fetching...
        val originalUserDataResponse = buildUserData("OriginalFirst", "OriginalLast")
        val originalUserDataSuccessResult = Result.Success(originalUserDataResponse)
        val oloAuthRequest = OloAuthenticationRequest(
            "fuzz-sm",
            originalUserDataResponse.oloProviderToken,
            originalUserDataResponse.user!!.email
        )
        val oloAuthenticationResponse = buildOloAuthenticationResponse()
        val oloAuthenticationSuccessResult = Result.Success(oloAuthenticationResponse)
        val oloContactDetailsResponse = buildOloContactDetailsResponse()
        val oloContactDetailsSuccessResult = Result.Success(oloContactDetailsResponse)

        //Responses for updateContactInformation()...
        val updatedUserDataResponse = buildUserData("UpdatedFirst", "UpdatedLast")
        val updatedUserDataSuccessResult = Result.Success(updatedUserDataResponse)
        val updatedContactDetailResponse =
            OloUserContactDetailsResponse(contactDetails = "6669996666")
        val updatedContactDetailSuccessResult = Result.Success(updatedContactDetailResponse)

        val updateUserNameRequest = UpdateBasicUserDataRequest(
            firstName = "requestFirst",
            lastName = "requestLast",
        )
        val mockAuthenticationRepository = mock<AuthenticationRepository> {

            //Responses for initial user data fetching...
            onBlocking {
                getUserData("Bearer $authToken")
            } doReturn flowOf(originalUserDataSuccessResult)

            onBlocking {
                getOloToken(anyString(), eq(oloAuthRequest))
            } doReturn flowOf(oloAuthenticationSuccessResult)

            onBlocking {
                getOloUserContactDetails(eq("oloAuthToken"), anyString())
            } doReturn flowOf(oloContactDetailsSuccessResult)

            //Responses for updateContactInformation()...
            onBlocking {
                updateUserName("authToken", 345, updateUserNameRequest)
            } doReturn flowOf(updatedUserDataSuccessResult)
            onBlocking {
                updateOloUserContactDetail(anyString(), anyString(), anyString())
            } doReturn flowOf(updatedContactDetailSuccessResult)
        }

        val subject = AccountOverviewViewModel(
            mockAuthenticationRepository,
            mock()
        )
        subject.oloToken = "oloAuthToken"

        // Use a coroutine-proof block:
        coroutineTestRule.dispatcher.runBlockingTest {

            // Request initial user data:
            subject.getUserData(authToken)

            // Check original profile data...
            subject.userProfile.asFlow().test {
                val newProfile = expectItem()

                // Data to be updated...
                assertThat(newProfile.firstName, equalTo("OriginalFirst"))
                assertThat(newProfile.lastName, equalTo("OriginalLast"))
                assertThat(newProfile.phoneNumber, equalTo("(333) 333-4444"))

                // Rest of original data...
                assertThat(newProfile.id, equalTo(345))
                assertThat(newProfile.email, equalTo("an@email.com"))
                assertThat(newProfile.receiveMarketingEmails, equalTo(true))
                assertThat(newProfile.receiveSmsFromShakeShack, equalTo(false))
                assertThat(newProfile.genderSelection?.itemName, equalTo("Female"))
                assertThat(newProfile.kidsSelection?.itemName, equalTo("Nope"))
                assertThat(newProfile.petsSelection?.size, equalTo(2))
                assertThat(newProfile.petsSelection?.get(0)?.itemName, equalTo("Cat"))
                assertThat(newProfile.petsSelection?.get(1)?.itemName, equalTo("Fish"))
            }

            // Call method to test.
            // Note that these parameters are used for requests and method calls, but the ones
            // we would store at the end come from the response.
            subject.updateContactInformation(
                firstName = "requestFirst",
                lastName = "requestLast",
                phoneNumber = "0000000000"

            )

            // Verify method calls...
            verify(mockAuthenticationRepository)
                .updateUserName(
                    authHeader = authToken,
                    id = 345, // Using the id from the original user data we had
                    updateUserNameRequest
                )
            verify(mockAuthenticationRepository).updateOloUserContactDetail(
                eq("oloAuthToken"),
                anyString(),
                eq("0000000000")
            )

            // Check profile data was updated as expected...
            subject.userProfile.asFlow().test {
                val newProfile = expectItem()

                // Updated data...
                assertThat(newProfile.firstName, equalTo("UpdatedFirst"))
                assertThat(newProfile.lastName, equalTo("UpdatedLast"))
                assertThat(newProfile.phoneNumber, equalTo("(666) 999-6666"))

                // Rest of data unchanged...
                assertThat(newProfile.id, equalTo(345))
                assertThat(newProfile.email, equalTo("an@email.com"))
                assertThat(newProfile.receiveMarketingEmails, equalTo(true))
                assertThat(newProfile.receiveSmsFromShakeShack, equalTo(false))
                assertThat(newProfile.genderSelection?.itemName, equalTo("Female"))
                assertThat(newProfile.kidsSelection?.itemName, equalTo("Nope"))
                assertThat(newProfile.petsSelection?.size, equalTo(2))
                assertThat(newProfile.petsSelection?.get(0)?.itemName, equalTo("Cat"))
                assertThat(newProfile.petsSelection?.get(1)?.itemName, equalTo("Fish"))

                // No other updates to this LiveData:
                expectNoEvents()
            }
        }
    }

    @ExperimentalTime
    @Test
    fun `updateContactInformation() calls repository and does not update profile if got an error response`() {

        // Note: Using `anyString()` to cover for `BuildConfig.OLO_KEY`.

        // Prepare test subject...

        val authToken = "authToken"

        //Responses for initial user data fetching...
        val originalUserDataResponse = buildUserData("OriginalFirst", "OriginalLast")
        val originalUserDataSuccessResult = Result.Success(originalUserDataResponse)
        val oloAuthRequest = OloAuthenticationRequest(
            "fuzz-sm",
            originalUserDataResponse.oloProviderToken,
            originalUserDataResponse.user!!.email
        )
        val oloAuthenticationResponse = buildOloAuthenticationResponse()
        val oloAuthenticationSuccessResult = Result.Success(oloAuthenticationResponse)
        val oloContactDetailsResponse = buildOloContactDetailsResponse()
        val oloContactDetailsSuccessResult = Result.Success(oloContactDetailsResponse)

        //Responses for updateContactInformation()...
        val updatedUserDataErrorResult = Result.Error(Exception("Could not update user data"))
        val updatedContactDetailErrorResult =
            Result.Error(Exception("Could not update contact details"))

        val updateUserNameRequest = UpdateBasicUserDataRequest(
            firstName = "requestFirst",
            lastName = "requestLast",
        )
        val mockAuthenticationRepository = mock<AuthenticationRepository> {

            //Responses for initial user data fetching...
            onBlocking {
                getUserData("Bearer $authToken")
            } doReturn flowOf(originalUserDataSuccessResult)

            onBlocking {
                getOloToken(anyString(), eq(oloAuthRequest))
            } doReturn flowOf(oloAuthenticationSuccessResult)

            onBlocking {
                getOloUserContactDetails(eq("oloAuthToken"), anyString())
            } doReturn flowOf(oloContactDetailsSuccessResult)

            //Responses for updateContactInformation()...
            onBlocking {
                updateUserName("authToken", 345, updateUserNameRequest)
            } doReturn flowOf(updatedUserDataErrorResult)
            onBlocking {
                updateOloUserContactDetail(anyString(), anyString(), anyString())
            } doReturn flowOf(updatedContactDetailErrorResult)
        }

        val subject = AccountOverviewViewModel(
            mockAuthenticationRepository,
            mock()
        )
        subject.oloToken = "oloAuthToken"

        // Use a coroutine-proof block:
        coroutineTestRule.dispatcher.runBlockingTest {

            // Request initial user data:
            subject.getUserData(authToken)

            // Check original profile data...
            subject.userProfile.asFlow().test {
                val newProfile = expectItem()

                // Data to (not) be updated...
                assertThat(newProfile.firstName, equalTo("OriginalFirst"))
                assertThat(newProfile.lastName, equalTo("OriginalLast"))
                assertThat(newProfile.phoneNumber, equalTo("(333) 333-4444"))

                // Rest of original data...
                assertThat(newProfile.id, equalTo(345))
                assertThat(newProfile.email, equalTo("an@email.com"))
                assertThat(newProfile.receiveMarketingEmails, equalTo(true))
                assertThat(newProfile.receiveSmsFromShakeShack, equalTo(false))
                assertThat(newProfile.genderSelection?.itemName, equalTo("Female"))
                assertThat(newProfile.kidsSelection?.itemName, equalTo("Nope"))
                assertThat(newProfile.petsSelection?.size, equalTo(2))
                assertThat(newProfile.petsSelection?.get(0)?.itemName, equalTo("Cat"))
                assertThat(newProfile.petsSelection?.get(1)?.itemName, equalTo("Fish"))
            }

            // Call method to test.
            // Note that these parameters are used for requests and method calls, but the ones
            // we would store at the end come from the response.
            subject.updateContactInformation(
                firstName = "requestFirst",
                lastName = "requestLast",
                phoneNumber = "0000000000"

            )

            // Verify method calls...
            verify(mockAuthenticationRepository)
                .updateUserName(
                    authHeader = authToken,
                    id = 345, // Using the id from the original user data we had
                    updateUserNameRequest
                )
            verify(mockAuthenticationRepository).updateOloUserContactDetail(
                eq("oloAuthToken"),
                anyString(),
                eq("0000000000")
            )

            // Check profile data was updated as expected...
            subject.userProfile.asFlow().test {
                val newProfile = expectItem()

                // Contact Information not updated...
                assertThat(newProfile.firstName, equalTo("OriginalFirst"))
                assertThat(newProfile.lastName, equalTo("OriginalLast"))
                assertThat(newProfile.phoneNumber, equalTo("(333) 333-4444"))

                // Rest of data unchanged...
                assertThat(newProfile.id, equalTo(345))
                assertThat(newProfile.email, equalTo("an@email.com"))
                assertThat(newProfile.receiveMarketingEmails, equalTo(true))
                assertThat(newProfile.receiveSmsFromShakeShack, equalTo(false))
                assertThat(newProfile.genderSelection?.itemName, equalTo("Female"))
                assertThat(newProfile.kidsSelection?.itemName, equalTo("Nope"))
                assertThat(newProfile.petsSelection?.size, equalTo(2))
                assertThat(newProfile.petsSelection?.get(0)?.itemName, equalTo("Cat"))
                assertThat(newProfile.petsSelection?.get(1)?.itemName, equalTo("Fish"))

                // No other updates to this LiveData:
                expectNoEvents()
            }
        }
    }

    private fun buildUserData(firstName: String, lastName: String): UserData {
        return UserData(
            dob = "12/12/1980",
            encryptedMagicalImmutable = "o+V/r0VwG0/PJnlDEn+4+A==",
            favItemChainId = "32",
            gender = "Female",
            grantFlowType = "password",
            id = 345,
            isAreaManager = false,
            isStaff = false,
            isStoreManager = false,
            isSuperuser = false,
            isTeamMember = false,
            kids = "Nope",
            kiosk = "12",
            locations = null,
            loyaltyProviders = null,
            magicalImmutableIv = "",
            needsToEnterNewPassword = false,
            oloProviderToken = "oloProviderToken",
            optoutEmail = false,
            optoutMessages = true,
            pets = "Cat, Fish",
            referralCode = "",
            type = "",
            user = UserData.User("an@email.com", firstName, lastName, "an.username"),
            userprofileAllergens = listOf(
                UserData.ApiAllergen(1, "Soy"),
                UserData.ApiAllergen(3, "Egg"),
                UserData.ApiAllergen(6, "Peanut"),
            ),
            zip = "60612"
        )
    }

    private fun buildOloAuthenticationResponse(): OloAuthenticationResponse {
        return OloAuthenticationResponse(
            authorizationCode = null,
            authToken = "oloAuthToken",
            basketId = null,
            contactNumber = null,
            emailAddress = null,
            expiresIn = null,
            firstName = null,
            lastName = null,
            provider = null,
            providerToken = null,
            providerUserId = null,
            refreshToken = null
        )
    }

    private fun buildOloContactDetailsResponse(): OloUserContactDetailsResponse {
        return OloUserContactDetailsResponse(
            // US phone format not applied yet.
            // Should have been applied by the time this info reaches the user profile.
            contactDetails = "3333334444"
        )
    }
}