package com.test.authsystem.controller.v0

import com.test.authsystem.aop.Authorized
import com.test.authsystem.constants.AuthClaims
import com.test.authsystem.constants.SystemResponseStatus
import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.exception.UsersDontMatchException
import com.test.authsystem.model.api.AuthRequest
import com.test.authsystem.model.api.AuthResponse
import com.test.authsystem.model.api.ChangePassRequest
import com.test.authsystem.model.api.ChangeRoleRequest
import com.test.authsystem.model.api.CreateUserRequest
import com.test.authsystem.model.api.StatusResponse
import com.test.authsystem.service.AuthService
import com.test.authsystem.service.JwtTokenHandler
import com.test.authsystem.service.UserModificationService
import com.test.authsystem.util.extractJwtTokenFromHeader
import jakarta.validation.Valid
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v0/users")
class UsersAuthController(
    var authService: AuthService,
    var userModificationService: UserModificationService,
    var jwtTokenHandler: JwtTokenHandler,
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

    @Authorized(SystemRoles.USER)
    @PutMapping("/{user}/password")
    fun changePassword(
        @PathVariable("user") login: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String,
        @RequestBody changeRequest: ChangePassRequest
    ): StatusResponse {
        checkJwtAndRequestUsers(authHeader, login)

        log.info { "Processing change password request for user $login" }
        authService.changePassword(login, changeRequest)

        return StatusResponse(status = SystemResponseStatus.SUCCESS.name, description = "Password has been changed")
    }

    private fun checkJwtAndRequestUsers(authHeader: String, login : String) {
        val jwtToken = extractJwtTokenFromHeader(authHeader)
        val jwtUser = jwtTokenHandler.getClaimFromToken(AuthClaims.LOGIN, jwtToken)
        if (jwtUser.lowercase() != login.lowercase()) throw UsersDontMatchException("Other users can't be modified")
    }

    @Authorized(SystemRoles.ADMIN)
    @PutMapping("/{user}/role")
    fun changeRole(
        @PathVariable("user") login: String,
        @RequestBody changeRoleRequest: ChangeRoleRequest
    ): StatusResponse {
        log.info { "Processing role change to ${changeRoleRequest.newRole} for the user $login" }
        userModificationService.changeUserRole(login, changeRoleRequest)

        return StatusResponse(status = SystemResponseStatus.SUCCESS.name, description = "Role has been changed")
    }
}