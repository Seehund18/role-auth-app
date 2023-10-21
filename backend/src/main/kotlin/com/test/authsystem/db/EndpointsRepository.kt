package com.test.authsystem.db

import com.test.authsystem.model.db.EndpointsEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EndpointsRepository: CrudRepository<EndpointsEntity, Long>