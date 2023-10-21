package com.test.authsystem.model.api

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Get user response")
data class GetUserInfoResponse(
    @Schema(description = "User login", example = "newUser", required = true)
    val login: String,

    @Schema(description = "User email", example = "newUser@gmail.com", required = true)
    val email: String,

    @Schema(description = "User birthday", example = "1995-08-20", required = false)
    val birthday: LocalDate?,

    @Schema(description = "User registration date", example = "2023-10-20", required = true)
    val registrationDate: LocalDate,

    @Schema(description = "User role", example = "user", required = true)
    val role: String,

    @Schema(description = "Information about available endpoints")
    val endpoints: List<Endpoint>? = emptyList()
)

data class Endpoint(
    @Schema(description = "User login", example = "newUser", required = true)
    val description: String,

    @Schema(description = "Endpoint url", example = "/v0/roles/user", required = true)
    val url: String

)