package com.test.authsystem.db

import com.test.authsystem.model.db.Role
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RolesRepository: CrudRepository<Role, Int> {

    fun findByName(roleName: String): Role
}