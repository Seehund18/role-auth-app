package com.test.authsystem.db

import com.test.authsystem.model.db.UserEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UsersRepository: CrudRepository <UserEntity, Long> {

    fun existsByLoginIgnoreCaseOrEmail(login: String, email: String): Boolean
    fun findByLoginIgnoreCase(name: String): UserEntity?
}