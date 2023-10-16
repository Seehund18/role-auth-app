package com.test.authsystem.db

import com.test.authsystem.generateRoleEntity
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
class RolesRepositoryTest
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

    @ParameterizedTest
    @ValueSource(strings = ["user", "User", "USER", "UsEr", "uSeR"])
    fun testFindByNameIgnoreCaseSuccess(nameToFind: String) {
        val roleEntity = generateRoleEntity("User", "Test user", 100)
        entityManager.persistAndFlush(roleEntity)

        val foundRole = rolesRepository.findByNameIgnoreCase(nameToFind)

        assertEquals(roleEntity.name.lowercase(), foundRole?.name?.lowercase())
        assertEquals(roleEntity.description, foundRole?.description)
        assertEquals(roleEntity.priorityValue, foundRole?.priorityValue)
    }

    @Test
    fun testFindByPriorityValueLessThanEqual() {
        val roleEntity1 = generateRoleEntity("role1", "Test user", 1)
        val roleEntity2 = generateRoleEntity("role2", "Test user", 10)
        val roleEntity3 = generateRoleEntity("role3", "Test user", 100)
        val roleEntity4 = generateRoleEntity("role4", "Test user", 200)
        entityManager.persist(roleEntity1)
        entityManager.persist(roleEntity2)
        entityManager.persist(roleEntity3)
        entityManager.persist(roleEntity4)
        entityManager.flush()

        assertEquals(0, rolesRepository.findByPriorityValueLessThanEqual(0).size)
        assertEquals(1, rolesRepository.findByPriorityValueLessThanEqual(1).size)
        assertEquals(2, rolesRepository.findByPriorityValueLessThanEqual(10).size)
        assertEquals(3, rolesRepository.findByPriorityValueLessThanEqual(100).size)
        assertEquals(4, rolesRepository.findByPriorityValueLessThanEqual(200).size)
        assertEquals(4, rolesRepository.findByPriorityValueLessThanEqual(300).size)
    }
}