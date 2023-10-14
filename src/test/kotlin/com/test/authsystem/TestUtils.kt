package com.test.authsystem

import com.test.authsystem.model.db.PasswordEntity
import com.test.authsystem.model.db.RoleEntity
import com.test.authsystem.model.db.UserEntity
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

private val random = Random(512)

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
        id = random.nextLong(),
        login = login ?: "testLogin",
        email = email ?: "testEmail",
        registrationTimestamp = LocalDateTime.now(),
        birthday = LocalDate.now(),
        role = roleEntity ?: RoleEntity(
            id = random.nextLong(),
            name = "testRole",
            description = "testDesc",
            priorityValue = 100
        ),
        passwordEntity = passEntity ?: PasswordEntity(
            id = random.nextLong(),
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
        id = random.nextLong(),
        name = name ?: "testRole",
        description = description ?: "testDesc",
        priorityValue = priorityValue ?: 100
    )
}

fun generatePassEntity(passHash : String?): PasswordEntity {
    return generatePassEntity(passHash, null)
}

fun generatePassEntity(passHash : String?, salt: String?): PasswordEntity {
    return PasswordEntity(
            id = random.nextLong(),
            passwordHash = passHash?.toByteArray() ?: "someTestHash".toByteArray(),
            salt = salt?.toByteArray() ?: "someSalt".toByteArray()
    )
}

private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun generateRandomString(length: Int) : String {
    return (1..length)
        .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
        .joinToString("")
}
