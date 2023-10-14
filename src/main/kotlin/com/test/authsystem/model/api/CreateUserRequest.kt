package com.test.authsystem.model.api

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

data class CreateUserRequest(
    @field:NotBlank(message = "login must not be blank")
    val login: String,

    @field:NotBlank(message = "email must not be blank")
    @field:Email(message = "not correct email")
    val email: String,

    val password: CharArray,

    val birthday: LocalDate
)