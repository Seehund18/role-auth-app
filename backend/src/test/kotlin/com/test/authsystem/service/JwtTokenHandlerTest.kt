package com.test.authsystem.service

import com.test.authsystem.config.props.JwtTokenProps
import com.test.authsystem.constants.AuthClaims
import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.exception.JwtTokenException
import com.test.authsystem.model.db.PasswordEntity
import com.test.authsystem.model.db.RoleEntity
import com.test.authsystem.model.db.UserEntity
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class JwtTokenHandlerTest {

    private val testSecret = "vjhajkdvgjadbv,adbv.kha/dkhvkdjabvmabdv,mbad,vsva"
    private val testLifespan = Duration.ofMinutes(10)
    private val testIssuer = "test-issuer"
    private val random = Random(2556)

    private var jwtTokenHandler: JwtTokenHandler = JwtTokenHandler(
        JwtTokenProps(
            issuer = testIssuer,
            lifespan = testLifespan,
            secret = testSecret
        )
    )

    @Test
    fun testGenerateJwtTokenAndGetClaimsSuccess() {
        val currentTimestamp = LocalDateTime.now()
        val expectedSubject = random.nextLong()
        val expectedEmailClaim = "testEmail@gmail.com"
        val expectedLoginClaim = "someLogin"
        val expectedRole = SystemRoles.REVIEWER.name

        val userEntity = UserEntity(
            id = expectedSubject,
            login = expectedLoginClaim,
            email = expectedEmailClaim,
            registrationTimestamp = LocalDateTime.now(),
            birthday = LocalDate.now(),
            role = RoleEntity(random.nextLong(), expectedRole, "Test role", 10),
            passwordEntity = PasswordEntity(passwordHash = ByteArray(5), salt = ByteArray(5))
        )

        val (jwtToken, expirationTimestamp) = jwtTokenHandler.generateJwtToken(userEntity)
        val claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(testSecret.toByteArray()))
            .build()
            .parseClaimsJws(jwtToken)

        assertTrue(expirationTimestamp.isAfter(currentTimestamp))
        assertEquals(testIssuer, claims.body[Claims.ISSUER])
        assertEquals(expectedSubject.toString(), claims.body[Claims.SUBJECT])
        assertEquals(expectedEmailClaim, jwtTokenHandler.getClaimFromToken(AuthClaims.EMAIL, jwtToken))
        assertEquals(expectedLoginClaim, jwtTokenHandler.getClaimFromToken(AuthClaims.LOGIN, jwtToken))
        assertEquals(expectedRole, jwtTokenHandler.getClaimFromToken(AuthClaims.ROLE, jwtToken))
    }

    @Test
    fun testGenerateJwtTokenAndGetAllClaimsSuccess() {
        val currentTimestamp = LocalDateTime.now()
        val expectedSubject = random.nextLong()
        val expectedEmailClaim = "testEmail@gmail.com"
        val expectedLoginClaim = "someLogin"
        val expectedRole = SystemRoles.REVIEWER.name

        val userEntity = UserEntity(
            id = expectedSubject,
            login = expectedLoginClaim,
            email = expectedEmailClaim,
            registrationTimestamp = LocalDateTime.now(),
            birthday = LocalDate.now(),
            role = RoleEntity(random.nextLong(), expectedRole, "Test role", 10),
            passwordEntity = PasswordEntity(passwordHash = ByteArray(5), salt = ByteArray(5))
        )

        val (jwtToken, expirationTimestamp) = jwtTokenHandler.generateJwtToken(userEntity)
        val claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(testSecret.toByteArray()))
            .build()
            .parseClaimsJws(jwtToken)

        assertTrue(expirationTimestamp.isAfter(currentTimestamp))
        assertEquals(testIssuer, claims.body[Claims.ISSUER])
        assertEquals(expectedSubject.toString(), claims.body[Claims.SUBJECT])

        val claimsMap = jwtTokenHandler.getAllClaimsFromToken(jwtToken)

        assertEquals(expectedEmailClaim, claimsMap[AuthClaims.EMAIL.claimName])
        assertEquals(expectedLoginClaim, claimsMap[AuthClaims.LOGIN.claimName])
        assertEquals(expectedRole, claimsMap[AuthClaims.ROLE.claimName])
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "fasfaga", "   ", "3t9ty90tiwq", "fajkgkaf"])
    fun testGetClaimFromTokenErrorOnBadToken(token: String) {
        assertThrows(JwtTokenException::class.java) { jwtTokenHandler.getClaimFromToken(AuthClaims.LOGIN, token) }
    }
}