package com.test.authsystem.service

import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.exception.PassDoesntMatchException
import com.test.authsystem.generatePassEntity
import com.test.authsystem.generateRoleEntity
import com.test.authsystem.generateUserEntity
import com.test.authsystem.model.api.ChangePassRequest
import com.test.authsystem.model.api.ChangeRoleRequest
import java.time.LocalDate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class UserModificationServiceTest {

    private val rolesRepo = Mockito.mock(RolesRepository::class.java)
    private val usersRepo = Mockito.mock(UsersRepository::class.java)
    private val passHashingService = Mockito.mock(PassHashingService::class.java)

    private val userModificationService = UserModificationService(passHashingService, rolesRepo, usersRepo)

    @Test
    fun testChangePasswordSuccess() {
        val expectedLogin = "testLogin"
        val expectedEmail = "testEmail"
        val testPassHash = "somePassHash"
        val expectedNewPassHash = "someNewHashPass"
        val expectedBirthday = LocalDate.now()
        val changePassRequest = ChangePassRequest(
            oldPass = "someOldPass".toCharArray(),
            newPass = "someNewPass".toCharArray()
        )

        val passwordEntity = generatePassEntity(passHash = testPassHash)
        whenever(usersRepo.findByLoginIgnoreCase(any())).thenReturn(
            generateUserEntity(
                login = expectedLogin,
                email = expectedEmail,
                passEntity = passwordEntity
            )
        )
        whenever(passHashingService.generateHashedPassWithSalt(any(), any())).thenReturn(testPassHash.toByteArray())
        whenever(passHashingService.generateHashedPassAndSalt(any())).thenReturn(expectedNewPassHash.toByteArray() to "newSalt".toByteArray())
        // Return the argument of save function
        Mockito.`when`(usersRepo.save(any())).thenAnswer { answer -> answer.arguments[0] }

        val userEntity = userModificationService.changePassword(expectedLogin, changePassRequest)

        kotlin.test.assertEquals(expectedLogin, userEntity.login)
        kotlin.test.assertEquals(expectedEmail, userEntity.email)
        kotlin.test.assertEquals(expectedBirthday, userEntity.birthday)
        Assertions.assertArrayEquals(expectedNewPassHash.toByteArray(), userEntity.passwordEntity.passwordHash)
    }

    @Test
    fun testChangePasswordErrorOnUserAbsence() {
        val expectedLogin = "testLogin"
        val changePassRequest = ChangePassRequest(
            oldPass = "someOldPass".toCharArray(),
            newPass = "someNewPass".toCharArray()
        )

        whenever(usersRepo.findByLoginIgnoreCase(any())).thenReturn(null)

        assertThrows(NoSuchElementException::class.java) {
            userModificationService.changePassword(
                expectedLogin,
                changePassRequest
            )
        }

        verify(usersRepo).findByLoginIgnoreCase(eq(expectedLogin))
    }

    @Test
    fun testChangePasswordErrorOnHashMismatch() {
        val expectedLogin = "testLogin"
        val expectedEmail = "testEmail"
        val testPassHash = "somePassHash"
        val wrongPassHash = "completelyOtherHash"
        val changePassRequest = ChangePassRequest(
            oldPass = "someOldPass".toCharArray(),
            newPass = "someNewPass".toCharArray()
        )

        val passwordEntity = generatePassEntity(passHash = testPassHash)
        whenever(usersRepo.findByLoginIgnoreCase(any())).thenReturn(
            generateUserEntity(
                login = expectedLogin,
                email = expectedEmail,
                passEntity = passwordEntity
            )
        )
        whenever(passHashingService.generateHashedPassWithSalt(any(), any())).thenReturn(wrongPassHash.toByteArray())

        assertThrows(PassDoesntMatchException::class.java) {
            userModificationService.changePassword(
                expectedLogin,
                changePassRequest
            )
        }
    }

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