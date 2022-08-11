package com.mobilesystems.feedme.domain.model

data class LoggedInUser(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val email: String)