package com.test.authsystem.model.api

import java.time.LocalDateTime

data class AuthResponse(val status: String,
                        val jwtToken: String,
                        val expirationDate: LocalDateTime
)