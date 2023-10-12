package com.test.authsystem.util

import com.test.authsystem.exception.AuthException

private val bearerTokenRegex = Regex("Bearer (.+)")

fun extractJwtTokenFromHeader(header : String): String {
    return bearerTokenRegex.find(header)?.groups?.get(1)?.value ?: throw AuthException("No authorization token in the request")
}