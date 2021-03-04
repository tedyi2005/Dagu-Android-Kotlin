package com.dagu.android.data.authentication

import com.dagu.android.BuildConfig
import com.dagu.android.data.authentication.api.AuthenticationApi
import com.dagu.android.data.authentication.api.OloUserAccountApi
import com.dagu.android.data.authentication.api.UserAccountApi
import com.dagu.android.data.authentication.model.*
import com.dagu.android.data.di.module.IoDispatcher
import com.dagu.android.data.repository.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import okhttp3.Credentials
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

@Singleton
@ExperimentalCoroutinesApi
class AuthenticationRepository @Inject constructor(
    private val authenticationApi: AuthenticationApi,
    private val userAccountApi: UserAccountApi,
    private val oloUserAccountApi: OloUserAccountApi,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {

    //region In-memory caches...
    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private var userAuthentication: UserAuthentication? = null
    private var userData: UserData? = null
    private var oloAuthenticationResponse: OloAuthenticationResponse? = null
    private var contactDetails: OloUserContactDetailsResponse? = null
    private var forgottenPasswordData: ResponseBody? = null
    //endregion


    private val _recentOrders = MutableStateFlow<Result<RecentOrderResponse>>(Result.Loading(true))
    val recentOrders: StateFlow<Result<RecentOrderResponse>> get() = _recentOrders

    private val basicAuthHeader: String by lazy {
        Credentials.basic(BuildConfig.SSMA_CLIENT_ID, BuildConfig.SSMA_CLIENT_SECRET)
    }

    fun logout() {
        userAuthentication = null
        userData = null
        oloAuthenticationResponse = null
        contactDetails = null
        // TODO add logout API call when available
    }

    suspend fun userProfileLookup(username: String): Flow<Result<UserProfileLookupResponse>> {
        return flow {
            emit(Result.Loading(true))

            val result = try {
                val response = userAccountApi.userProfileLookup(
                    basicAuthHeader,
                    UserProfileLookupRequest(email = username)
                )
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e)
            }

            emit(Result.Loading(false))

            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun createSSMAUser(
        username: String,
        password: String,
        firstName: String,
        lastName: String
    ): Flow<Result<UserData>> {
        return flow {
            emit(Result.Loading(true))
            val result = try {
                val ssmaUser = SSMAUser(firstName, lastName, username, username, password)
                val createSSMAUserRequest = CreateSSMAUserRequest(user = ssmaUser)

                val response = userAccountApi.createSSMAUser(authorization = basicAuthHeader, createSSMAUserBody =  createSSMAUserRequest)
                Result.Success(response)
            } catch (e: java.lang.Exception) {
                Result.Error(e)
            }
            emit(Result.Loading(false))
            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun login(
        grantType: String,
        username: String,
        password: String
    ): Flow<Result<UserAuthentication>> {

        return flow {
            emit(Result.Loading(true))
            val result = try {
                val response =
                    authenticationApi.getToken(basicAuthHeader, grantType, username, password)
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e)
            }
            emit(Result.Loading(false))

            if (result is Result.Success) {
                userAuthentication = result.data
            }

            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun refreshToken(
        grantType: String,
        refreshToken: String
    ): Flow<Result<UserAuthentication>> {
        return flow {
            emit(Result.Loading(true))
            val result = try {
                val response =
                    authenticationApi.getRefreshToken(basicAuthHeader, grantType, refreshToken)
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e)
            }
            emit(Result.Loading(false))

            if (result is Result.Success) {
                userAuthentication = result.data
            }

            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun updateUserName(
        authHeader: String,
        id: Int,
        updateBasicUserDataRequest: UpdateBasicUserDataRequest
    ): Flow<Result<UserData>> {
        return flow {
            emit(Result.Loading(true))
            val result = try {
                val updateBody = UpdateUserRequest(updateBasicUserDataRequest)
                val response =
                    userAccountApi.userProfileUpdate("Bearer $authHeader", id, updateBody)
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e)
            }
            emit(Result.Loading(false))

            // Update in-memory cache with updated data:
            if (result is Result.Success) {
                userData = result.data
            }

            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun getUserData(authHeader: String): Flow<Result<UserData>> {
        return flow {
            // Return cache first, if available:
            userData?.let {
                emit(Result.Success(it))
                return@flow
            }

            emit(Result.Loading(true))

            val result = try {
                val response = userAccountApi.getUserData(authHeader)
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e)
            }

            emit(Result.Loading(false))

            if (result is Result.Success) {
                userData = result.data
            }

            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun getOloToken(
        oloKey: String,
        oloAuthenticationRequest: OloAuthenticationRequest
    ): Flow<Result<OloAuthenticationResponse>> {
        return flow {
            // Return cache first, if available:
            oloAuthenticationResponse?.let {
                emit(Result.Success(it))
                return@flow
            }

            emit(Result.Loading(true))

            val result = try {
                val response =
                    oloUserAccountApi.getOrCreateOloUser(oloKey, oloAuthenticationRequest)
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e)
            }

            emit(Result.Loading(false))

            if (result is Result.Success) {
                oloAuthenticationResponse = result.data
            }
            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun updateOloUserContactDetail(
        oloAuthToken: String,
        oloKey: String,
        contactNumber: String
    ): Flow<Result<OloUserContactDetailsResponse>> {
        return flow {
            emit(Result.Loading(true))
            val result = try {
                val response = oloUserAccountApi.updateOloContactDetails(
                    oloAuthToken, oloKey,
                    UpdateOloContactDetailsRequest(contactNumber)
                )
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e)
            }

            emit(Result.Loading(false))

            // Update in-memory cache with updated data:
            if (result is Result.Success) {
                contactDetails = result.data
            }

            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun getOloUserContactDetails(
        oloAuthToken: String,
        oloKey: String
    ): Flow<Result<OloUserContactDetailsResponse>> {
        return flow {
            // Return cache first, if available:
            contactDetails?.let {
                emit(Result.Success(it))
                return@flow
            }

            emit(Result.Loading(true))

            val result = try {
                val response = oloUserAccountApi.getOloContactDetails(oloAuthToken, oloKey)
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e)
            }

            emit(Result.Loading(false))

            if (result is Result.Success) {
                contactDetails = result.data
            }
            emit(result)
        }.flowOn(dispatcher)
    }


    suspend fun requestPasswordRecovery(email: String): Flow<Result<ResponseBody>> {
        return flow {

            emit(Result.Loading(true))

            val result = try {
                val response = authenticationApi.requestPasswordRecovery(basicAuthHeader, email)

                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e)
            }

            emit(Result.Loading(false))

            if (result is Result.Success) {
                forgottenPasswordData = result.data
            }
            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun loginWithFacebook(accessToken: String
    ): Flow<Result<UserAuthentication>> {

        return flow {
            emit(Result.Loading(true))
            val result = try {
                val response =
                    authenticationApi.loginWithFacebook(basicAuthHeader, accessToken = accessToken)
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e)
            }
            emit(Result.Loading(false))

            if (result is Result.Success) {
                userAuthentication = result.data
            }

            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun googleSignIn(grantType: String, tokenId: String): Flow<Result<UserAuthentication>> {
        return flow {
            emit(Result.Loading(true))
            val result = try {
                val response = authenticationApi.googleSignIn(
                    authorization = basicAuthHeader,
                    grantType = grantType,
                    tokenId = tokenId
                )
                Result.Success(response)
            } catch (exception: Exception) {
                Result.Error(exception)
            }
            emit(Result.Loading(false))

            if (result is Result.Success) {
                userAuthentication = result.data
            }

            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun getOloUserRecentOrders(oloAuthToken: String, oloKey: String) {
        _recentOrders.value = Result.Loading(true)
        val result = oloUserAccountApi.getOloUserRecentOrders(oloAuthToken, oloKey)
        _recentOrders.value = Result.Loading(false)
        _recentOrders.value = Result.Success(result)
    }
}
