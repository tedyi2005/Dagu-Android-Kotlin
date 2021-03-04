package com.dagu.android.presentation.authentication

/**
 * Data validation state of the login form.
 */
class LoginFormState(
    val generalError: Int? = null, // i.e: Email and password do not match
    val usernameError: Int? = null, // i.e: Wrong email format
    val passwordError: Int? = null, // i.e: Empty password
    val isDataValid: Boolean = false
)
