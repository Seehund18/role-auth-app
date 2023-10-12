package com.test.authsystem.service

import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.generateRoleEntity
import com.test.authsystem.generateUserEntity
import com.test.authsystem.model.api.ChangeRoleRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

internal class UserModificationServiceTest {

    private val rolesRepo = Mockito.mock(RolesRepository::class.java)
    private val usersRepo = Mockito.mock(UsersRepository::class.java)

    private val userModificationService = UserModificationService(rolesRepo, usersRepo)

    @Test
    fun testChangeUserRoleSuccess() {
        val expectedLogin = "testLogin"
        val newRole = "someNewRole"
        val changeRoleRequest = ChangeRoleRequest(
            newRole = newRole
        )

        whenever(rolesRepo.findByNameIgnoreCase(eq(newRole))).thenReturn(
            generateRoleEntity(
                name = newRole,
                description = "Some description",
                priorityValue = 100
            )
        )
        whenever(usersRepo.findByLoginIgnoreCase(any())).thenReturn(
            generateUserEntity(
                login = expectedLogin,
                email = null,
                roleEntity = generateRoleEntity(
                    name = "oldRole",
                    description = "old descriptions",
                    priorityValue = 1000
                ),
                passEntity = null
            )
        )
        // Return the argument of save function
        Mockito.`when`(usersRepo.save(any())).thenAnswer { answer -> answer.arguments[0] }

        val user = userModificationService.changeUserRole(expectedLogin, changeRoleRequest)

        assertEquals(newRole, user.role.name)
    }

    @Test
    fun testChangeUserRoleErrorOnRoleAbsence() {
        val expectedLogin = "testLogin"
        val newRole = "someNewRole"
        val changeRoleRequest = ChangeRoleRequest(
            newRole = newRole
        )

        whenever(rolesRepo.findByNameIgnoreCase(eq(newRole))).thenReturn(null)

        assertThrows(NoSuchElementException::class.java) {
            userModificationService.changeUserRole(expectedLogin, changeRoleRequest)
        }
    }

    @Test
    fun testChangeUserRoleErrorOnUserAbsence() {
        val expectedLogin = "testLogin"
        val newRole = "someNewRole"
        val changeRoleRequest = ChangeRoleRequest(
            newRole = newRole
        )

        whenever(rolesRepo.findByNameIgnoreCase(eq(newRole))).thenReturn(
            generateRoleEntity(
                name = newRole,
                description = "Some description",
                priorityValue = 100
            )
        )
        whenever(usersRepo.findByLoginIgnoreCase(any())).thenReturn(null)

        assertThrows(NoSuchElementException::class.java) {
            userModificationService.changeUserRole(expectedLogin, changeRoleRequest)
        }
    }
}