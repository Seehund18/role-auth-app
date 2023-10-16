package com.test.authsystem.model.api

import com.test.authsystem.validation.NotBlankPassword
import jakarta.validation.constraints.NotBlank

data class AuthRequest(
    @field:NotBlank(message = "login must not be blank")
    val login: String,

    @field:NotBlankPassword(message = "password must not be blank")
    val password: CharArray
)