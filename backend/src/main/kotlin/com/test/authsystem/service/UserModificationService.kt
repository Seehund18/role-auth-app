package com.test.authsystem.service

import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.exception.PassDoesntMatchException
import com.test.authsystem.model.api.ChangePassRequest
import com.test.authsystem.model.api.ChangeRoleRequest
import com.test.authsystem.model.db.PasswordEntity
import com.test.authsystem.model.db.UserEntity
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class UserModificationService(
    private val passHashingService: PassHashingService,
    private val rolesRepo: RolesRepository,
    private val usersRepo: UsersRepository
) {

    private val log: KLogger = KotlinLogging.logger {}

    @Transactional
    fun changePassword(login: String, changePassRequest: ChangePassRequest): UserEntity {
        var user = usersRepo.findByLoginIgnoreCase(login) ?: throw NoSuchElementException("User with given login doesn't exist")

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
    fun changeUserRole(login : String, changeRoleRequest: ChangeRoleRequest) : UserEntity {
        log.info { "Changing role of $login to ${changeRoleRequest.newRole}" }

        val newRole = rolesRepo.findByNameIgnoreCase(changeRoleRequest.newRole) ?: throw NoSuchElementException("Role wasn't found")
        val user = usersRepo.findByLoginIgnoreCase(login) ?: throw NoSuchElementException("User wasn't found")
        user.role = newRole

        return usersRepo.save(user)
    }
}