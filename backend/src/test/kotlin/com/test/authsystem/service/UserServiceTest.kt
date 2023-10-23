package com.test.authsystem.service

import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.exception.DuplicateException
import com.test.authsystem.exception.NoEntityWasFound
import com.test.authsystem.exception.PassDoesntMatchException
import com.test.authsystem.generatePassEntity
import com.test.authsystem.generateRoleEntity
import com.test.authsystem.generateUserEntity
import com.test.authsystem.model.api.ChangePassRequest
import com.test.authsystem.model.api.ChangeRoleRequest
import com.test.authsystem.model.api.CreateUserRequest
import com.test.authsystem.model.db.EndpointsEntity
import com.test.authsystem.model.db.RoleEntity
import java.time.LocalDate
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class UserServiceTest {

    private val rolesRepo = Mockito.mock(RolesRepository::class.java)
    private val usersRepo = Mockito.mock(UsersRepository::class.java)
    private val passHashingService = Mockito.mock(PassHashingService::class.java)

    private val userService = UserService(passHashingService, rolesRepo, usersRepo)

    @ParameterizedTest
    @MethodSource("roleAndExpectedEndpoints")
    fun testGetUserInfoSuccess(userRole: SystemRoles, expectedEndpoints: List<String>) {
        val expectedLogin = "testLogin"
        val expectedEmail = "testEmail"
        val testPassHash = "somePassHash"
        val expectedBirthday = LocalDate.now()

        val passwordEntity = generatePassEntity(passHash = testPassHash)
        whenever(usersRepo.findByLoginIgnoreCase(any())).thenReturn(
            generateUserEntity(
                login = expectedLogin,
                email = expectedEmail,
                passEntity = passwordEntity,
                roleEntity = generateRoleEntity(userRole.name)
            )
        )

        whenever(rolesRepo.findByPriorityValueGreaterThanEqual(any())).thenReturn(
            generateRolesWithEqualOrLessPriority(userRole)
        )

        val (userEntity, endpoints) = userService.getUserInfo(expectedLogin)

        assertEquals(expectedLogin, userEntity.login)
        assertEquals(expectedEmail, userEntity.email)
        assertEquals(expectedBirthday, userEntity.birthday)
        assertArrayEquals(testPassHash.toByteArray(), userEntity.passwordEntity.passwordHash)
        assertTrue(endpoints.map { endpoint -> endpoint.url }.containsAll(expectedEndpoints))
    }

    private fun generateRolesWithEqualOrLessPriority(role: SystemRoles): List<RoleEntity> {
        val admin = generateRoleEntity(SystemRoles.ADMIN.name, listOf(EndpointsEntity(url = "/api/admin", description = "Admin endpoint")))
        val reviewer = generateRoleEntity(SystemRoles.REVIEWER.name, listOf(EndpointsEntity(url = "/api/reviewer", description = "Reviewer endpoint")))
        val user = generateRoleEntity(SystemRoles.USER.name, listOf(EndpointsEntity(url = "/api/user", description = "User endpoint")))

        return when (role) {
            SystemRoles.ADMIN -> listOf(admin, reviewer, user)
            SystemRoles.REVIEWER -> listOf(reviewer, user)
            SystemRoles.USER-> listOf(user)
        }
    }

    companion object {
        @JvmStatic
        fun roleAndExpectedEndpoints() = listOf(
            Arguments.of(SystemRoles.ADMIN, listOf("/api/admin", "/api/reviewer", "/api/user")),
            Arguments.of(SystemRoles.REVIEWER, listOf("/api/reviewer", "/api/user")),
            Arguments.of(SystemRoles.USER, listOf("/api/user"))
        )
    }

    @ParameterizedTest
    @MethodSource("roleAndExpectedEndpoints")
    fun testGetUserErrorOnUserAbsence(userRole: SystemRoles, expectedEndpoints: List<String>) {
        val expectedLogin = "testLogin"

        whenever(usersRepo.findByLoginIgnoreCase(any())).thenReturn(null)

        assertThrows(NoEntityWasFound::class.java) {
            userService.getUserInfo(expectedLogin)
        }
        verify(usersRepo).findByLoginIgnoreCase(eq(expectedLogin))
    }

    @Test
    fun testSignUpNewUserSuccess() {
        val expectedLogin = "testLogin"
        val expectedEmail = "testEmail"
        val testPassword = "someTestPassword"
        val expectedBirthday = LocalDate.now()
        val createUserRequest = CreateUserRequest(
            login = expectedLogin,
            email = expectedEmail,
            password = testPassword.toCharArray(),
            birthday = expectedBirthday
        )

        // Return the argument of save function
        Mockito.`when`(usersRepo.save(any())).thenAnswer { answer -> answer.arguments[0] }
        whenever(usersRepo.existsByLoginIgnoreCaseOrEmail(eq(expectedLogin), eq(expectedEmail))).thenReturn(false)
        whenever(passHashingService.generateHashedPassAndSalt(any())).thenReturn("hashedPass".toByteArray() to "salt".toByteArray())
        whenever(rolesRepo.findByNameIgnoreCase(any())).thenReturn(generateRoleEntity())

        val userEntity = userService.createNewUser(createUserRequest)

        kotlin.test.assertEquals(expectedLogin, userEntity.login)
        kotlin.test.assertEquals(expectedEmail, userEntity.email)
        kotlin.test.assertEquals(expectedBirthday, userEntity.birthday)
        Assertions.assertNotEquals(testPassword.toByteArray(), userEntity.passwordEntity.passwordHash)
    }

    @Test
    fun testSignUpNewUserErrorOnDuplicateLoginOrEmail() {
        val expectedLogin = "testLogin"
        val expectedEmail = "testEmail"
        val testPassword = "someTestPassword"
        val expectedBirthday = LocalDate.now()
        val createUserRequest = CreateUserRequest(
            login = expectedLogin,
            email = expectedEmail,
            password = testPassword.toCharArray(),
            birthday = expectedBirthday
        )

        whenever(usersRepo.existsByLoginIgnoreCaseOrEmail(eq(expectedLogin), eq(expectedEmail))).thenReturn(true)

        assertThrows(DuplicateException::class.java) { userService.createNewUser(createUserRequest) }
    }

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

        val userEntity = userService.changePassword(expectedLogin, changePassRequest)

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

        assertThrows(NoEntityWasFound::class.java) {
            userService.changePassword(
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
            userService.changePassword(
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

        val user = userService.changeUserRole(expectedLogin, changeRoleRequest)

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

        assertThrows(NoEntityWasFound::class.java) {
            userService.changeUserRole(expectedLogin, changeRoleRequest)
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

        assertThrows(NoEntityWasFound::class.java) {
            userService.changeUserRole(expectedLogin, changeRoleRequest)
        }
    }
}