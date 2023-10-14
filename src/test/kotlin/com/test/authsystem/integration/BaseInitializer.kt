package com.test.authsystem.integration

import java.io.File
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.io.ClassPathResource
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait

open class BaseInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    companion object {
        class KDockerComposeContainer(file: File) : DockerComposeContainer<KDockerComposeContainer>(file)

        class Container(
            val serviceName: String,
            val containerPort: Int,
            val localPort: Int
        )

        private val POSTGRES = Container("postgres", 5432, 5450)

        var testDockerCompose: ClassPathResource = ClassPathResource("./test-docker-compose.yaml")

        private val COMPOSE_CONTAINER: KDockerComposeContainer by lazy {
            KDockerComposeContainer(testDockerCompose.file)
                .withLocalCompose(false)
                .withExposedService(POSTGRES.serviceName, POSTGRES.containerPort, Wait.forListeningPort())
        }
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        COMPOSE_CONTAINER.start()

        val postgresUrl = COMPOSE_CONTAINER.getServiceHost(POSTGRES.serviceName, POSTGRES.containerPort)
        val jdbcURL = "jdbc:postgresql://$postgresUrl:${POSTGRES.localPort}/auth_testcontainers"

        TestPropertyValues.of(
            "spring.datasource.url=$jdbcURL"
        ).applyTo(applicationContext.environment)
    }
}