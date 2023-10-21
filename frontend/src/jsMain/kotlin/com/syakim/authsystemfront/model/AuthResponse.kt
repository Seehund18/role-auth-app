package com.syakim.authsystemfront.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val status: String,
    val jwtToken: String,
    val expirationDate: String
)