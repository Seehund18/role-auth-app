package com.test.authsystem.db

import com.test.authsystem.generatePassEntity
import com.test.authsystem.generateRoleEntity
import com.test.authsystem.generateUserEntity
import com.test.authsystem.integration.BaseInitializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(initializers = [BaseInitializer::class])
class UsersRepositoryTest
@Autowired
constructor(
    var entityManager: TestEntityManager,
    var rolesRepository: RolesRepository,
    var usersRepository: UsersRepository
) {

    @BeforeEach
    fun setup() {
        usersRepository.deleteAll()
        rolesRepository.deleteAll()
        entityManager.flush()
    }

    @Test
    fun testExistsByLoginIgnoreCaseOrEmail() {
        val roleEntity = generateRoleEntity("user", "Test user", 100)
        val userEntity = generateUserEntity("user", "test@mail.com", roleEntity, generatePassEntity("someHash"))
        entityManager.persist(roleEntity)
        entityManager.persist(userEntity)
        entityManager.flush()

        assertEquals(true, usersRepository.existsByLoginIgnoreCaseOrEmail("user", "test@mail.com"))
        assertEquals(true, usersRepository.existsByLoginIgnoreCaseOrEmail("User", "anotherMail@mail.com"))
        assertEquals(true, usersRepository.existsByLoginIgnoreCaseOrEmail("USER", "anotherMail@mail.com"))
        assertEquals(true, usersRepository.existsByLoginIgnoreCaseOrEmail("user", "anotherMail@mail.com"))
        assertEquals(true, usersRepository.existsByLoginIgnoreCaseOrEmail("anotherUser", "test@mail.com"))
        assertEquals(false, usersRepository.existsByLoginIgnoreCaseOrEmail("anotherUser", "anotherMail@mail.com"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["user", "User", "USER", "UsEr", "uSeR"])
    fun testFindByLoginIgnoreCaseSuccess(login: String) {
        val roleEntity = generateRoleEntity("user", "Test user", 100)
        val expectedUserEntity = generateUserEntity("user", "test@mail.com", roleEntity, generatePassEntity("someHash"))
        entityManager.persist(roleEntity)
        entityManager.persist(expectedUserEntity)
        entityManager.flush()

        val userEntity = usersRepository.findByLoginIgnoreCase(login)

        assertEquals(expectedUserEntity.login, userEntity?.login)
        assertEquals(expectedUserEntity.email, userEntity?.email)
        assertEquals(expectedUserEntity.birthday, userEntity?.birthday)
        assertEquals(expectedUserEntity.registrationTimestamp, userEntity?.registrationTimestamp)
    }
}