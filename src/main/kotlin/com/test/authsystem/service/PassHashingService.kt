package com.test.authsystem.service

interface PassHashingService {

    fun generateHashedPassAndSalt(passBytes: CharArray): Pair<ByteArray, ByteArray>
}