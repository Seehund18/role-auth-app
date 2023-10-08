package com.test.authsystem.service

import com.test.authsystem.config.props.PBKDF2HashingProps
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@Service
class PBKDF2HashingService(var configProps: PBKDF2HashingProps): PassHashingService {

    private val random = SecureRandom()

    private val algorithmName: String = "PBKDF2WithHmacSHA1"

    override fun generateHashedPassAndSalt(passBytes: CharArray): Pair<ByteArray, ByteArray> {

        val salt = ByteArray(configProps.saltLength)
        random.nextBytes(salt)

        val hashedPass = generateHashedPassWithSalt(passBytes, salt)

        return hashedPass to salt
    }

    override fun generateHashedPassWithSalt(passBytes: CharArray, salt: ByteArray) : ByteArray {
        val spec: KeySpec = PBEKeySpec(passBytes, salt, configProps.iterationCount, configProps.keyLength)
        val factory = SecretKeyFactory.getInstance(algorithmName)

        return factory.generateSecret(spec).encoded
    }


}