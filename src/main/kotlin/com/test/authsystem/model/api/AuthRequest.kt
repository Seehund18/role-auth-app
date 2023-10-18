package com.test.authsystem.model.api

import com.test.authsystem.validation.NotBlankPassword
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Authentication request")
data class AuthRequest(
    @Schema(description = "User login", example = "newUser", required = true)
    @field:NotBlank(message = "login must not be blank")
    val login: String,

    @Schema(description = "User password", example = "somePassword", required = true, type = "String")
    @field:NotBlankPassword(message = "password must not be blank")
    val password: CharArray
)