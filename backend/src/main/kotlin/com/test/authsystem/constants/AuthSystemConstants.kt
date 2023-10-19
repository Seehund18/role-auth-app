package com.test.authsystem.constants

enum class SystemRoles {
    ADMIN, REVIEWER, USER
}

enum class AuthClaims(val claimName: String) {
    EMAIL("email"), LOGIN("user"), ROLE("role")
}

enum class SystemResponseStatus {
    SUCCESS, FAILED
}
