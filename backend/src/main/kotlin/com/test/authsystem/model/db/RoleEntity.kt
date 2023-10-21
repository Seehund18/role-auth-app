package com.test.authsystem.model.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table


@Entity
@Table(name = "roles", schema = "auth_system")
class RoleEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(nullable = false) var name: String,
    @Column(nullable = true) var description: String?,
    @Column(nullable = false) var priorityValue: Int,

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "min_role_id", nullable = true)
    var endpointsList: List<EndpointsEntity> = emptyList()
)