# test-auth-system

Auth system for test task written in Kotlin with PostgreSQL for persistence


To run locally:
1. Run the environment using docker-compose
```
docker-compose -p test-auth-system up -d
```
2. Run build with migrations
```
./gradlew clean build flywayMigrate
```