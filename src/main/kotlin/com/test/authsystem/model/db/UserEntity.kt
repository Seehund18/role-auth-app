package com.test.authsystem.model.db

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users", schema = "auth_system")
data class UserEntity(@Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long?,
                      @Column(nullable = false) var login: String,
                      @Column(nullable = false) var email: String,
                      @Column(nullable = false) var passwordHash: String,
                      @Column(nullable = true) var registrationTimestamp: LocalDateTime?,
                      @Column(nullable = true) var birthday: LocalDate,
                      @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH, CascadeType.PERSIST])
                  @JoinColumn(name = "role_id", nullable = false)
                  @OnDelete(action = OnDeleteAction.SET_NULL)
                  var role: RoleEntity)