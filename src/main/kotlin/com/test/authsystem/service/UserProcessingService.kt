package com.test.authsystem.service

import com.test.authsystem.constants.DefaultRoles
import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.exception.DuplicateException
import com.test.authsystem.model.api.CreateUserRequest
import com.test.authsystem.model.db.UserEntity
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserProcessingService(val rolesRepo: RolesRepository,
                            val usersRepo: UsersRepository,
                            val log: KLogger = KotlinLogging.logger {}) {

    @Transactional
    fun saveNewUser(createUserRequest: CreateUserRequest) : UserEntity {

        if (usersRepo.existsByLoginIgnoreCase(createUserRequest.login)) {
            throw DuplicateException("User with login ${createUserRequest.login} already exists")
        }

        if (usersRepo.existsByEmail(createUserRequest.email)) {
            throw DuplicateException("User with email ${createUserRequest.email} already exists")
        }

        val defaultRole = rolesRepo.findByNameIgnoreCase(DefaultRoles.USER.name)
        var userEntity = UserEntity(null, createUserRequest.login, createUserRequest.email, createUserRequest.password, LocalDateTime.now(), createUserRequest.birthday, defaultRole)

        userEntity = usersRepo.save(userEntity)

        return userEntity
    }

}