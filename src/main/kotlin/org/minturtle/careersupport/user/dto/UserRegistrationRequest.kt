package org.minturtle.careersupport.user.dto

data class UserRegistrationRequest(
    val nickname: String,
    val username: String,
    val password: String
)