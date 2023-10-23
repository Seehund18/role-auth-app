package com.test.authsystem.controller.v0

import com.test.authsystem.aop.Authorized
import com.test.authsystem.constants.SystemResponseStatus
import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.model.api.StatusResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Roles API", description = "Role protected endpoints")
@RestController
@RequestMapping("/v0/roles")
class RolesController {

    @Operation(summary = "ADMIN protected endpoint")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "User permissions were enough to access", content = [
                    (Content(mediaType = "application/json", schema = Schema(implementation = StatusResponse::class)))]
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
    @Authorized(minRole = SystemRoles.ADMIN)
    @GetMapping("/admin")
    fun adminEndpoint(jwtClaims: MutableMap<String, String>): ResponseEntity<StatusResponse> {
        return ResponseEntity.ok()
            .body(
                StatusResponse(
                    status = SystemResponseStatus.SUCCESS.name,
                    description = "Response from admin endpoint"
                )
            )
    }

    @Operation(summary = "REVIEWER protected endpoint")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "User permissions were enough to access", content = [
                    (Content(mediaType = "application/json", schema = Schema(implementation = StatusResponse::class)))]
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
    @Authorized(minRole = SystemRoles.REVIEWER)
    @GetMapping("/reviewer")
    fun reviewerEndpoint(jwtClaims: MutableMap<String, String>): ResponseEntity<StatusResponse> {
        return ResponseEntity.ok()
            .body(
                StatusResponse(
                    status = SystemResponseStatus.SUCCESS.name,
                    description = "Response from reviewer endpoint"
                )
            )
    }

    @Operation(summary = "USER protected endpoint")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "User permissions were enough to access", content = [
                    (Content(mediaType = "application/json", schema = Schema(implementation = StatusResponse::class)))]
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
    @Authorized(minRole = SystemRoles.USER)
    @GetMapping("/user")
    fun userEndpoint(jwtClaims: MutableMap<String, String>): ResponseEntity<StatusResponse> {
        return ResponseEntity.ok()
            .body(
                StatusResponse(
                    status = SystemResponseStatus.SUCCESS.name,
                    description = "Response from user endpoint"
                )
            )
    }

}