package com.test.authsystem.controller.v0

import com.test.authsystem.model.api.AuthRequest
import com.test.authsystem.model.api.AuthResponse
import com.test.authsystem.model.api.CreateUserRequest
import com.test.authsystem.model.api.UserResponse
import com.test.authsystem.service.AuthService
import jakarta.validation.Valid
import mu.KLogger
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v0/users")
class UsersController(var authService: AuthService,
                      val log: KLogger = KotlinLogging.logger {}) {

    private val successStatus: String = "SUCCESS"

    @PostMapping
    fun addUser(@RequestBody @Valid createUserRequest: CreateUserRequest): UserResponse {
        log.info { "Processing request for adding a new user: ${createUserRequest.login}" }

        authService.signUpNewUser(createUserRequest)


        return UserResponse(status = successStatus, description = "User has been successfully created")
    }

    @PostMapping("/auth")
    fun authUser(@RequestBody authRequest: AuthRequest): AuthResponse {
        log.info { "Processing auth request for user ${authRequest.login}" }

        val (jwtToken, expirationDate) = authService.signInUser(authRequest)

        return AuthResponse(successStatus, jwtToken, expirationDate)
    }
}