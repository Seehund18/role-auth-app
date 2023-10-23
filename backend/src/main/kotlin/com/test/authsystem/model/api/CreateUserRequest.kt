package com.test.authsystem.model.api

import com.test.authsystem.validation.NotBlankPassword
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import java.time.LocalDate

@Schema(description = "Request for creating a new user")
data class CreateUserRequest(
    @Schema(description = "User login", example = "newUser", required = true)
    @field:NotBlank(message = "login must not be blank")
    val login: String,

    @Schema(description = "User email", example = "newUser@gmail.com", required = true)
    @field:NotBlank(message = "email must not be blank")
    @field:Email(message = "not correct email")
    val email: String,

    @Schema(description = "User password", example = "someNewPassword", required = true, type = "String")
    @field:NotBlankPassword(message = "Password must not be blank")
    val password: CharArray,

    @Schema(description = "User birthday in format YYYY-mm-dd", example = "1990-09-24", required = false)
    @field:Past(message = "Birthday must be in the past")
    val birthday: LocalDate?
)