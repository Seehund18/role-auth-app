package com.test.authsystem

import com.test.authsystem.model.db.PasswordEntity
import com.test.authsystem.model.db.RoleEntity
import com.test.authsystem.model.db.UserEntity
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

fun generateUserEntity(login: String?, email: String?, passEntity: PasswordEntity?): UserEntity {
    return generateUserEntity(login, email, null, passEntity)
}

fun generateUserEntity(
    login: String?,
    email: String?,
    roleEntity: RoleEntity?,
    passEntity: PasswordEntity?
): UserEntity {
    return UserEntity(
        id = null,
        login = login ?: "testLogin",
        email = email ?: "testEmail",
        registrationTimestamp = LocalDateTime.now(),
        birthday = LocalDate.now(),
        role = roleEntity ?: RoleEntity(
            id = null,
            name = "testRole",
            description = "testDesc",
            priorityValue = 100
        ),
        passwordEntity = passEntity ?: PasswordEntity(
            id = null,
            passwordHash = "someTestHash".toByteArray(),
            salt = "someSalt".toByteArray()
        )
    )
}

fun generateRoleEntity(): RoleEntity {
    return generateRoleEntity(null, null, null)
}

fun generateRoleEntity(name: String?): RoleEntity {
    return generateRoleEntity(name, null, null)
}

fun generateRoleEntity(name: String?, description: String?, priorityValue: Int?): RoleEntity {
    return RoleEntity(
        id = null,
        name = name ?: "testRole",
        description = description ?: "testDesc",
        priorityValue = priorityValue ?: 100
    )
}

fun generatePassEntity(passHash: String?): PasswordEntity {
    return generatePassEntity(passHash, null)
}

fun generatePassEntity(passHash: String?, salt: String?): PasswordEntity {
    return generatePassEntity(passHash?.toByteArray(), salt?.toByteArray())
}

fun generatePassEntity(passHash: ByteArray?, salt: ByteArray?): PasswordEntity {
    return PasswordEntity(
        id = null,
        passwordHash = passHash ?: "someTestHash".toByteArray(),
        salt = salt ?: "someSalt".toByteArray()
    )
}

private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun generateRandomString(length: Int): String {
    return (1..length)
        .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
        .joinToString("")
}
