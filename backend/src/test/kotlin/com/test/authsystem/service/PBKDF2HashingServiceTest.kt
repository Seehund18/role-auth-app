package com.test.authsystem.service

import com.test.authsystem.config.props.PBKDF2HashingProps
import java.security.SecureRandom

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PBKDF2HashingServiceTest {

    private val hashingService : PBKDF2HashingService = PBKDF2HashingService(PBKDF2HashingProps())

    @Test
    fun testGenerateHashedPassAndSaltSuccess() {
        val pass = "someTestPass"

        val (hashedPass, salt) = hashingService.generateHashedPassAndSalt(pass.toCharArray())

        assertNotEquals(pass.toByteArray(), hashedPass)
        assertTrue(hashedPass.isNotEmpty())
        assertTrue(salt.isNotEmpty())
    }

    @Test
    fun testGenerateHashedPassWithSalt() {
        val pass = "anotherTestPass"
        val salt = ByteArray(10)
        SecureRandom().nextBytes(salt)

        val hashedPass = hashingService.generateHashedPassWithSalt(pass.toCharArray(), salt)

        assertNotEquals(pass.toByteArray(), hashedPass)
        assertTrue(hashedPass.isNotEmpty())
    }
}