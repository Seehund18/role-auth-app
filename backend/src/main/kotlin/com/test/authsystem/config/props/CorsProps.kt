package com.test.authsystem.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("cors")
class CorsProps(val allowedOrigins: List<String> = emptyList())