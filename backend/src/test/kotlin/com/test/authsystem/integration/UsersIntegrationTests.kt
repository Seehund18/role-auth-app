package com.test.authsystem.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.service.PassHashingService
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KLogger
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

class UsersIntegrationTests
@Autowired
constructor(
    private var userRepo: UsersRepository,
    rolesRepository: RolesRepository,
    passHashingService: PassHashingService,
    context: WebApplicationContext,
    jsonMapper: ObjectMapper
) : BaseIntegrationTest(userRepo, rolesRepository, passHashingService) {

    private var mockMvc: MockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    private var scenarioVerifier: ScenarioVerifier = ScenarioVerifier(mockMvc, jsonMapper, userRepo, rolesRepository)
    private val log: KLogger = KotlinLogging.logger {}

    @BeforeEach
    fun setup() {
        cleanDB()
    }

    @Test
    fun contextLoads() {
    }

    @Test
    @DisplayName("Create users scenario - create multiple users")
    fun testAddMultipleUsersInParallelSuccess() = runBlocking {
        val jobs = mutableListOf<Deferred<Any>>()
        repeat(10) {
            jobs += async(Dispatchers.IO) {
                scenarioVerifier.verifyCreateUser(null, null, null)
            }
        }
        jobs.awaitAll()

        Assertions.assertEquals(10, userRepo.count())
    }

    @Test
    @DisplayName(
        """
            Create user and access random endpoint scenario - create multiple users, authenticate and
            |then access a random endpoint
            """
    )
    fun testCreateAuthAccessRandomEndpointInParallelSuccess() = runBlocking {
        val jobs = mutableListOf<Deferred<Any>>()
        val successCalls = AtomicInteger()
        val failedCalls = AtomicInteger()

        repeat(10) {
            jobs += async(Dispatchers.IO) {
                scenarioVerifier.createAuthAccessRandomEndpointScenario(successCalls, failedCalls)
            }
        }
        jobs.awaitAll()

        log.info { "Number of success calls ${successCalls.get()} and failed calls ${failedCalls.get()}" }

        Assertions.assertEquals(10, userRepo.count())
        Assertions.assertEquals(10, successCalls.get() + failedCalls.get())
    }

    @Test
    @DisplayName(
        """
            Change role scenario - create multiple users, authenticate, try to access forbidden endpoint and
            |then change his role via admin and access the same endpoint again
            """
    )
    fun testChangeUserRoleInParallelSuccess() = runBlocking {
        val adminLogin = "login"
        val adminPass = "adminPass"
        createAdmin(adminLogin, adminPass)
        val adminAuth = scenarioVerifier.verifyAuthCall(adminLogin, adminPass)

        val jobs = mutableListOf<Deferred<Any>>()
        val endpointCalls = ConcurrentHashMap<String, AtomicInteger>()
        repeat(10) {
            jobs += async(Dispatchers.IO) {
                scenarioVerifier.createAuthChangeRoleScenario(adminAuth.jwtToken, endpointCalls)
            }
        }
        jobs.awaitAll()

        log.info { "Calls to endpoints: $endpointCalls" }
        Assertions.assertEquals(10, endpointCalls.values.sumOf { atomicInt -> atomicInt.get() })
        Assertions.assertEquals(11, userRepo.count())
    }

    @Test
    @DisplayName(
        """
            Change password scenario - create multiple users, authenticate, access endpoint
            |then change password and try to authorise with a new password
            """
    )
    fun testChangePasswordInParallelSuccess() = runBlocking {
        val jobs = mutableListOf<Deferred<Any>>()
        repeat(10) {
            jobs += async(Dispatchers.IO) {
                scenarioVerifier.createAuthChangePasswordScenario()
            }
        }
        jobs.awaitAll()

        Assertions.assertEquals(10, userRepo.count())
    }
}
