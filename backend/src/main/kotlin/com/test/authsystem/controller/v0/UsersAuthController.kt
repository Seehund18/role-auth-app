package com.test.authsystem.controller.v0

import com.test.authsystem.aop.Authorized
import com.test.authsystem.constants.AuthClaims
import com.test.authsystem.constants.SystemResponseStatus
import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.exception.UsersDontMatchException
import com.test.authsystem.model.api.ChangePassRequest
import com.test.authsystem.model.api.ChangeRoleRequest
import com.test.authsystem.model.api.CreateUserRequest
import com.test.authsystem.model.api.GetUserInfoResponse
import com.test.authsystem.model.api.StatusResponse
import com.test.authsystem.service.JwtTokenHandler
import com.test.authsystem.service.UserService
import com.test.authsystem.util.extractJwtTokenFromHeader
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.net.URI
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@OpenAPIDefinition(
    info = Info(
        title = "Role based auth system API",
        version = "0",
        description = """System with role protected endpoints. Currently supported roles:
            ADMIN > REVIEWER > USER
            """
    )
)
@Tag(name = "User API", description = "Actions with users")
@RestController
@RequestMapping("/v0/users")
class UsersAuthController(
    var userService: UserService,
    var jwtTokenHandler: JwtTokenHandler,
    val log: KLogger = KotlinLogging.logger {}
) {

    @Operation(summary = "Get user info. Available only to the user issuing request")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "User info has been retrieved", content = [
                    (Content(mediaType = "application/json", schema = Schema(implementation = StatusResponse::class)))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Data from the request isn't correct or valid",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StatusResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "No JWT token in Authorization header or it's malformed",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StatusResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "User doesn't have necessary permissions",
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
    @Authorized(SystemRoles.USER)
    @GetMapping("/{user}")
    fun getUser(
        jwtClaims: MutableMap<String, String>,
        @PathVariable("user") login: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String,
    ): ResponseEntity<GetUserInfoResponse> {
        checkJwtAndRequestUsers(authHeader, login)
        log.info { "Processing request for getting user info: $login" }

        val (userEntity, endpointList) = userService.getUserInfo(login)

        return ResponseEntity.ok()
            .body(
                GetUserInfoResponse(
                    login = userEntity.login,
                    email = userEntity.email,
                    birthday = userEntity.birthday,
                    registrationDate = userEntity.registrationTimestamp.toLocalDate(),
                    role = userEntity.role.name,
                    endpoints = endpointList
                )
            )
    }

    @Operation(summary = "Create new user. Available to everyone")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "User has been successfully created", content = [
                    (Content(mediaType = "application/json", schema = Schema(implementation = StatusResponse::class)))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Data from request isn't valid",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StatusResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "409",
                description = "User with such login and email already exists",
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
    fun addUser(@RequestBody @Valid createUserRequest: CreateUserRequest): ResponseEntity<StatusResponse> {
        log.info { "Processing request for adding a new user: ${createUserRequest.login}" }
        userService.createNewUser(createUserRequest)

        return ResponseEntity.created(URI("/v0/users/${createUserRequest.login}"))
            .body(
                StatusResponse(
                    status = SystemResponseStatus.SUCCESS.name,
                    description = "User has been successfully created"
                )
            )
    }

    @Operation(summary = "Change user's password. Available only to the user changing password")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Password has been changed", content = [
                    (Content(mediaType = "application/json", schema = Schema(implementation = StatusResponse::class)))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Data from the request isn't correct or valid",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StatusResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "No JWT token in Authorization header or it's malformed",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StatusResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "User doesn't have necessary permissions",
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
    @Authorized(SystemRoles.USER)
    @PutMapping("/{user}/password")
    fun changePassword(
        jwtClaims: MutableMap<String, String>,
        @PathVariable("user") login: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String,
        @RequestBody @Valid changeRequest: ChangePassRequest
    ): ResponseEntity<StatusResponse> {
        checkJwtAndRequestUsers(authHeader, login)

        log.info { "Processing change password request for user $login" }
        userService.changePassword(login, changeRequest)

        return ResponseEntity.ok()
            .body(StatusResponse(status = SystemResponseStatus.SUCCESS.name, description = "Password has been changed"))
    }

    private fun checkJwtAndRequestUsers(authHeader: String, login: String) {
        val jwtToken = extractJwtTokenFromHeader(authHeader)
        val jwtUser = jwtTokenHandler.getClaimFromToken(AuthClaims.LOGIN, jwtToken)
        if (jwtUser.lowercase() != login.lowercase()) throw UsersDontMatchException("Other users can't be changed")
    }

    @Operation(summary = "Change user's role. Available only to the admin")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "User role has been changed", content = [
                    (Content(mediaType = "application/json", schema = Schema(implementation = StatusResponse::class)))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Data from the request isn't correct or valid",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StatusResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "No JWT token in Authorization header or it's malformed",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StatusResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "User doesn't have necessary permissions",
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
    @Authorized(SystemRoles.ADMIN)
    @PutMapping("/{user}/role")
    fun changeUserRole(
        jwtClaims: MutableMap<String, String>,
        @PathVariable("user") login: String,
        @RequestBody @Valid changeRoleRequest: ChangeRoleRequest
    ): ResponseEntity<StatusResponse> {
        log.info { "Processing role change to ${changeRoleRequest.newRole} for the user $login" }
        userService.changeUserRole(login, changeRoleRequest)

        return ResponseEntity.ok()
            .body(StatusResponse(status = SystemResponseStatus.SUCCESS.name, description = "Role has been changed"))
    }
}