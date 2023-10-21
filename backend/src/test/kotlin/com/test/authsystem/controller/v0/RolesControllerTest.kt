package com.test.authsystem.controller.v0

import com.test.authsystem.aop.AuthorizationAspect
import com.test.authsystem.constants.SystemResponseStatus
import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.exception.NotEnoughPermissionsException
import com.test.authsystem.service.AuthService
import com.test.authsystem.service.JwtTokenHandler
import com.test.authsystem.service.UserModificationService
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest
@Import(value = [AopAutoConfiguration::class, AuthorizationAspect::class])
internal class RolesControllerTest
@Autowired
constructor(
    private val mockMvc: MockMvc
) {

    @MockBean
    private lateinit var authService: AuthService

    @MockBean
    private lateinit var userModificationService: UserModificationService

    @MockBean
    private lateinit var jwtTokenHandler: JwtTokenHandler

    @Test
    fun testAdminEndpointSuccess() {
        val jwtToken = "someStubJwtToken"
        val request = MockMvcRequestBuilders
            .get("/v0/roles/admin")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")

        whenever(authService.authorizeRequest(eq(jwtToken), eq(SystemRoles.ADMIN))).thenReturn(emptyMap())

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(SystemResponseStatus.SUCCESS.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.description").isNotEmpty)
        verify(authService).authorizeRequest(eq(jwtToken), eq(SystemRoles.ADMIN))
    }

    @Test
    fun testReviewerEndpointSuccess() {
        val jwtToken = "someStubJwtToken"
        val request = MockMvcRequestBuilders
            .get("/v0/roles/reviewer")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")

        whenever(authService.authorizeRequest(eq(jwtToken), eq(SystemRoles.ADMIN))).thenReturn(emptyMap())

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(SystemResponseStatus.SUCCESS.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.description").isNotEmpty)
        verify(authService).authorizeRequest(eq(jwtToken), eq(SystemRoles.REVIEWER))
    }

    @Test
    fun testUserEndpointSuccess() {
        val jwtToken = "someStubJwtToken"
        val request = MockMvcRequestBuilders
            .get("/v0/roles/user")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")

        whenever(authService.authorizeRequest(eq(jwtToken), eq(SystemRoles.ADMIN))).thenReturn(emptyMap())

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(SystemResponseStatus.SUCCESS.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.description").isNotEmpty)
        verify(authService).authorizeRequest(eq(jwtToken), eq(SystemRoles.USER))
    }

    @Test
    fun testEndpointErrorOnLackOfPermissions() {
        val jwtToken = "someStubJwtToken"
        val request = MockMvcRequestBuilders
            .get("/v0/roles/admin")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")

        doThrow(NotEnoughPermissionsException::class).whenever(authService).authorizeRequest(eq(jwtToken), any())

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(SystemResponseStatus.FAILED.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.description").isNotEmpty)
        verify(authService).authorizeRequest(eq(jwtToken), any())
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "Bearer ", "Basic"])
    fun testEndpointErrorOnMalformedAuth(authHeader : String?) {
        val request = MockMvcRequestBuilders
            .get("/v0/roles/admin")
            .header(HttpHeaders.AUTHORIZATION, authHeader)

        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(SystemResponseStatus.FAILED.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.description").isNotEmpty)
        verifyNoInteractions(authService)
    }
}