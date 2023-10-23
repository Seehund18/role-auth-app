package com.test.authsystem.controller.v0

import com.test.authsystem.constants.SystemResponseStatus
import com.test.authsystem.service.AuthService
import com.test.authsystem.service.JwtTokenHandler
import com.test.authsystem.service.UserService
import java.time.LocalDateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest
@ActiveProfiles("test")
class AuthControllerTest
@Autowired
constructor(
    private val mockMvc: MockMvc
) {

    @MockBean
    private lateinit var authService: AuthService

    @MockBean
    private lateinit var userService: UserService

    @MockBean
    private lateinit var jwtTokenHandler: JwtTokenHandler

    @Test
    fun testUserAuthSuccess() {
        val expectedLogin = "testLogin"
        val password = "fasfadfr3t398t"
        val authRequestBody = """
            {
                "login": "$expectedLogin",
                "password": "$password"
            }
        """
        val request = MockMvcRequestBuilders
            .post("/v0/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(authRequestBody)

        whenever(authService.signInUser(any())).thenReturn("some_jwt_token" to LocalDateTime.now().plusDays(1))

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(SystemResponseStatus.SUCCESS.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jwtToken").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.expirationDate").isNotEmpty)
    }

    @ParameterizedTest
    @MethodSource("badAuthUserParameters")
    fun testUserAuthRequestValidationError(login: String?, password: String?) {
        val authRequestBody = """
            {
                "login": ${if (login != null) "\"$login\"" else null},
                "password": ${if (password != null) "\"$password\"" else null}
            }
        """
        val request = MockMvcRequestBuilders
            .post("/v0/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(authRequestBody)

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(SystemResponseStatus.FAILED.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.description").isNotEmpty)
    }

    companion object {
        @JvmStatic
        fun badAuthUserParameters() = listOf(
            Arguments.of(null, "correctPass"),
            Arguments.of("", "correctPass"),
            Arguments.of("   ", "correctPass"),
            Arguments.of("correctLogin", null),
            Arguments.of("correctLogin", ""),
            Arguments.of("correctLogin", "   "),
        )
    }
}