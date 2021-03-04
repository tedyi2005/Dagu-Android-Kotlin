package com.dagu.android.presentation.authentication

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.dagu.android.data.authentication.AccountPreferencesRepository
import com.dagu.android.data.authentication.AuthenticationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.regex.Pattern

@ExperimentalCoroutinesApi
class CreateAccountViewModel @ViewModelInject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val accountPreferencesRepository: AccountPreferencesRepository
) : ViewModel() {
    private val _email = MutableLiveData("")
    private val _firstName = MutableLiveData("")
    private val _lastName = MutableLiveData("")
    private val _phoneNumber = MutableLiveData("")
    private val _password = MutableLiveData("")
    private val _confirmPassword = MutableLiveData("")
    private val _receiveMktEmails = MutableLiveData(false)
    private val _currentPassword = MutableLiveData("")

    private var _digitRuleValidation = MutableLiveData(false)
    private var _lowerCaseRuleValidation = MutableLiveData(false)
    private var _upperCaseValidation = MutableLiveData(false)
    private var _specialCharValidation = MutableLiveData(false)
    private var _lengthValidation = MutableLiveData(false)
    private var _confirmPasswordValidation = MutableLiveData(false)

    val digitRuleValidation: LiveData<Boolean> get() = _digitRuleValidation
    val lowerCaseRuleValidation: LiveData<Boolean> get() = _lowerCaseRuleValidation
    val upperCaseRuleValidation: LiveData<Boolean> get() = _upperCaseValidation
    val specialCharValidation: LiveData<Boolean> get() = _specialCharValidation
    val lengthValidation: LiveData<Boolean> get() = _lengthValidation
    val confirmPasswordValidation: LiveData<Boolean> get() = _confirmPasswordValidation

    private var _isValidEmail = MutableLiveData(false)
    private var _isValidFirstName = MutableLiveData(false)
    private var _isValidLastName = MutableLiveData(false)
    private var _isValidPhoneNumber = MutableLiveData(false)
    private var _isValidPassword = MutableLiveData(false)
    private var _isValidCurrentPassword = MutableLiveData(false)
    private var _isValidForm = MutableLiveData(false)
    private var _isValidChangPasswordForm = MutableLiveData(false)

    val isValidEmail: LiveData<Boolean> get() = _isValidEmail
    val isValidFirstName: LiveData<Boolean> get() = _isValidFirstName
    val isValidLastName: LiveData<Boolean> get() = _isValidLastName
    val isValidPhoneNumber: LiveData<Boolean> get() = _isValidPhoneNumber
    val isValidPassword: LiveData<Boolean> get() = _isValidPassword
    val isValidForm: LiveData<Boolean> get() = _isValidForm
    val isValidChangPasswordForm: LiveData<Boolean> get() = _isValidChangPasswordForm


    fun setEmail(email: String) {
        _email.value = email
    }

    fun setFirstName(firstName: String) {
        _firstName.value = firstName
    }

    fun setLastName(lastName: String) {
        _lastName.value = lastName
    }

    fun setPhoneNumber(phoneNumber: String) {
        _phoneNumber.value = phoneNumber
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun setCurrentPassword(currentPassword: String) {
        _currentPassword.value = currentPassword
    }

    fun setConfirmPassword(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
    }

    fun setReceiveMktEmails(receiveMktEmails: Boolean) {
        _receiveMktEmails.value = receiveMktEmails
    }

    private fun validateEmail(email: String) {
        val emailRule = Pattern.compile("^[\\w-.]+@([\\w-]+.)+[\\w-]{2,4}\$")

        _isValidEmail.value = emailRule.matcher(email).matches()
    }

    private fun validateFirstName(firstName: String) {
        _isValidFirstName.value = firstName.length >= 2
    }

    private fun validateLastName(lastName: String) {
        _isValidLastName.value = lastName.length >= 2
    }

    private fun validatePhoneNumber(phoneNumber: String) {
        // TODO check if we need a regex to check a valid US phone number
        val rawPhoneNumber = PhoneNumberUtil.normalizeDigitsOnly(phoneNumber)
        _isValidPhoneNumber.value = rawPhoneNumber.length == 10
    }

    private fun validateCurrentPassword(currentPassword : String) {
        _isValidCurrentPassword.value = currentPassword.isNotEmpty()
    }

    private fun validatePasswordRules(password: String, confirmPassword: String) {
        val digitRule = Pattern.compile("\\d+.*|.*\\d+|.*\\d+.*")
        val lowerCaseRule = Pattern.compile("^.*[a-z].*\$")
        val upperCaseRule = Pattern.compile("^.*[A-Z].*\$")
        val specialCharRule = Pattern.compile(".*[~!@#\$%^&*_+=`|(){}\\[:;\"'<>,.?\\]/\\s-].*")
        val lengthRule = Pattern.compile("^.{8,}$")

        _digitRuleValidation.value = digitRule.matcher(password).matches()
        _lowerCaseRuleValidation.value = lowerCaseRule.matcher(password).matches()
        _upperCaseValidation.value = upperCaseRule.matcher(password).matches()
        _specialCharValidation.value = specialCharRule.matcher(password).matches()
        _lengthValidation.value = lengthRule.matcher(password).matches()
        _confirmPasswordValidation.value =
            password == confirmPassword && password.isNotEmpty() && confirmPassword.isNotEmpty()

        _isValidPassword.value =
            _digitRuleValidation.value == true
                    && _lowerCaseRuleValidation.value == true
                    && _upperCaseValidation.value == true
                    && _specialCharValidation.value == true
                    && _lengthValidation.value == true
                    && _confirmPasswordValidation.value == true
    }

    fun validateForm() {
        validateEmail(_email.value.toString())
        validateFirstName(_firstName.value.toString())
        validateLastName(_lastName.value.toString())
        validatePhoneNumber(_phoneNumber.value.toString())
        validatePasswordRules(_password.value.toString(), _confirmPassword.value.toString())
        _isValidForm.value = _isValidEmail.value == true
                && _isValidFirstName.value == true
                && _isValidLastName.value == true
                && _isValidPhoneNumber.value == true
                && _isValidPassword.value == true
    }

    fun validateChangePasswordForm() {
        validatePasswordRules(_password.value.toString(), _confirmPassword.value.toString())
        validateCurrentPassword(_currentPassword.value.toString())
        _isValidChangPasswordForm.value = _isValidCurrentPassword.value == true
                && _isValidPassword.value == true
    }
}