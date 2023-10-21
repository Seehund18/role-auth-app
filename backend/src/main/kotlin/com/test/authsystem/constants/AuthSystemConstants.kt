package com.test.authsystem.constants

enum class SystemRoles {
    ADMIN, REVIEWER, USER;
    companion object {
        fun parse(value : String?): SystemRoles {
            return SystemRoles.entries
                .lastOrNull { authClaim -> authClaim.name.lowercase() == value?.lowercase() }
                ?: throw NoSuchElementException("No system role was found for $value")
        }
    }
}

enum class AuthClaims(val claimName: String) {
    EMAIL("email"), LOGIN("user"), ROLE("role")
}

enum class SystemResponseStatus {
    SUCCESS, FAILED
}
