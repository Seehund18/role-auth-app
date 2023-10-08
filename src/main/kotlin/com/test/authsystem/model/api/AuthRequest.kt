package com.test.authsystem.model.api

import jakarta.validation.constraints.NotBlank

data class AuthRequest(@NotBlank val login: String,
                       val password: CharArray)