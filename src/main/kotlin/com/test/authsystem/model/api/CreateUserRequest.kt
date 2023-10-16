package com.test.authsystem.model.api

import com.test.authsystem.validation.NotBlankPassword
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import java.time.LocalDate

data class CreateUserRequest(
    @field:NotBlank(message = "login must not be blank")
    val login: String,

    @field:NotBlank(message = "email must not be blank")
    @field:Email(message = "not correct email")
    val email: String,

    @field:NotBlankPassword(message = "Password must not be blank")
    val password: CharArray,

    @field:Past(message = "Birthday must be in the past")
    val birthday: LocalDate
)