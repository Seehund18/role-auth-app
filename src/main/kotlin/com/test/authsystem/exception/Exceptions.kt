package com.test.authsystem.exception

class DuplicateException(message: String) : RuntimeException(message)

class PassDoesntMatchException(message: String) : RuntimeException(message)

class AuthException(message: String) : RuntimeException(message)

class JwtTokenException(message: String?, cause: Throwable?) : RuntimeException(message, cause)

class NotEnoughPermissionsException(message: String) : RuntimeException(message)

class UsersDontMatchException(message: String) : RuntimeException(message)