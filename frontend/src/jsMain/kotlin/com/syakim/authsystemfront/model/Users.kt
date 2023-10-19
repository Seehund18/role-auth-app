package io.realworld.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(val user: User)

@Serializable
data class User(
    val email: String? = null,
    val token: String? = null,
    val login: String? = null,
    val password: String? = null,
    val birthday: String? = null
)

@Serializable
data class AuthResponse(
    val status: String,
    val jwtToken: String,
    val expirationDate: String
)
