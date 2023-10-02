package com.test.authsystem.service

import com.test.authsystem.db.RolesRepository
import com.test.authsystem.model.User
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class UserProcessingService(val rolesRepo: RolesRepository,
                            val log: KLogger = KotlinLogging.logger {}) {

    fun saveNewUser(requestId: String, user: User) : Boolean {

        val userRole = rolesRepo.findByName("User")

        log.info { "[$requestId] User role $userRole" }

        return true
    }

}