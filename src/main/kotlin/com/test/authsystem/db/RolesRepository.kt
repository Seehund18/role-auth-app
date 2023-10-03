package com.test.authsystem.db

import com.test.authsystem.model.db.RoleEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RolesRepository: CrudRepository<RoleEntity, Long> {

    fun findByNameIgnoreCase(roleName: String): RoleEntity
}