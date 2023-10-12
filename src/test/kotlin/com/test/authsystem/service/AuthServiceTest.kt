package com.test.authsystem.service

import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.exception.DuplicateException
import com.test.authsystem.exception.PassDoesntMatchException
import com.test.authsystem.generatePassEntity
import com.test.authsystem.generateRoleEntity
import com.test.authsystem.generateUserEntity
import com.test.authsystem.model.api.AuthRequest
import com.test.authsystem.model.api.ChangePassRequest
import com.test.authsystem.model.api.CreateUserRequest
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
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
        `when`(usersRepo.save(any())).thenAnswer { answer -> answer.arguments[0] }
        whenever(usersRepo.existsByLoginIgnoreCaseOrEmail(eq(expectedLogin), eq(expectedEmail))).thenReturn(false)
        whenever(passHashingService.generateHashedPassAndSalt(any())).thenReturn("hashedPass".toByteArray() to "salt".toByteArray())
        whenever(rolesRepo.findByNameIgnoreCase(any())).thenReturn(generateRoleEntity())

        val userEntity = authService.signUpNewUser(createUserRequest)

        assertEquals(expectedLogin, userEntity.login)
        assertEquals(expectedEmail, userEntity.email)
        assertEquals(expectedBirthday, userEntity.birthday)
        assertNotEquals(testPassword.toByteArray(), userEntity.passwordEntity.passwordHash)
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

        Assertions.assertThrows(DuplicateException::class.java) { authService.signUpNewUser(createUserRequest) }
    }

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

        Assertions.assertThrows(NoSuchElementException::class.java) { authService.signInUser(authRequest) }

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
        `when`(usersRepo.save(any())).thenAnswer { answer -> answer.arguments[0] }

        val userEntity = authService.changePassword(expectedLogin, changePassRequest)

        assertEquals(expectedLogin, userEntity.login)
        assertEquals(expectedEmail, userEntity.email)
        assertEquals(expectedBirthday, userEntity.birthday)
        assertArrayEquals(expectedNewPassHash.toByteArray(), userEntity.passwordEntity.passwordHash)
    }

    @Test
    fun testChangePasswordErrorOnUserAbsence() {
        val expectedLogin = "testLogin"
        val changePassRequest = ChangePassRequest(
            oldPass = "someOldPass".toCharArray(),
            newPass = "someNewPass".toCharArray()
        )

        whenever(usersRepo.findByLoginIgnoreCase(any())).thenReturn(null)

        Assertions.assertThrows(NoSuchElementException::class.java) { authService.changePassword(expectedLogin, changePassRequest) }

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

        Assertions.assertThrows(PassDoesntMatchException::class.java) { authService.changePassword(expectedLogin, changePassRequest) }
    }
}