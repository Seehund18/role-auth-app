package com.test.authsystem.controller.v0

import com.test.authsystem.constants.AuthClaims
import com.test.authsystem.constants.SystemResponseStatus
import com.test.authsystem.exception.DuplicateException
import com.test.authsystem.exception.NoEntityWasFound
import com.test.authsystem.exception.PassDoesntMatchException
import com.test.authsystem.exception.UsersDontMatchException
import com.test.authsystem.generateRoleEntity
import com.test.authsystem.generateUserEntity
import com.test.authsystem.model.api.Endpoint
import com.test.authsystem.service.AuthService
import com.test.authsystem.service.JwtTokenHandler
import com.test.authsystem.service.UserService
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ActiveProfiles("test")
internal class UsersControllerTest
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
    fun testGetUserSuccess() {
        val expectedLogin = "testLogin"
        val expectedEmail = "testEmail@gmail.com"
        val expectedRole = "testRole"
        val expectedUserEntity = generateUserEntity(
            login = expectedLogin,
            email = expectedEmail,
            passEntity = null,
            roleEntity = generateRoleEntity(name = expectedRole)
        )
        val expectedEndpoints = listOf(
            Endpoint(
                description = "stubEndpoint",
                url = "someUrl"
            ), Endpoint(
                description = "stubEndpoint2",
                url = "someUrl2"
            )
        )

        val jwtToken = "dumb_token"
        whenever(userService.getUserInfo(eq(expectedLogin)))
            .thenReturn(expectedUserEntity to expectedEndpoints)
        whenever(jwtTokenHandler.getClaimFromToken(eq(AuthClaims.LOGIN), eq(jwtToken))).thenReturn(expectedLogin)

        val request = MockMvcRequestBuilders
            .get("/v0/users/$expectedLogin")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.login").value(expectedLogin))
            .andExpect(jsonPath("$.email").value(expectedEmail))
            .andExpect(jsonPath("$.birthday").isNotEmpty)
            .andExpect(jsonPath("$.registrationDate").isNotEmpty)
            .andExpect(jsonPath("$.role").value(expectedRole))
            .andExpect(jsonPath("$.endpoints[*]").isArray)
            .andExpect(jsonPath("$.endpoints[0].url").value("someUrl"))
            .andExpect(jsonPath("$.endpoints[0].description").value("stubEndpoint"))
            .andExpect(jsonPath("$.endpoints[1].url").value("someUrl2"))
            .andExpect(jsonPath("$.endpoints[1].description").value("stubEndpoint2"))
    }

    @Test
    fun testGetUserErrorOnTokenAbsence() {
        val expectedLogin = "testLogin"

        val request = MockMvcRequestBuilders
            .get("/v0/users/$expectedLogin")

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(SystemResponseStatus.FAILED.name))
            .andExpect(jsonPath("$.description").isNotEmpty)
    }

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

        whenever(userService.createNewUser(any())).thenReturn(generateUserEntity(expectedLogin, expectedEmail, null))

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(SystemResponseStatus.SUCCESS.name))
            .andExpect(jsonPath("$.description").isNotEmpty)
    }

    @ParameterizedTest
    @MethodSource("badAddUserParameters")
    fun testAddUserRequestValidationError(login: String?, email: String?, password: String?, birthday: String?) {
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
            .andExpect(jsonPath("$.description").isNotEmpty)
    }

    @ParameterizedTest
    @MethodSource("internalExceptionAndStatus")
    fun testControllerExceptionHandling(exClass: Class<Exception>, status: HttpStatus) {
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

        whenever(userService.createNewUser(any())).thenThrow(exClass)

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().`is`(status.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(SystemResponseStatus.FAILED.name))
            .andExpect(jsonPath("$.description").isNotEmpty)
    }

    @Test
    fun testChangeUserPasswordSuccess() {
        val user = "newUser"
        val oldPass = "oldPass"
        val newPass = "newPass"

        val changePassRequestBody = """
            {
                "oldPass": "$oldPass",
                "newPass": "$newPass"
            }
        """
        val jwtToken = "someStubJwtToken"
        val request = MockMvcRequestBuilders
            .post("/v0/users/$user/password")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(changePassRequestBody)

        whenever(userService.changePassword(any(), any())).thenReturn(null)
        whenever(jwtTokenHandler.getClaimFromToken(eq(AuthClaims.LOGIN), eq(jwtToken))).thenReturn(user)

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(SystemResponseStatus.SUCCESS.name))
            .andExpect(jsonPath("$.description").isNotEmpty)
    }

    @ParameterizedTest
    @NullAndEmptySource
    fun testChangeUserPasswordRequestValidationError(newPass: String?) {
        val user = "newUser"
        val oldPass = "oldPass"

        val changePassRequestBody = """
            {
                "oldPass": "$oldPass",
                "newPass": ${if (newPass != null) "\"$newPass\"" else null}
            }
        """
        val jwtToken = "someStubJwtToken"
        val request = MockMvcRequestBuilders
            .post("/v0/users/$user/password")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(changePassRequestBody)

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(SystemResponseStatus.FAILED.name))
            .andExpect(jsonPath("$.description").isNotEmpty)
    }

    @Test
    fun testChangeUserPasswordErrorOnTokenAbsence() {
        val user = "newUser"
        val changePassRequestBody = """
            {
                "oldPass": "oldPass",
                "newPass": "newPass"
            }
        """
        val request = MockMvcRequestBuilders
            .post("/v0/users/$user/password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(changePassRequestBody)

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(SystemResponseStatus.FAILED.name))
            .andExpect(jsonPath("$.description").isNotEmpty)
    }

    @Test
    fun testChangeUserRoleSuccess() {
        val user = "newUser"

        val changeRoleRequestBody = """
            {
                "newRole": "reviewer"
            }
        """
        val jwtToken = "someStubJwtToken"
        val request = MockMvcRequestBuilders
            .put("/v0/users/$user/role")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(changeRoleRequestBody)

        whenever(userService.changeUserRole(any(), any())).thenReturn(null)

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(SystemResponseStatus.SUCCESS.name))
            .andExpect(jsonPath("$.description").isNotEmpty)
    }

    @ParameterizedTest
    @NullAndEmptySource
    fun testChangeUserRoleBadRequestError(newRole: String?) {
        val user = "newUser"

        val changeRoleRequestBody = """
            {
                "newRole": ${if (newRole != null) "\"$newRole\"" else null}
            }
        """
        val jwtToken = "someStubJwtToken"
        val request = MockMvcRequestBuilders
            .put("/v0/users/$user/role")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(changeRoleRequestBody)

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(SystemResponseStatus.FAILED.name))
            .andExpect(jsonPath("$.description").isNotEmpty)
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
            Arguments.of("correctLogin", "correctEmail@gmail.com", "correctPass", "2100-10-14")
        )

        @JvmStatic
        fun internalExceptionAndStatus() = listOf(
            Arguments.of(DuplicateException::class.java, HttpStatus.CONFLICT),
            Arguments.of(NoEntityWasFound::class.java, HttpStatus.BAD_REQUEST),
            Arguments.of(PassDoesntMatchException::class.java, HttpStatus.BAD_REQUEST),
            Arguments.of(UsersDontMatchException::class.java, HttpStatus.FORBIDDEN),
            Arguments.of(RuntimeException::class.java, HttpStatus.INTERNAL_SERVER_ERROR)
        )

    }
}