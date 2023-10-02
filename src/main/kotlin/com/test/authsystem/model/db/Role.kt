package com.test.authsystem.model.db

import jakarta.persistence.*

@Entity
@Table(name = "roles", schema = "auth_system")
data class Role(@Column(nullable = false) var name: String,
           @Column(nullable = true) var description: String?,
           @Column(nullable = false) var priorityValue: Int,
           @Id @GeneratedValue(strategy = GenerationType.AUTO) var id: String?)