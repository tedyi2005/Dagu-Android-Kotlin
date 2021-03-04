package com.dagu.android.presentation.account.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.dagu.android.BuildConfig
import com.dagu.android.data.authentication.AccountPreferencesRepository
import com.dagu.android.data.authentication.AuthenticationRepository
import com.dagu.android.data.authentication.model.*
import com.dagu.android.data.payment.PaymentMethod
import com.dagu.android.data.pdp.SingleViewItem
import com.dagu.android.data.repository.Result
import com.dagu.android.data.repository.UIResult
import com.dagu.android.data.user.UserAddress
import com.dagu.android.data.user.UserProfile
import com.dagu.android.util.JsonExamples
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class AccountOverviewViewModel @ViewModelInject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val accountPreferencesRepository: AccountPreferencesRepository
) : ViewModel() {

    private val _recentOrders = MutableLiveData<Result<RecentOrderResponse>>()
    val recentOrders: LiveData<Result<RecentOrderResponse>>
        get() = _recentOrders

    private val _currentOrder = MutableLiveData<Orders>()
    val currentOrder: LiveData<Orders>
        get() = _currentOrder

    private val _serverError = MutableLiveData("")
    val serverError: LiveData<String> get() = _serverError

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile>
        get() = _userProfile

    private val _paymentMethods = MutableLiveData<List<PaymentMethod>>()
    val paymentMethods: LiveData<List<PaymentMethod>>
        get() = _paymentMethods

    private val _addresses = MutableLiveData<List<UserAddress>>()
    val addresses: LiveData<List<UserAddress>> get() = _addresses

    private val _allergens = MutableLiveData<List<SingleViewItem>>()
    val allergens: LiveData<List<SingleViewItem>> get() = _allergens

    private lateinit var genderOptionList: ArrayList<SingleViewItem>
    private lateinit var kidsOptionList: ArrayList<SingleViewItem>
    private lateinit var petsOptionList: ArrayList<SingleViewItem>

    var oloToken: String? = null
    var authToken: String? = null
    var updatePhoneNumberLiveData = MutableLiveData<UIResult<Boolean>>()
    var updateNameLiveData = MutableLiveData<UIResult<Boolean>>()
    val contactInformationUpdateResult: LiveData<UIResult<Boolean>>
        get() {
            val savedResult = MediatorLiveData<UIResult<Boolean>>()
            savedResult.addSource(updatePhoneNumberLiveData) {
                savedResult.value = combineContactInformationUpdateResults()
            }

            savedResult.addSource(updateNameLiveData) {
                savedResult.value = combineContactInformationUpdateResults()
            }
            return savedResult
        }


    init {
        // TODO Replace this static data with proper payment methods once we figure out how those
        //      will come from the server.
        val newPaymentMethods = listOf(
            PaymentMethod("Amex", "3322", default = true, expired = false),
            PaymentMethod("Personal", "1234", default = false, expired = true),
            PaymentMethod("Contactless", "6666", default = false, expired = false)
        )
        _paymentMethods.value = newPaymentMethods

        initOptionLists()
    }

    private fun initOptionLists() {
        val listType = object : TypeToken<List<SingleViewItem>>() {}.type

        val genderData = JsonExamples.GENDER
        val kidsData = JsonExamples.KIDS
        val petsData = JsonExamples.PETS
        genderOptionList = Gson().fromJson(genderData, listType)
        kidsOptionList = Gson().fromJson(kidsData, listType)
        petsOptionList = Gson().fromJson(petsData, listType)
    }

    /**
     * User information requests flow:
     * Step 1) Request user data with our regular token, this response should also contain info we need
     * to request Olo user.
     * Step 2) If user data has what we need, use that to now request Olo user.
     * Step 3) Once Olo user is acquired, use Olo auth token it contains, to now request Olo user
     * contact data (That contains the user phone, for instance).
     */
    // Step 1:
    fun getUserData(authToken: String) {
        this.authToken = authToken
        viewModelScope.launch {
            authenticationRepository.getUserData("Bearer $authToken")
                .catch {
                    // TODO Handle errors
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            // Parse available information for each section...
                            _addresses.value = result.data.zip.toAddressesList()
                            _allergens.value = result.data.userprofileAllergens?.toAllergensList()
                            _userProfile.value = _userProfile.value.updateWithUserData(result.data)

                            // Go on requesting Olo auth token:
                            if (result.data.oloProviderToken != null &&
                                result.data.user?.email != null
                            ) {
                                getOloAuthToken(
                                    result.data.oloProviderToken,
                                    result.data.user.email
                                )
                            }
                        }
                        is Result.Loading -> {
                        }
                        is Result.Error -> {
                            // TODO Handle errors
                        }
                    }
                }
        }
    }

    // Step 2:
    private fun getOloAuthToken(oloProviderToken: String, userId: String) {
        val oloAuthRequest = OloAuthenticationRequest("fuzz-sm", oloProviderToken, userId)
        viewModelScope.launch {
            authenticationRepository.getOloToken(BuildConfig.OLO_KEY, oloAuthRequest)
                .catch {
                    // TODO Handle errors
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            // Go on requesting Olo contact details:
                            result.data.authToken?.let {
                                accountPreferencesRepository.updateOloAuthTokem(it)
                                getOloUserContactDetails(it)
                            }
                        }
                        is Result.Loading -> {
                        }
                        is Result.Error -> {
                            // TODO Handle errors
                        }
                    }
                }
        }
    }

    // Step 3:
    private fun getOloUserContactDetails(oloAuthToken: String) {
        viewModelScope.launch {
            authenticationRepository.getOloUserContactDetails(oloAuthToken, BuildConfig.OLO_KEY)
                .catch {
                    // TODO Handle errors
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _userProfile.value =
                                _userProfile.value.updateWithOloContactDetails(result.data)
                        }
                        is Result.Loading -> {
                        }
                        is Result.Error -> {
                            // TODO Handle errors
                        }
                    }
                }
        }
    }

    fun onBirthdayEpochValueSelected(epochValue: Long) {
        // TODO Handle new birthday value
    }

    fun updateContactInformation(firstName: String, lastName: String, phoneNumber: String) {
        viewModelScope.launch {
            updateUserPhoneNumber(phoneNumber)
            updateUserName(firstName, lastName)
        }
    }

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            updatePassword(newPassword)
        }
    }

    private suspend fun updatePassword(newPassword: String) {
        if (authToken != null && userProfile.value?.id != null) {
            authenticationRepository.updateUserName(
                authToken!!,
                userProfile.value?.id!!,
                UpdateBasicUserDataRequest(newPassword = newPassword)
            ).catch {
                updateNameLiveData.postValue(UIResult.Error("Error updating user data: $it"))
            }.collect { result ->
                when (result) {
                    is Result.Success -> {
                        val currentUserProfile = userProfile.value.updateWithUserData(result.data)
                        _userProfile.postValue(currentUserProfile)
                        updateNameLiveData.postValue(UIResult.Success(true))
                    }
                    is Result.Loading -> {
                    }
                    is Result.Error -> {
                        updateNameLiveData.postValue(UIResult.Error("Error updating user data: ${result.exception}"))
                    }
                }
            }
        } else {
            updateNameLiveData.postValue(UIResult.Error("ID or Auth token is null"))
        }
    }

    // TODO Create proper addresses list once we figure out how those will come from the server.
    //      For now well create a fake address from the only zip code we can get.
    private fun String?.toAddressesList(): List<UserAddress> {
        val addressesList = arrayListOf<UserAddress>()
        this?.let { addressesList.add(UserAddress(name = "Zip Code", firstLine = this)) }
        return addressesList
    }

    // TODO Mark allergens as selected once we figure out how that selection comes from the server.
    //      For now all allergens we can get will appear as not selected.
    private fun List<UserData.ApiAllergen?>?.toAllergensList(): List<SingleViewItem> {
        return this?.map {
            if (it?.id != null && it.name != null) {
                SingleViewItem(
                    id = it.id,
                    itemName = it.name,
                    itemValue = null,
                    isValueVisible = false,
                )
            } else {
                SingleViewItem(id = null, itemName = null, itemValue = null, isValueVisible = false)
            }
        } ?: emptyList()
    }

    private fun UserProfile?.updateWithUserData(data: UserData): UserProfile {
        // TODO Validate gender, kids and pets with real API values when possible...
        return UserProfile(
            id = data.id ?: this?.id,
            firstName = data.user?.firstName ?: this?.firstName,
            lastName = data.user?.lastName ?: this?.lastName,
            email = data.user?.email ?: this?.email,
            receiveSmsFromShakeShack =
            data.optoutMessages?.let { !it } ?: this?.receiveSmsFromShakeShack,
            receiveMarketingEmails =
            data.optoutEmail?.let { !it } ?: this?.receiveMarketingEmails,
            genderSelection = if (data.gender != null) {
                data.gender.toGenderSelection()
            } else {
                this?.genderSelection
            },
            kidsSelection = if (data.kids != null) {
                data.kids.toKidsSelection()
            } else {
                this?.kidsSelection
            },
            petsSelection = if (data.pets != null) {
                data.pets.toPetsSelection()
            } else {
                this?.petsSelection
            },
            phoneNumber = this?.phoneNumber, // Comes from a different request
            birthday = null // TODO Figure out birthday from API
        )
    }

    private fun UserProfile?.updateWithOloContactDetails(
        contactDetailsResponse: OloUserContactDetailsResponse
    ): UserProfile {
        val phoneNumber = try {
            contactDetailsResponse.contactDetails.toUsPhoneFormat()
                ?: this?.phoneNumber
        } catch (e: Exception) {
            e.printStackTrace()
            "" // return empty string if an error occurs
        }

        return UserProfile(
            phoneNumber = phoneNumber,

            // Everything else comes from a different request...
            id = this?.id,
            firstName = this?.firstName,
            lastName = this?.lastName,
            email = this?.email,
            receiveSmsFromShakeShack = this?.receiveSmsFromShakeShack,
            receiveMarketingEmails = this?.receiveMarketingEmails,
            genderSelection = this?.genderSelection,
            kidsSelection = this?.kidsSelection,
            petsSelection = this?.petsSelection,
            birthday = null // TODO Figure out birthday from API
        )
    }

    private fun Any.toGenderSelection(): SingleViewItem? {
        genderOptionList.forEach { gender ->
            gender.itemName?.let { genderName ->
                if (genderName == this) {
                    return gender
                }
            }
        }
        return null
    }

    private fun Any.toKidsSelection(): SingleViewItem? {
        kidsOptionList.forEach { option ->
            option.itemName?.let { optionName ->
                if (optionName == this) {
                    return option
                }
            }
        }
        return null
    }

    private fun Any.toPetsSelection(): List<SingleViewItem>? {
        val selectionList = arrayListOf<SingleViewItem>()
        petsOptionList.forEach { option ->
            option.itemName?.let { optionName ->
                if (this.toString().contains(optionName)) {
                    selectionList.add(option)
                }
            }
        }
        return selectionList
    }

    private fun String?.toUsPhoneFormat(): String? {
        return if (this == null) {
            null
        } else {
            val pnu = PhoneNumberUtil.getInstance()
            val pn = pnu.parse(this, "US")
            pnu.format(pn, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
        }
    }

    // Method to check if both network calls (user data and contact details) have been successful,
    // and return an UIResult for the combined operation.
    private fun combineContactInformationUpdateResults(): UIResult<Boolean> {
        val phoneNumberResult = updatePhoneNumberLiveData.value
        val nameResult = updateNameLiveData.value
        var infoHasBeenUpdated = false

        if (phoneNumberResult == null || nameResult == null) {
            return UIResult.Loading(true)
        }
        if (phoneNumberResult is UIResult.Success) {
            infoHasBeenUpdated = phoneNumberResult.data
        } else if (phoneNumberResult is UIResult.Error) {
            return phoneNumberResult
        }

        if (nameResult is UIResult.Success) {
            infoHasBeenUpdated = nameResult.data
        } else if (nameResult is UIResult.Error) {
            return nameResult
        }
        return UIResult.Success(infoHasBeenUpdated)
    }

    private suspend fun updateUserPhoneNumber(phoneNumber: String) {
        if (phoneNumber != userProfile.value?.phoneNumber) {
            authenticationRepository.updateOloUserContactDetail(
                "$oloToken", BuildConfig.OLO_KEY, phoneNumber
            ).catch {
                updatePhoneNumberLiveData.postValue(UIResult.Error("Error updating phone number: $it"))
            }.collect { result ->
                when (result) {
                    is Result.Success -> {
                        val currentUserProfile =
                            userProfile.value.updateWithOloContactDetails(result.data)
                        _userProfile.postValue(currentUserProfile)
                        updatePhoneNumberLiveData.postValue(UIResult.Success(true))
                    }
                    is Result.Loading -> {
                    }
                    is Result.Error -> {
                        updatePhoneNumberLiveData.postValue(UIResult.Error("Error updating phone number: ${result.exception}"))
                    }
                }
            }

        } else {
            updatePhoneNumberLiveData.postValue(UIResult.Success(true))
        }
    }

    private suspend fun updateUserName(firstName: String, lastName: String) {
        if (firstName != userProfile.value?.firstName || lastName != userProfile.value?.lastName
        ) {
            if (authToken != null && userProfile.value?.id != null) {
                val updateBasicUserRequest = UpdateBasicUserDataRequest(
                    firstName = firstName,
                    lastName = lastName
                )
                authenticationRepository.updateUserName(
                    authToken!!,
                    userProfile.value?.id!!,
                    updateBasicUserRequest
                ).catch {
                    updateNameLiveData.postValue(UIResult.Error("Error updating user data: $it"))
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val currentUserProfile =
                                userProfile.value.updateWithUserData(result.data)
                            _userProfile.postValue(currentUserProfile)
                            updateNameLiveData.postValue(UIResult.Success(true))
                        }
                        is Result.Loading -> {
                        }
                        is Result.Error -> {
                            updateNameLiveData.postValue(UIResult.Error("Error updating user data: ${result.exception}"))
                        }
                    }
                }
            } else {
                updateNameLiveData.postValue(UIResult.Error("ID or Auth token is null"))
            }

        } else {
            updateNameLiveData.postValue(UIResult.Success(true))
        }
    }

    fun getOloUserRecentOrders(oloAuthToken: String) {
        viewModelScope.launch {
            try {
                authenticationRepository.getOloUserRecentOrders(oloAuthToken, BuildConfig.OLO_KEY)
                _recentOrders.value = authenticationRepository.recentOrders.value
            } catch (e: Exception) {
                _serverError.value = "An error occurred while fetching recent orders: ${e.message}"
            }
        }
    }

    fun setUserProfileFromUserData(userData: UserData) {
        _userProfile.value = _userProfile.value.updateWithUserData(userData)
    }

    fun setUserProfileFromOloUserContactDetails(oloUserContactDetails: OloUserContactDetailsResponse) {
        _userProfile.value = _userProfile.value.updateWithOloContactDetails(oloUserContactDetails)
    }

    fun clearUserProfileData() {
        _userProfile.value = null
        _recentOrders.value = null
        _paymentMethods.value = null
        _allergens.value = null
        _addresses.value = null

        // TODO clearData from repository
    }

    fun onReorder(order: Orders) {
        _currentOrder.value = order
    }
}
