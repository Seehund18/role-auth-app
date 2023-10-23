package com.test.authsystem.service

import com.test.authsystem.constants.AuthClaims
import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.exception.DuplicateException
import com.test.authsystem.exception.NoEntityWasFound
import com.test.authsystem.exception.NotEnoughPermissionsException
import com.test.authsystem.exception.PassDoesntMatchException
import com.test.authsystem.generatePassEntity
import com.test.authsystem.generateRoleEntity
import com.test.authsystem.generateUserEntity
import com.test.authsystem.model.api.AuthRequest
import com.test.authsystem.model.api.CreateUserRequest
import com.test.authsystem.model.db.RoleEntity
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class AuthServiceTest {

    private val rolesRepo = mock(RolesRepository::class.java)
    private val usersRepo = mock(UsersRepository::class.java)
    private val passHashingService = mock(PassHashingService::class.java)
    private val jwtTokenHandler = mock(JwtTokenHandler::class.java)

    private val authService = AuthService(rolesRepo, usersRepo, passHashingService, jwtTokenHandler)

    @Test
    fun testSignInUserSuccess() {
        val expectedLogin = "testLogin"
        val expectedEmail = "testEmail"
        val testPassword = "someTestPassword"
        val testPassHash = "somePassHash"
        val authRequest = AuthRequest(
            login = expectedLogin,
            password = testPassword.toCharArray()
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
        whenever(jwtTokenHandler.generateJwtToken(any())).thenReturn("someJwt" to LocalDateTime.now())

        authService.signInUser(authRequest)

        verify(usersRepo).findByLoginIgnoreCase(eq(expectedLogin))
        verify(passHashingService).generateHashedPassWithSalt(eq(testPassword.toCharArray()), any())
        verify(jwtTokenHandler).generateJwtToken(any())
    }

    @Test
    fun testSignInUserErrorOnUserAbsence() {
        val expectedLogin = "testLogin"
        val testPassword = "someTestPassword"
        val authRequest = AuthRequest(
            login = expectedLogin,
            password = testPassword.toCharArray()
        )

        whenever(usersRepo.findByLoginIgnoreCase(any())).thenReturn(null)

        Assertions.assertThrows(NoEntityWasFound::class.java) { authService.signInUser(authRequest) }

        verify(usersRepo).findByLoginIgnoreCase(eq(expectedLogin))
    }

    @Test
    fun testSignInUserErrorOnHashMismatch() {
        val expectedLogin = "testLogin"
        val expectedEmail = "testEmail"
        val testPassword = "someTestPassword"
        val testPassHash = "somePassHash"
        val wrongPassHash = "completelyOtherHash"
        val authRequest = AuthRequest(
            login = expectedLogin,
            password = testPassword.toCharArray()
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

        Assertions.assertThrows(PassDoesntMatchException::class.java) { authService.signInUser(authRequest) }
    }

    @ParameterizedTest
    @MethodSource("successRolePairs")
    fun testAuthorizeRequestSuccess(userRole: SystemRoles, minRequiredRole: SystemRoles) {
        val dumbJwt = "Dumb_jwt_token"
        val user = "someUser"
        val email = "someEmail@gmail.com"
        val expectedMapWithClaims = mapOf(
            AuthClaims.ROLE.claimName to userRole.name,
            AuthClaims.LOGIN.claimName to user,
            AuthClaims.EMAIL.claimName to "someEmail@gmail.com"
        )

        whenever(jwtTokenHandler.getAllClaimsFromToken(dumbJwt)).thenReturn(expectedMapWithClaims)
        whenever(usersRepo.findByLoginIgnoreCase(user)).thenReturn(
            generateUserEntity(
                user, email,
                roleEntity = generateRoleEntity(userRole.name),
                passEntity = null
            )
        )
        whenever(rolesRepo.findByNameIgnoreCase(any())).thenReturn(
            generateRoleEntity(minRequiredRole.name)
        )
        whenever(rolesRepo.findByPriorityValueLessThanEqual(any())).thenReturn(
            generateRolesWithEqualOrMorePriority(
                minRequiredRole
            )
        )

        val claims = authService.authorizeRequest(dumbJwt, minRequiredRole)
        assertEquals(expectedMapWithClaims, claims)
        verify(jwtTokenHandler).getAllClaimsFromToken(eq(dumbJwt))
    }

    @ParameterizedTest
    @MethodSource("badRolePairs")
    fun testAuthorizeRequestErrorOnInsufficientPermissions(userRole: SystemRoles, minRequiredRole: SystemRoles) {
        val dumbJwt = "Dumb_jwt_token"
        val user = "someUser"
        val email = "someEmail@gmail.com"
        val mapWithClaims = mapOf(
            AuthClaims.ROLE.claimName to userRole.name,
            AuthClaims.LOGIN.claimName to user,
            AuthClaims.EMAIL.claimName to email
        )

        whenever(jwtTokenHandler.getAllClaimsFromToken(eq(dumbJwt))).thenReturn(mapWithClaims)
        whenever(usersRepo.findByLoginIgnoreCase(user)).thenReturn(
            generateUserEntity(
                login = user,
                email = email,
                roleEntity = generateRoleEntity(userRole.name),
                passEntity = null
            )
        )
        whenever(rolesRepo.findByNameIgnoreCase(any())).thenReturn(
            generateRoleEntity(minRequiredRole.name)
        )
        whenever(rolesRepo.findByPriorityValueLessThanEqual(any())).thenReturn(
            generateRolesWithEqualOrMorePriority(
                minRequiredRole
            )
        )

        Assertions.assertThrows(NotEnoughPermissionsException::class.java) {
            authService.authorizeRequest(dumbJwt, minRequiredRole)
        }
        verify(jwtTokenHandler).getAllClaimsFromToken(eq(dumbJwt))
    }

    private fun generateRolesWithEqualOrMorePriority(role: SystemRoles): List<RoleEntity> {
        return when (role) {
            SystemRoles.ADMIN -> listOf(generateRoleEntity(SystemRoles.ADMIN.name))
            SystemRoles.REVIEWER -> listOf(
                generateRoleEntity(SystemRoles.ADMIN.name),
                generateRoleEntity(SystemRoles.REVIEWER.name)
            )

            SystemRoles.USER -> listOf(
                generateRoleEntity(SystemRoles.ADMIN.name),
                generateRoleEntity(SystemRoles.REVIEWER.name),
                generateRoleEntity(SystemRoles.USER.name)
            )
        }
    }

    companion object {
        @JvmStatic
        fun successRolePairs() = listOf(
            // User role on the left and min required role on the right
            Arguments.of(SystemRoles.USER, SystemRoles.USER),
            Arguments.of(SystemRoles.REVIEWER, SystemRoles.USER),
            Arguments.of(SystemRoles.REVIEWER, SystemRoles.REVIEWER),
            Arguments.of(SystemRoles.ADMIN, SystemRoles.USER),
            Arguments.of(SystemRoles.ADMIN, SystemRoles.REVIEWER),
            Arguments.of(SystemRoles.ADMIN, SystemRoles.ADMIN),
        )

        @JvmStatic
        fun badRolePairs() = listOf(
            // User role on the left and min required role on the right
            Arguments.of(SystemRoles.USER, SystemRoles.ADMIN),
            Arguments.of(SystemRoles.REVIEWER, SystemRoles.ADMIN),
            Arguments.of(SystemRoles.USER, SystemRoles.REVIEWER)
        )
    }
}