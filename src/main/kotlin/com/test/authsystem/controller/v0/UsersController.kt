package com.test.authsystem.controller.v0

import com.test.authsystem.model.api.CreateUserRequest
import com.test.authsystem.model.api.UserResponse
import com.test.authsystem.service.UserProcessingService
import jakarta.validation.Valid
import mu.KLogger
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v0/users")
class UsersController(var userProcService: UserProcessingService,
                      val log: KLogger = KotlinLogging.logger {}) {

    private val successStatus: String = "SUCCESS"

    @PostMapping
    fun addUser(@RequestBody @Valid createUserRequest: CreateUserRequest): UserResponse {
        log.info { "Processing request for adding a new user: ${createUserRequest.login}" }

        userProcService.saveNewUser(createUserRequest)

        return UserResponse(status = successStatus, description = "User has been successfully created")
    }
}