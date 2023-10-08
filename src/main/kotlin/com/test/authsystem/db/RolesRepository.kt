package com.test.authsystem.db

import com.test.authsystem.model.db.RoleEntity
import org.springframework.cache.annotation.Cacheable

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RolesRepository: CrudRepository<RoleEntity, Long> {

    @Cacheable(cacheNames = ["persistedRole"], key="#roleName")
    fun findByNameIgnoreCase(roleName: String): RoleEntity

    fun findByPriorityValueLessThanEqual(priorityValue: Int): List<RoleEntity>
}