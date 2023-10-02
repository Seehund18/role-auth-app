package com.test.authsystem.controller.v0

import com.test.authsystem.model.User
import com.test.authsystem.service.UserProcessingService
import jakarta.validation.Valid
import mu.KLogger
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/v0/users")
class UsersController(var userProcService: UserProcessingService,
                      val log: KLogger = KotlinLogging.logger {}) {


    @PostMapping
    fun addUser(@RequestBody @Valid user: User): User {
        val requestId = UUID.randomUUID().toString()
        log.info { "[$requestId] Processing request for adding a new user: ${user.login}" }

        userProcService.saveNewUser(requestId, user)

        return user
    }
}