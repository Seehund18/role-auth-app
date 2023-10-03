package com.test.authsystem.model.api

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class CreateUserRequest(@field:NotBlank(message = "login must not be blank") val login: String,
                             @field:NotBlank(message = "email must not be blank")
                             @field:Email(message = "not correct email")
                             val email: String,
                             @field:NotBlank(message = "password must not be blank") val password: String,
                             val birthday: LocalDate)