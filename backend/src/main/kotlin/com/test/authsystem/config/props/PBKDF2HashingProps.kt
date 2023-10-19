package com.test.authsystem.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "password-generator.pbkdf2")
data class PBKDF2HashingProps(var iterationCount: Int = 65536,
                              var keyLength: Int = 64,
                              var saltLength: Int = 26)