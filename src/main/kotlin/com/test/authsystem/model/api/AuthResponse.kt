package com.test.authsystem.model.api

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Authentication response")
data class AuthResponse(
    @Schema(description = "Status", example = "SUCCESS", required = true)
    val status: String,
    @Schema(
        description = "Jwt token to use for subsequent requests",
        example = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhdXRoLXN5c3RlbSIsInN",
        required = true
    )
    val jwtToken: String,
    @Schema(description = "Timestamp of jwt token expiration", example = "2023-10-19T00:22:49.245625", required = true)
    val expirationDate: LocalDateTime
)