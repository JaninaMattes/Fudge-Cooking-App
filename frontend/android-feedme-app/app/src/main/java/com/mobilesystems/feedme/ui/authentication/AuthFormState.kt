package com.mobilesystems.feedme.ui.authentication

/**
 * Data validation state of the login form.
 */
data class AuthFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val confirmPasswordError: Int? = null,
    val emailError: Int? = null,
    val isDataValid: Boolean = false
)