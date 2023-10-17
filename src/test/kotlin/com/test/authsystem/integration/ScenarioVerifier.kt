package com.test.authsystem.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.test.authsystem.constants.SystemResponseStatus
import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.generateRandomString
import com.test.authsystem.model.api.AuthResponse
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import org.junit.jupiter.api.Assertions
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class ScenarioVerifier(
    private val mockMvc: MockMvc,
    private val jsonMapper: ObjectMapper,
    private val userRepo: UsersRepository,
    private val rolesRepo: RolesRepository
) {

    private val adminRoleEndpoint = "/v0/roles/admin"
    private val reviewerRoleEndpoint = "/v0/roles/reviewer"
    private val userRoleEndpoint = "/v0/roles/user"

    private val endpointsAndRoles = listOf(
        adminRoleEndpoint to SystemRoles.ADMIN,
        reviewerRoleEndpoint to SystemRoles.REVIEWER,
        userRoleEndpoint to SystemRoles.USER
    )

    fun createAuthChangePasswordScenario() {
        // Create user, auth and success accessing user endpoint
        val userLogin = generateRandomString(10)
        val email = "${generateRandomString(5)}@gmail.com"
        val password = generateRandomString(15)
        verifyCreateUser(userLogin, email, password)
        val userAuth = verifyAuthCall(userLogin, password)
        verifyRoleEndpointCallSuccess(userAuth.jwtToken, userRoleEndpoint)

        val newPass = generateRandomString(15)
        verifyChangePassword(userAuth.jwtToken, userLogin, password, newPass)
        val newUserAuth = verifyAuthCall(userLogin, newPass)
        verifyRoleEndpointCallSuccess(newUserAuth.jwtToken, userRoleEndpoint)
    }

    fun createAuthAccessRandomEndpointScenario(successCount: AtomicInteger, failedCount: AtomicInteger) {
        val userLogin = generateRandomString(10)
        val email = "${generateRandomString(5)}@gmail.com"
        val password = generateRandomString(15)

        verifyCreateUser(userLogin, email, password)
        val authResponse = verifyAuthCall(userLogin, password)
        val userRole = SystemRoles.USER

        val endpointToCall = Random.nextInt(0, endpointsAndRoles.size)
        val endpointAndRole = endpointsAndRoles[endpointToCall]
        val permissionsEnough = checkPriority(endpointAndRole.second, userRole)
        if (permissionsEnough) {
            verifyRoleEndpointCallSuccess(authResponse.jwtToken, endpointAndRole.first)
            successCount.incrementAndGet()
        } else {
            verifyRoleEndpointCallForbidden(authResponse.jwtToken, endpointAndRole.first)
            failedCount.incrementAndGet()
        }
    }

    private fun checkPriority(endpointRole: SystemRoles, userRole: SystemRoles): Boolean {
        val endpointRoleEntity =
            rolesRepo.findByNameIgnoreCase(endpointRole.name) ?: throw RuntimeException("$endpointRole wasn't found")
        return rolesRepo.findByPriorityValueLessThanEqual(endpointRoleEntity.priorityValue)
            .map { roleEntity -> roleEntity.name.lowercase() }
            .contains(userRole.name.lowercase())
    }

    fun createAuthChangeRoleScenario(
        adminJwtToken: String,
        endpointAndCalls: ConcurrentHashMap<String, AtomicInteger>
    ) {
        // Getting random endpoint except user endpoint
        val endpointAndRole = endpointsAndRoles[Random.nextInt(0, endpointsAndRoles.size - 1)]
        val testedEndpoint = endpointAndRole.first
        val neededRole = endpointAndRole.second

        // Create user, auth and fail to access admin endpoint
        val userLogin = generateRandomString(10)
        val email = "${generateRandomString(5)}@gmail.com"
        val password = generateRandomString(15)
        verifyCreateUser(userLogin, email, password)
        val userAuth = verifyAuthCall(userLogin, password)
        verifyRoleEndpointCallForbidden(userAuth.jwtToken, testedEndpoint)

        verifyPromoteUser(adminJwtToken, userLogin, neededRole.name)
        verifyRoleEndpointCallSuccess(userAuth.jwtToken, testedEndpoint)

        endpointAndCalls.compute(testedEndpoint) { _, count ->
            if (count == null) {
                return@compute AtomicInteger(1)
            } else {
                count.incrementAndGet()
                return@compute count
            }
        }
    }

    fun verifyCreateUser(login: String?, email: String?, password: String?) {
        val expectedLogin = login ?: generateRandomString(10)
        val expectedEmail = email ?: "${generateRandomString(5)}@gmail.com"
        val requestPassword = password ?: generateRandomString(15)
        val expectedBirthday = "1995-09-02"

        val createUserRequestBody = """
            {
                "login": "$expectedLogin",
                "email": "$expectedEmail",
                "password": "$requestPassword",
                "birthday": "$expectedBirthday"
            }
        """
        val request = MockMvcRequestBuilders
            .post("/v0/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createUserRequestBody)

        mockMvc.perform(request)
            .andExpect {
                MockMvcResultMatchers.status().isCreated
                MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
                MockMvcResultMatchers.jsonPath("$.status").value(SystemResponseStatus.SUCCESS.name)
                MockMvcResultMatchers.jsonPath("$.description").isNotEmpty
            }

        val createdUser = userRepo.findByLoginIgnoreCase(expectedLogin)
        Assertions.assertNotNull(createdUser?.id)
        Assertions.assertEquals(expectedLogin, createdUser?.login)
        Assertions.assertEquals(expectedEmail, createdUser?.email)
        Assertions.assertEquals(expectedBirthday, createdUser?.birthday.toString())
        Assertions.assertTrue(!requestPassword.toByteArray().contentEquals(createdUser?.passwordEntity?.passwordHash))
        Assertions.assertNotNull(createdUser?.passwordEntity?.salt)
        Assertions.assertEquals(SystemRoles.USER.name.lowercase(), createdUser?.role?.name?.lowercase())
    }

    fun verifyAuthCall(login: String, password: String): AuthResponse {
        val authUserRequestBody = """
            {
                "login": "$login",
                "password": "$password"
            }
        """

        val request = MockMvcRequestBuilders
            .post("/v0/users/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(authUserRequestBody)

        val response = mockMvc.perform(request)
            .andExpect {
                MockMvcResultMatchers.status().isOk
                MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
                MockMvcResultMatchers.jsonPath("$.status").value(SystemResponseStatus.SUCCESS.name)
                MockMvcResultMatchers.jsonPath("$.jwtToken").isNotEmpty
                MockMvcResultMatchers.jsonPath("$.expirationDate").isNotEmpty
            }
            .andReturn()
            .response
            .contentAsString
        return jsonMapper.readValue(response, AuthResponse::class.java)
    }

    private fun verifyRoleEndpointCallSuccess(jwtToken: String, endpoint: String) {
        val request = MockMvcRequestBuilders
            .get(endpoint)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")

        mockMvc.perform(request)
            .andExpect {
                MockMvcResultMatchers.status().isOk
                MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
                MockMvcResultMatchers.jsonPath("$.status").value(SystemResponseStatus.SUCCESS.name)
                MockMvcResultMatchers.jsonPath("$.description").isNotEmpty
            }
            .andReturn()
    }

    private fun verifyRoleEndpointCallForbidden(jwtToken: String, endpoint: String) {
        val request = MockMvcRequestBuilders
            .get(endpoint)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")

        mockMvc.perform(request)
            .andExpect {
                MockMvcResultMatchers.status().isForbidden
                MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
                MockMvcResultMatchers.jsonPath("$.status").value(SystemResponseStatus.FAILED.name)
                MockMvcResultMatchers.jsonPath("$.description").isNotEmpty
            }
            .andReturn()
    }

    private fun verifyPromoteUser(adminJwtToken: String, login: String, newRole: String) {
        val updateAuthRequestBody = """
            {
                "newRole": "$newRole",
            }
        """
        val request = MockMvcRequestBuilders
            .put("/v0/users/$login/role")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $adminJwtToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateAuthRequestBody)

        mockMvc.perform(request)
            .andExpect {
                MockMvcResultMatchers.status().isOk
                MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
                MockMvcResultMatchers.jsonPath("$.status").value(SystemResponseStatus.SUCCESS.name)
                MockMvcResultMatchers.jsonPath("$.description").isNotEmpty
            }
            .andReturn()
    }

    private fun verifyChangePassword(userJwtToken: String, login: String, oldPass: String, newPass: String) {
        val changePassRequestBody = """
            {
                "oldPass": "$oldPass",
                "newPass": "$newPass"
            }
        """
        val request = MockMvcRequestBuilders
            .put("/v0/users/$login/password")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $userJwtToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(changePassRequestBody)

        mockMvc.perform(request)
            .andExpect {
                MockMvcResultMatchers.status().isOk
                MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
                MockMvcResultMatchers.jsonPath("$.status").value(SystemResponseStatus.SUCCESS.name)
                MockMvcResultMatchers.jsonPath("$.description").isNotEmpty
            }
            .andReturn()
    }
}