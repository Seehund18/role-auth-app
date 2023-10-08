package com.test.authsystem.service

import com.test.authsystem.constants.DefaultRoles
import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.exception.SignInException
import com.test.authsystem.exception.DuplicateException
import com.test.authsystem.model.api.AuthRequest
import com.test.authsystem.model.api.CreateUserRequest
import com.test.authsystem.model.db.PasswordEntity
import com.test.authsystem.model.db.UserEntity
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class AuthService(private val rolesRepo: RolesRepository,
                  private val usersRepo: UsersRepository,
                  private val passHashingService: PBKDF2HashingService,
                  private val jwtTokenProvider: JwtTokenProvider,
                  private val log: KLogger = KotlinLogging.logger {}) {

    @Transactional
    fun signUpNewUser(createUserRequest: CreateUserRequest) : UserEntity {
        if (usersRepo.existsByLoginIgnoreCaseOrEmail(createUserRequest.login, createUserRequest.email)) {
            throw DuplicateException("User with such login or email already exists")
        }

        val (hashedPass, salt)  = passHashingService.generateHashedPassAndSalt(createUserRequest.password)
        val passwordEntity = PasswordEntity(passwordHash = hashedPass, salt = salt)

        val defaultRole = rolesRepo.findByNameIgnoreCase(DefaultRoles.USER.name)
        var userEntity = UserEntity(login = createUserRequest.login,
            email = createUserRequest.email,
            registrationTimestamp = LocalDateTime.now(),
            birthday = createUserRequest.birthday,
            role = defaultRole,
            passwordEntity = passwordEntity)
        userEntity = usersRepo.save(userEntity)

        log.info { "New user ${createUserRequest.login} has been saved" }
        return userEntity
    }

    @Transactional
    fun signInUser(authRequest: AuthRequest): Pair<String, LocalDate> {
        val user = usersRepo.findByLogin(authRequest.login) ?:
            throw SignInException("User with given login doesn't exist")

        if (!checkUserPassword(authRequest, user)) {
            throw SignInException("Wrong password")
        }

        return jwtTokenProvider.generateJwtToken(user);
    }

    private fun checkUserPassword(authRequest: AuthRequest, user: UserEntity): Boolean {
        val generatedPass = passHashingService.generateHashedPassWithSalt(authRequest.password,
            user.passwordEntity.salt)
        return user.passwordEntity.passwordHash.contentEquals(generatedPass)
    }

}