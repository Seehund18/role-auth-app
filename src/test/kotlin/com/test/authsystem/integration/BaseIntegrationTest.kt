package com.test.authsystem.integration

import com.test.authsystem.db.UsersRepository
import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(initializers = [BaseInitializer::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BaseIntegrationTest(private var userRepo: UsersRepository) {

    @Transactional
    protected fun cleanDB() {
        userRepo.deleteAll()
    }

}