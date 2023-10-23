package com.test.authsystem.service

import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.exception.DuplicateException
import com.test.authsystem.exception.NoEntityWasFound
import com.test.authsystem.exception.PassDoesntMatchException
import com.test.authsystem.model.api.ChangePassRequest
import com.test.authsystem.model.api.ChangeRoleRequest
import com.test.authsystem.model.api.CreateUserRequest
import com.test.authsystem.model.api.Endpoint
import com.test.authsystem.model.db.PasswordEntity
import com.test.authsystem.model.db.UserEntity
import jakarta.transaction.Transactional
import java.time.LocalDateTime
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class UserService(
    private val passHashingService: PassHashingService,
    private val rolesRepo: RolesRepository,
    private val usersRepo: UsersRepository
) {

    private val log: KLogger = KotlinLogging.logger {}
    private val defaultRole: SystemRoles = SystemRoles.USER

    @Transactional
    fun getUserInfo(login: String): Pair<UserEntity, List<Endpoint>> {
        val user = usersRepo.findByLoginIgnoreCase(login)
            ?: throw NoEntityWasFound("User with given login doesn't exist")

        val roles = rolesRepo.findByPriorityValueGreaterThanEqual(user.role.priorityValue)
        val availableEndpoints = roles.flatMap { roleEntity -> roleEntity.endpointsList }
            .map { endpointEntity ->
                Endpoint(
                    description = endpointEntity.description,
                    url = endpointEntity.url
                )
            }

        log.info { "Extracted data for $login" }
        return user to availableEndpoints
    }

    @Transactional
    fun createNewUser(createUserRequest: CreateUserRequest): UserEntity {
        if (usersRepo.existsByLoginIgnoreCaseOrEmail(createUserRequest.login, createUserRequest.email)) {
            throw DuplicateException("User with such login or email already exists")
        }

        val (hashedPass, salt) = passHashingService.generateHashedPassAndSalt(createUserRequest.password)
        val passwordEntity = PasswordEntity(passwordHash = hashedPass, salt = salt)

        val defaultRole =
            rolesRepo.findByNameIgnoreCase(defaultRole.name) ?: throw RuntimeException("$defaultRole role wasn't found")
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
    fun changePassword(login: String, changePassRequest: ChangePassRequest): UserEntity {
        var user = usersRepo.findByLoginIgnoreCase(login)
            ?: throw NoEntityWasFound("User with given login doesn't exist")

        if (!checkUserPassword(changePassRequest.oldPass, user)) {
            throw PassDoesntMatchException("Wrong password")
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

    @Transactional
    fun changeUserRole(login: String, changeRoleRequest: ChangeRoleRequest): UserEntity {
        val newRole = rolesRepo.findByNameIgnoreCase(changeRoleRequest.newRole)
            ?: throw NoEntityWasFound("Role wasn't found")
        var user = usersRepo.findByLoginIgnoreCase(login) ?: throw NoEntityWasFound("User wasn't found")
        user.role = newRole

        user = usersRepo.save(user)

        log.info { "Changed role of $login to ${changeRoleRequest.newRole}" }
        return user
    }
}