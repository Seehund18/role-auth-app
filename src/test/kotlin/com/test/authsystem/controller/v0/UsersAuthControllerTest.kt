package com.test.authsystem.controller.v0

import com.test.authsystem.constants.SystemResponseStatus
import com.test.authsystem.exception.DuplicateException
import com.test.authsystem.generateUserEntity
import com.test.authsystem.service.AuthService
import com.test.authsystem.service.JwtTokenHandler
import com.test.authsystem.service.UserModificationService
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
internal class UsersAuthControllerTest(@Autowired private val mockMvc : MockMvc) {

    @MockBean
    private lateinit var authService: AuthService
    @MockBean
    private lateinit var userModificationService: UserModificationService
    @MockBean
    private lateinit var jwtTokenHandler: JwtTokenHandler

    @Test
    fun testAddUserSuccess() {
        val expectedLogin = "testLogin"
        val expectedEmail = "testEmail@gmail.com"
        val password = "fasfadfr3t398t"
        val expectedBirthday = "1995-09-02"
        val createUserRequestBody = """
            {
                "login": "$expectedLogin",
                "email": "$expectedEmail",
                "password": "$password",
                "birthday": "$expectedBirthday"
            }
        """
        val request = MockMvcRequestBuilders
            .post("/v0/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createUserRequestBody)

        whenever(authService.signUpNewUser(any())).thenReturn(generateUserEntity(expectedLogin, expectedEmail, null))

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(SystemResponseStatus.SUCCESS.name))
            .andExpect(jsonPath("$.description").isNotEmpty);
    }

    @ParameterizedTest
    @MethodSource("badAddUserParameters")
    fun testAddUserRequestValidationError(login : String?, email : String?, password : String?, birthday : String?) {
        val createUserRequestBody = """
            {
                "login": ${if (login != null) "\"$login\"" else null},
                "email": ${if (email != null) "\"$email\"" else null},
                "password": ${if (password != null) "\"$password\"" else null},
                "birthday": ${if (birthday != null) "\"$birthday\"" else null}
            }
        """
        val request = MockMvcRequestBuilders
            .post("/v0/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createUserRequestBody)

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(SystemResponseStatus.FAILED.name))
            .andExpect(jsonPath("$.description").isNotEmpty);
    }

    @ParameterizedTest
    @MethodSource("internalExceptionAndStatus")
    fun testAddUserErrorOnDuplicate(exClass : Class<Exception>, status: HttpStatus) {
        val createUserRequestBody = """
            {
                "login": "testLogin",
                "email": "testEmail@gmail.com",
                "password": "correctpassword",
                "birthday": "1990-10-05"
            }
        """
        val request = MockMvcRequestBuilders
            .post("/v0/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createUserRequestBody)

        whenever(authService.signUpNewUser(any())).thenThrow(exClass)

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().`is`(status.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(SystemResponseStatus.FAILED.name))
            .andExpect(jsonPath("$.description").isNotEmpty);
    }

    companion object {
        @JvmStatic
        fun badAddUserParameters() = listOf(
            Arguments.of(null, "correctEmail@gmail.com", "correctPass", "1995-09-02"),
            Arguments.of("", "correctEmail@gmail.com", "correctPass", "1995-09-02"),
            Arguments.of("   ", "correctEmail@gmail.com", "correctPass", "1995-09-02"),
            Arguments.of("correctLogin", null, "correctPass", "1995-09-02"),
            Arguments.of("correctLogin", "", "correctPass", "1995-09-02"),
            Arguments.of("correctLogin", "    ", "correctPass", "1995-09-02"),
            Arguments.of("correctLogin", "correctEmail@gmail.com", null, "1995-09-02"),
            Arguments.of("correctLogin", "correctEmail@gmail.com", "", "1995-09-02"),
            Arguments.of("correctLogin", "correctEmail@gmail.com", "   ", "1995-09-02"),
            Arguments.of("correctLogin", "correctEmail@gmail.com", "correctPass", null),
            Arguments.of("correctLogin", "correctEmail@gmail.com", "correctPass", ""),
            Arguments.of("correctLogin", "correctEmail@gmail.com", "correctPass", "    "),
            Arguments.of("correctLogin", "correctEmail@gmail.com", "correctPass", "2100-10-14")
        )

        @JvmStatic
        fun internalExceptionAndStatus() = listOf(
            Arguments.of(DuplicateException::class.java, HttpStatus.CONFLICT),
            Arguments.of(RuntimeException::class.java, HttpStatus.INTERNAL_SERVER_ERROR)
        )

    }

    @Test
    fun authUser() {
    }

    @Test
    fun changePassword() {
    }

    @Test
    fun changeRole() {
    }
}