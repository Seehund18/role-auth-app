package com.test.authsystem.exception

class DuplicateException(message: String) : RuntimeException(message)

class PassDoesntMatchException(message: String) : RuntimeException(message)

class AuthException(message: String) : RuntimeException(message)

class JwtTokenException : RuntimeException {
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(message: String?) : super(message)
}

class NoEntityWasFound(message: String) : RuntimeException(message)

class NotEnoughPermissionsException(message: String) : RuntimeException(message)

class UsersDontMatchException(message: String) : RuntimeException(message)