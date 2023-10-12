package com.test.authsystem.util

import com.test.authsystem.exception.AuthException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class RequestUtilsKtTest {

    @Test
    fun testExtractJwtTokenFromHeaderSuccess() {
        val expectedJwtToken =
            "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhdXRoLXN5c3RlbSIsInN1YiI6IjQiLCJyb2xlIjoiVXNlciIsImlzcyI6ImF1dGgtc3lzdGVtIiwiZXhwIjoxNjk3MTQwODMzLCJ1c2VyIjoibmV3VXNlciIsImVtYWlsIjoibmV3VXNlckBnbWFpbC5jb20ifQ.g2YmPvC_AUgGAIqlqoz-hOGvNSW4aVH6vZ2phR0ECY4"
        val testedAuthHeader = "Bearer $expectedJwtToken"

        val jwtToken = extractJwtTokenFromHeader(testedAuthHeader)

        Assertions.assertEquals(expectedJwtToken, jwtToken)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "Bearer", " Bearer ", "someRandomString", "67463869", " fjdskgfhsk", "vkhsjdvhq "])
    fun testExtractJwtTokenFromHeaderFailed(authHeader: String) {
        Assertions.assertThrows(AuthException::class.java) { extractJwtTokenFromHeader(authHeader) }
    }
}