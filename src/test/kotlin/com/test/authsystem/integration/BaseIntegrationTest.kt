package com.test.authsystem.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.generatePassEntity
import com.test.authsystem.generateUserEntity
import com.test.authsystem.model.db.UserEntity
import com.test.authsystem.service.PassHashingService
import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(initializers = [BaseInitializer::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BaseIntegrationTest(
    private var userRepo: UsersRepository,
    private var rolesRepo: RolesRepository,
    private val passHashingService: PassHashingService
) {

    @Transactional
    protected fun cleanDB() {
        userRepo.deleteAll()
    }

    fun createAdmin(adminLogin: String, password: String): UserEntity {
        val adminRoleEntity = rolesRepo.findByNameIgnoreCase(SystemRoles.ADMIN.name)
        val (passHash, salt) = passHashingService.generateHashedPassAndSalt(password.toCharArray())
        val userEntity = generateUserEntity(
            login = adminLogin,
            email = "test@mail.com",
            roleEntity = adminRoleEntity,
            passEntity = generatePassEntity(passHash, salt),
        )

        return userRepo.save(userEntity)
    }

    @Bean
    fun jacksonMapper(): ObjectMapper {
        return jsonMapper {
            addModule(kotlinModule())
            addModule(JavaTimeModule())
        }
    }
}