package com.test.authsystem.service

import com.test.authsystem.constants.DefaultRoles
import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.exception.DuplicateException
import com.test.authsystem.model.api.CreateUserRequest
import com.test.authsystem.model.db.PasswordEntity
import com.test.authsystem.model.db.UserEntity
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserProcessingService(val rolesRepo: RolesRepository,
                            val usersRepo: UsersRepository,
                            val passHashingService: PBKDF2HashingService,
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

        val (hashedPass, salt)  = passHashingService.generateHashedPassAndSalt(createUserRequest.password)

        val passwordEntity = PasswordEntity(passwordHash = hashedPass, salt = salt)
        var userEntity = UserEntity(login = createUserRequest.login,
            email = createUserRequest.email,
            registrationTimestamp = LocalDateTime.now(),
            birthday = createUserRequest.birthday,
            role = defaultRole,
            password = passwordEntity)

        userEntity = usersRepo.save(userEntity)

        log.info { "New user ${createUserRequest.login} has been saved" }

        return userEntity
    }

}