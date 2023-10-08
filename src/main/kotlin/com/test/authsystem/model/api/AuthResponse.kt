package com.test.authsystem.model.api

import java.time.LocalDate

data class AuthResponse(val status: String,
                        val jwtToken: String,
                        val expirationDate: LocalDate)