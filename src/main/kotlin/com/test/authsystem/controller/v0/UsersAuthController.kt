package com.test.authsystem.controller.v0

import com.test.authsystem.constants.SystemResponseStatus
import com.test.authsystem.model.api.AuthRequest
import com.test.authsystem.model.api.AuthResponse
import com.test.authsystem.model.api.ChangePassRequest
import com.test.authsystem.model.api.CreateUserRequest
import com.test.authsystem.model.api.StatusResponse
import com.test.authsystem.service.AuthService
import jakarta.validation.Valid
import mu.KLogger
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v0/users")
class UsersAuthController(
    var authService: AuthService,
    val log: KLogger = KotlinLogging.logger {}
) {

    @PostMapping
    fun addUser(@RequestBody @Valid createUserRequest: CreateUserRequest): StatusResponse {
        log.info { "Processing request for adding a new user: ${createUserRequest.login}" }

        authService.signUpNewUser(createUserRequest)

        return StatusResponse(status = SystemResponseStatus.SUCCESS.name, description = "User has been successfully created")
    }

    @PostMapping("/auth")
    fun authUser(@RequestBody authRequest: AuthRequest): AuthResponse {
        log.info { "Processing auth request for user ${authRequest.login}" }

        val (jwtToken, expirationDate) = authService.signInUser(authRequest)

        return AuthResponse(SystemResponseStatus.SUCCESS.name, jwtToken, expirationDate)
    }

    @PostMapping("/{user}/password")
    fun changePassword(
        @PathVariable("user") login: String,
        @RequestBody changeRequest: ChangePassRequest
    ): StatusResponse {
        log.info { "Processing change password request for user $login" }

        authService.changePassword(login, changeRequest)

        return StatusResponse(status = SystemResponseStatus.SUCCESS.name, description = "Password has been changed")
    }
}