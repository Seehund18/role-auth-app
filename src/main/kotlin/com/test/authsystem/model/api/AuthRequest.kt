package com.test.authsystem.model.api

import jakarta.validation.constraints.NotBlank

data class AuthRequest(@field:NotBlank val login: String,
                       val password: CharArray)