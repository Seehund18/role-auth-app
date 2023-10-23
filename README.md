# role-auth-system

Role based auth system API for managing users, their creds and roles and access role protected endpoints.
Used tech stack:
- **Kotlin** as a main language
- **Kvision** and its modules for frontend
- **Postgres** for persistence
- **Flyway** for database migrations
- **Spring Data** for accessing database
- **Spring Boot** v3.1.4 for app starting
- **Spring MVC** for REST API and servlet
- **Springdoc** for Open API (Swagger 3) documentation
- **JUnit 5**, **Mockito**, **Testcontainers** for testing
- **Docker** for integration tests with Testcontainers and local deployment

### Project structure

- backend code can be found at `backend/src/main`
- integration tests reside with unit test at `backend/src/test/kotlin/integration`
- frontend code can be found at `frontend/src/jsMain`

### How to build and run

`build-and-run.sh` script is provided for convenience of building and running the application.
It builds both frontend and backend using `./gradlew`, builds docker image for them and run all the services using `docker-compose`.

Prerequisites:
- JDK17 must be installed on your machine
- Docker must be installed on your machine
- 8080 port must be available for backend
- 5432 port must be available for postgres
- 8090 port must be available for frontend

Please keep in mind, that for some systems script permissions must be changed before running it
```bash
chmod u+wx,o+wx ./build-and-run.sh
```

### URLs

After running applications in Docker, following links can be used to access different parts of the system:

- Swagger UI with backend API description [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html).
- Download Open API specification [`http://localhost:8080/v3/api-docs.yaml`](http://localhost:8080/v3/api-docs.yaml)
- Frontend [http://localhost:8090](http://localhost:8090)

### Testing

For convenience of testing backend, postman collection is provided, which can be found here
`postman/role_auth_app.postman_collection.json`

System already contains some default test user data for each role, filled by a migration script
`backend/src/main/resources/db/migration/V1.2__fill_default_data.sql`
You can use these default users to send requests:
| User login  | Email                     | Password         |
|:-----------:|:-------------------------:|:----------------:|
| admin       | someAdmin@gmail.com       | notJustSimpleAdmin  |
| reviewer    | someReviwerEmail@mail.com | someReviewerPass    |
| user        | simpleUserEmail@gmail.com | somePass            |

