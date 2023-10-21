package com.test.authsystem.constants

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class SystemRolesTest {

    @ParameterizedTest
    @MethodSource("correctValues")
    fun testParseSuccess(value : String, expectedSystemRole: SystemRoles) {
        assertEquals(expectedSystemRole, SystemRoles.parse(value))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "someRandom", "  admin", ])
    fun testParseError(value : String?) {
        assertThrows(NoSuchElementException::class.java) {
            SystemRoles.parse(value)
        }
    }

    companion object {
        @JvmStatic
        fun correctValues() = listOf(
            // User role on the left and min required role on the right
            Arguments.of("user", SystemRoles.USER),
            Arguments.of("USER", SystemRoles.USER),
            Arguments.of("User", SystemRoles.USER),
            Arguments.of("UsEr", SystemRoles.USER),
            Arguments.of("reviewer", SystemRoles.REVIEWER),
            Arguments.of("REVIEWER", SystemRoles.REVIEWER),
            Arguments.of("Reviewer", SystemRoles.REVIEWER),
            Arguments.of("ReVIEwer", SystemRoles.REVIEWER),
            Arguments.of("admin", SystemRoles.ADMIN),
            Arguments.of("ADMIN", SystemRoles.ADMIN),
            Arguments.of("Admin", SystemRoles.ADMIN),
            Arguments.of("AdMIn", SystemRoles.ADMIN)
        )
    }
}