package com.test.authsystem.service

import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.exception.SignInException
import com.test.authsystem.exception.DuplicateException
import com.test.authsystem.model.api.AuthRequest
import com.test.authsystem.model.api.ChangePassRequest
import com.test.authsystem.model.api.CreateUserRequest
import com.test.authsystem.model.db.PasswordEntity
import com.test.authsystem.model.db.UserEntity
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthService(
    private val rolesRepo: RolesRepository,
    private val usersRepo: UsersRepository,
    private val passHashingService: PBKDF2HashingService,
    private val jwtTokenHandler: JwtTokenHandler,
    private val log: KLogger = KotlinLogging.logger {},
    private val defaultRole : SystemRoles = SystemRoles.USER
) {

    @Transactional
    fun signUpNewUser(createUserRequest: CreateUserRequest): UserEntity {
        if (usersRepo.existsByLoginIgnoreCaseOrEmail(createUserRequest.login, createUserRequest.email)) {
            throw DuplicateException("User with such login or email already exists")
        }

        val (hashedPass, salt) = passHashingService.generateHashedPassAndSalt(createUserRequest.password)
        val passwordEntity = PasswordEntity(passwordHash = hashedPass, salt = salt)

        val defaultRole = rolesRepo.findByNameIgnoreCase(defaultRole.name) ?: throw RuntimeException("$defaultRole role wasn't found")
        var userEntity = UserEntity(
            login = createUserRequest.login,
            email = createUserRequest.email,
            registrationTimestamp = LocalDateTime.now(),
            birthday = createUserRequest.birthday,
            role = defaultRole,
            passwordEntity = passwordEntity
        )
        userEntity = usersRepo.save(userEntity)

        log.info { "New user ${createUserRequest.login} has been saved" }
        return userEntity
    }

    @Transactional
    fun signInUser(authRequest: AuthRequest): Pair<String, LocalDateTime> {
        val user =
            usersRepo.findByLoginIgnoreCase(authRequest.login) ?: throw SignInException("User with given login doesn't exist")

        if (!checkUserPassword(authRequest.password, user)) {
            throw SignInException("Wrong password")
        }

        return jwtTokenHandler.generateJwtToken(user);
    }

    @Transactional
    fun changePassword(login: String, changePassRequest: ChangePassRequest): UserEntity {
        var user = usersRepo.findByLoginIgnoreCase(login) ?: throw SignInException("User with given login doesn't exist")

        if (!checkUserPassword(changePassRequest.oldPass, user)) {
            throw SignInException("Wrong password")
        }

        val (hashedPass, salt) = passHashingService.generateHashedPassAndSalt(changePassRequest.newPass)
        val passwordEntity = PasswordEntity(passwordHash = hashedPass, salt = salt)
        user.passwordEntity = passwordEntity

        user = usersRepo.save(user)

        log.info { "Password has been updated for $login" }
        return user
    }

    private fun checkUserPassword(password: CharArray, user: UserEntity): Boolean {
        val generatedPass = passHashingService.generateHashedPassWithSalt(password, user.passwordEntity.salt)
        return user.passwordEntity.passwordHash.contentEquals(generatedPass)
    }

}