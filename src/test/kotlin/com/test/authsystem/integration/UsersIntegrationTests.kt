package com.test.authsystem.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.model.api.StatusResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext


class UsersIntegrationTests
@Autowired
constructor(
    var userRepo: UsersRepository,
    var rolesRepository: RolesRepository,
    var context: WebApplicationContext
) : BaseIntegrationTest(userRepo) {

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        cleanDB()
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun testAddSingleUserSuccess() {
        val expectedLogin = "testLogin"
        val expectedEmail = "testEmail@gmail.com"
        val password = "myTestPassword"
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

        val response = mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response
            .contentAsString
        val statusResponse = jacksonObjectMapper().readValue(response, StatusResponse::class.java)

        Assertions.assertEquals("SUCCESS", statusResponse.status)

        val createdUser = userRepo.findByLoginIgnoreCase(expectedLogin)

        Assertions.assertNotNull(createdUser?.id)
        Assertions.assertEquals(expectedLogin, createdUser?.login)
        Assertions.assertEquals(expectedEmail, createdUser?.email)
        Assertions.assertEquals(expectedBirthday, createdUser?.birthday.toString())
        Assertions.assertTrue(!password.toByteArray().contentEquals(createdUser?.passwordEntity?.passwordHash))
        Assertions.assertNotNull(createdUser?.passwordEntity?.salt)
        Assertions.assertEquals(SystemRoles.USER.name.lowercase(), createdUser?.role?.name?.lowercase())
    }


}