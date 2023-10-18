# role-auth-system

Role based auth system API for managing users, their creds and roles and access role protected endpoints.
Used tech stack:
- **Kotlin** as a main language
- **Postgres** for persistence
- **Flyway** for migrations
- **Spring Data** for accessing database from code
- **Spring Boot** v3.1.4 for app start
- **Spring MVC** for REST API
- **Springdoc** for Open API (Swagger 3) documentation
- **JUnit 5**, **Mockito**, **Testcontainers** for testing

## Accessing Swagger

Springdoc is used for documenting REST API. You can access it here [`/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html).
You can also download Open API specification by accessing [`/v3/api-docs.yaml`](http://localhost:8080/v3/api-docs.yaml)


To run locally:
1. Run the environment using docker-compose
```
docker-compose -p test-auth-system up -d
```
2. Run build with migrations
```
./gradlew clean build flywayMigrate
```