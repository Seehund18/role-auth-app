package com.test.authsystem.controller.v0

import com.test.authsystem.constants.SystemResponseStatus
import com.test.authsystem.model.api.AuthRequest
import com.test.authsystem.model.api.AuthResponse
import com.test.authsystem.model.api.StatusResponse
import com.test.authsystem.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth API", description = "Authentication")
@RestController
@RequestMapping("/v0/auth")
class AuthController(
    val authService: AuthService,
    val log: KLogger = KotlinLogging.logger {}
) {

    @Operation(summary = "Authenticate the user with username and password. Available to everyone")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "User has been authenticated", content = [
                    (Content(mediaType = "application/json", schema = Schema(implementation = AuthResponse::class)))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Data from request isn't correct or valid",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StatusResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal error",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StatusResponse::class)
                )]
            )
        ]
    )
    @PostMapping
    fun authUser(@RequestBody @Valid authRequest: AuthRequest): ResponseEntity<AuthResponse> {
        log.info { "Processing auth request for user ${authRequest.login}" }
        val (jwtToken, expirationDate) = authService.signInUser(authRequest)

        return ResponseEntity.ok()
            .body(AuthResponse(SystemResponseStatus.SUCCESS.name, jwtToken, expirationDate))
    }
}