package com.test.authsystem.model.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "passwords", schema = "auth_system")
data class PasswordEntity(@Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
                          @Column(nullable = false) var passwordHash: ByteArray,
                          @Column(nullable = true) var salt: ByteArray)