package com.dagu.android.presentation.authentication

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.gson.Gson
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.dagu.android.data.authentication.AccountPreferencesRepository
import com.dagu.android.data.authentication.AuthenticationRepository
import com.dagu.android.data.authentication.model.*
import com.dagu.android.data.di.module.MainDispatcher
import com.dagu.android.data.repository.Result
import com.dagu.android.data.repository.UIResult
import com.dagu.android.util.Constants
import com.dagu.android.util.getApiErrorMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException

@ExperimentalCoroutinesApi
class AuthenticationViewModel @ViewModelInject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val accountPreferencesRepository: AccountPreferencesRepository,
    @MainDispatcher private val dispatcher: CoroutineDispatcher
) :
    ViewModel() {
    private val _email = MutableLiveData("")
    val email: LiveData<String> get() = _email

    private val _password = MutableLiveData("")
    val password: LiveData<String> get() = _password

    private val _createUserResult = MutableLiveData<UIResult<UserData>>()
    val createUserResult: LiveData<UIResult<UserData>> get() = _createUserResult

    private val _loginResult = MutableLiveData<UIResult<UserAuthentication>>()
    val loginResult: LiveData<UIResult<UserAuthentication>> = _loginResult

    var currentPasswordLiveData = MutableLiveData<UIResult<Boolean>>()

    private val _googleLoginResult = MutableLiveData<UIResult<UserAuthentication>>()
    val googleLoginResult: LiveData<UIResult<UserAuthentication>> = _googleLoginResult

    private val _forgottenPasswordResult = MutableLiveData<UIResult<ResponseBody>>()
    val forgottenPasswordResult: LiveData<UIResult<ResponseBody>> = _forgottenPasswordResult

    private val _facebookLoginResult = MutableLiveData<UIResult<UserAuthentication>>()
    val facebookLoginResult: LiveData<UIResult<UserAuthentication>> = _facebookLoginResult

    private val _authToken = MutableLiveData<String>()
    val authToken: LiveData<String?>
        get() = _authToken

    private val _userData = MutableLiveData<UIResult<UserData>>()
    val userData: LiveData<UIResult<UserData>> get() = _userData

    private val _oloUserData = MutableLiveData<UIResult<OloAuthenticationResponse>>()
    val oloUserData: LiveData<UIResult<OloAuthenticationResponse>> get() = _oloUserData

    private val _oloUserContactData = MutableLiveData<UIResult<OloUserContactDetailsResponse>>()
    val oloUserContactData: LiveData<UIResult<OloUserContactDetailsResponse>> get() = _oloUserContactData

    fun setLoginData(email: String, password: String) {
        _email.value = email
        _password.value = password
    }

    fun setAuthTokenFromAccountManager(authToken: String) {
        viewModelScope.launch(dispatcher) {
            accountPreferencesRepository.updateAuthToken(authToken)
            _authToken.value = authToken
        }
    }

    fun setRefreshTokenFromAccountManager(refreshToken: String) {
        viewModelScope.launch(dispatcher) {
            accountPreferencesRepository.updateRefreshToken(refreshToken)
        }
    }

    fun removeUserDataFromAccountManager() {
        viewModelScope.launch(dispatcher) {
            accountPreferencesRepository.updateAuthToken("")
            accountPreferencesRepository.updateRefreshToken("")
            accountPreferencesRepository.updateOloProviderToken("")
            accountPreferencesRepository.updateOloAuthTokem("")

            _authToken.value = ""
            _userData.value = null
            _oloUserData.value = null
            _oloUserContactData.value = null

            authenticationRepository.logout()

            // Logout from Facebook if we're signing out and have a valid access token
            if (AccessToken.isCurrentAccessTokenActive()) {
                LoginManager.getInstance().logOut()
            }
        }
    }

    fun getUserData(authToken: String) {
        viewModelScope.launch(dispatcher) {
            authenticationRepository.getUserData("Bearer $authToken")
                .catch {
                    _userData.value = UIResult.Error("Error fetching user data: ${it.message}")
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            result.data.oloProviderToken?.let {
                                accountPreferencesRepository.updateOloProviderToken(it)
                            }
                            _userData.value = UIResult.Success(result.data)
                        }
                        is Result.Loading -> {
                            _userData.value = UIResult.Loading(result.loading)
                        }
                        is Result.Error -> {
                            _userData.value =
                                UIResult.Error("Error fetching user data: ${result.exception.message}")
                        }
                    }
                }
        }
    }

    fun getOloAuthToken(oloProviderToken: String, oloKey: String, userId: String) {
        val oloAuthRequest = OloAuthenticationRequest("fuzz-sm", oloProviderToken, userId)

        viewModelScope.launch(dispatcher) {
            authenticationRepository.getOloToken(oloKey, oloAuthRequest)
                .catch {
                    _oloUserData.value =
                        UIResult.Error("Error fetching olo user data: ${it.message}")
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            result.data.authToken?.let {
                                accountPreferencesRepository.updateOloAuthTokem(it)
                            }
                            _oloUserData.value = UIResult.Success(result.data)
                        }
                        is Result.Loading -> {
                            _oloUserData.value = UIResult.Loading(result.loading)
                        }
                        is Result.Error -> {
                            _oloUserData.value =
                                UIResult.Error("Error fetching olo user data: ${result.exception.message}")
                        }
                    }
                }
        }
    }

    fun getOloUserData(oloAuthToken: String, oloKey: String) {
        viewModelScope.launch(dispatcher) {
            authenticationRepository.getOloUserContactDetails(oloAuthToken, oloKey)
                .catch {
                    _oloUserContactData.value =
                        UIResult.Error("Error fetching olo user data: ${it.message}")
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _oloUserContactData.value = UIResult.Success(result.data)
                        }
                        is Result.Loading -> {
                            _oloUserContactData.value = UIResult.Loading(result.loading)
                        }
                        is Result.Error -> {
                            _oloUserContactData.value =
                                UIResult.Error("Error fetching olo user data: ${result.exception.message}")
                        }
                    }
                }
        }

    }

    fun updateOloUserData(oloAuthToken: String, oloKey: String, phoneNumber: String) {
        val rawPhoneNumber = PhoneNumberUtil.normalizeDigitsOnly(phoneNumber)

        viewModelScope.launch(dispatcher) {
            authenticationRepository.updateOloUserContactDetail(
                oloAuthToken,
                oloKey,
                rawPhoneNumber
            )
                .catch {
                    _oloUserContactData.value =
                        UIResult.Error("Error updating olo user data: ${it.message}")
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _oloUserContactData.value = UIResult.Success(result.data)
                        }
                        is Result.Loading -> {
                            _oloUserContactData.value = UIResult.Loading(result.loading)
                        }
                        is Result.Error -> {
                            _oloUserContactData.value =
                                UIResult.Error("Error updating olo user data: ${result.exception.message}")
                        }
                    }
                }
        }
    }

    @ExperimentalCoroutinesApi
    fun login(username: String, password: String) {
        setLoginData(username, password)
        viewModelScope.launch(dispatcher) {
            authenticationRepository.login("password", username, password)
                .onStart { /* Loading state */ }
                .catch { exception ->
                    _loginResult.value =
                        UIResult.Error("Login failed: " + exception.message)
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            accountPreferencesRepository.updateRefreshToken(result.data.refreshToken)
                            _loginResult.value = UIResult.Success(result.data)
                            currentPasswordLiveData.postValue(UIResult.Success(true))

                        }
                        is Result.Loading -> {
                            _loginResult.value = UIResult.Loading(result.loading)
                        }
                        is Result.Error -> {
                            val invalidCredentials =
                                result.exception is HttpException && result.exception.code() == 401
                            if (invalidCredentials) {
                                _loginResult.value =
                                    UIResult.Error("The password and email address do not match.")
                            } else {
                                _loginResult.value =
                                    UIResult.Error("Login failed: " + result.exception.message)
                            }
                        }
                    }
                }
        }
    }

    fun googleSignIn(grantType: String, tokenId: String) {
        viewModelScope.launch(dispatcher) {
            authenticationRepository.googleSignIn(grantType, tokenId)
                .catch {
                    _googleLoginResult.value =
                        UIResult.Error("Error fetching user data: ${it.message}")
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            accountPreferencesRepository.updateRefreshToken(result.data.refreshToken)
                            _googleLoginResult.value = UIResult.Success(result.data)
                        }
                        is Result.Loading -> {
                            _googleLoginResult.value = UIResult.Loading(result.loading)
                        }
                        is Result.Error -> {
                            _googleLoginResult.value =
                                UIResult.Error(result.exception.getApiErrorMessage())
                        }
                    }
                }
        }
    }


    fun createAccount(username: String, firstName: String, lastName: String, password: String) {
        viewModelScope.launch(dispatcher) {
            authenticationRepository.createSSMAUser(username, password, firstName, lastName)
                .catch { exception ->
                    _createUserResult.value =
                        UIResult.Error("Create SSMA user failed: " + exception.message)
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            result.data.oloProviderToken?.let {
                                accountPreferencesRepository.updateOloProviderToken(it)
                            }
                            _createUserResult.value =
                                UIResult.Success(result.data)
                        }
                        is Result.Loading -> {
                            _createUserResult.value = UIResult.Loading(result.loading)
                        }
                        is Result.Error -> {
                            if (result.exception is HttpException) {
                                try {

                                    val userError =
                                        result.exception.response()?.errorBody()?.string()

                                    val errorObject: CreateSSMAUserError =
                                        Gson().fromJson(userError, CreateSSMAUserError::class.java)

                                    if (errorObject.error.userError.userName.isNotEmpty()) {
                                        _createUserResult.value =
                                            UIResult.Error(errorObject.error.userError.userName[0])
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                _createUserResult.value =
                                    UIResult.Error("Create SSMA user failed: " + result.exception.message)
                            }
                        }
                    }
                }
        }
    }

    @ExperimentalCoroutinesApi
    fun requestPasswordRecovery(email: String) {
        viewModelScope.launch(dispatcher) {
            authenticationRepository.requestPasswordRecovery(email)
                .catch { exception ->
                    _forgottenPasswordResult.value =
                        UIResult.Error("Reset password failed :  " + exception.message)
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _forgottenPasswordResult.value = UIResult.Success(result.data)
                        }
                        is Result.Loading -> {
                            _forgottenPasswordResult.value = UIResult.Loading(result.loading)
                        }
                        is Result.Error -> {
                            // For this request, error code 422 means that the provided email does
                            // not have an account. For security reasons we don't want users to have
                            // ways to tell if an email has an account or not, so handle 422 as
                            // a successful result (Users will see "Email sent").
                            if (result.exception is HttpException && result.exception.code() == Constants.ERROR_CODE_422)
                                _forgottenPasswordResult.value =
                                    UIResult.Success(ResponseBody.create(MediaType.parse(""), ""))
                            else
                                _forgottenPasswordResult.value =
                                    UIResult.Error("Reset password failed :  " + result.exception.message)
                        }
                    }
                }
        }
    }

    fun loginWithFacebook(accessToken: String) {
        viewModelScope.launch(dispatcher) {
            authenticationRepository.loginWithFacebook(accessToken)
                .catch { exception ->
                    _facebookLoginResult.value =
                        UIResult.Error("Login with Facebook failed: ${exception.message}")
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _facebookLoginResult.value = UIResult.Success(result.data)
                        }
                        is Result.Loading -> {
                            _facebookLoginResult.value = UIResult.Loading(result.loading)
                        }
                        is Result.Error -> {
                            if (result.exception is HttpException && result.exception.code() == 403) {
                                try {
                                    val errorString = result.exception.getApiErrorMessage()

                                    if (errorString.isNotEmpty()) {
                                        _facebookLoginResult.value =
                                            UIResult.Error("Login with Facebook failed: $errorString")
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                _facebookLoginResult.value =
                                    UIResult.Error("Login with Facebook failed: ${result.exception.message}")
                            }
                        }
                    }
                }
        }

    }
}
