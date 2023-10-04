package com.test.authsystem

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@ConfigurationPropertiesScan
@SpringBootApplication
class AuthSystemApplication

fun main(args: Array<String>) {
	runApplication<AuthSystemApplication>(*args)
}
