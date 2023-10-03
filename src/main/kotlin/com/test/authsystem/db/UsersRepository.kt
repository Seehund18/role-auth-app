package com.test.authsystem.db

import com.test.authsystem.model.db.UserEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UsersRepository: CrudRepository <UserEntity, Long> {

    fun existsByLoginIgnoreCase(login: String): Boolean
    fun existsByEmail(email: String): Boolean
}