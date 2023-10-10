package com.test.authsystem.service

import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.model.api.ChangeRoleRequest
import com.test.authsystem.model.db.RoleEntity
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class UserModificationService(
    private val rolesRepo: RolesRepository,
    private val usersRepo: UsersRepository,
    private val log: KLogger = KotlinLogging.logger {}
) {

    @Transactional
    fun changeUserRole(login : String, changeRoleRequest: ChangeRoleRequest) {
        val newRole = rolesRepo.findByNameIgnoreCase(changeRoleRequest.newRole) ?: throw NoSuchElementException("Role wasn't found")
        val user = usersRepo.findByLoginIgnoreCase(login) ?: throw NoSuchElementException("User wasn't found")

        user.role = newRole
        usersRepo.save(user)
    }
}