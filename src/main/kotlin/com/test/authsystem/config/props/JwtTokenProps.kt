package com.test.authsystem.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "jwt.token")
data class JwtTokenProps(
    var issuer: String,
    var lifespan: Duration,
    var secret: String
)