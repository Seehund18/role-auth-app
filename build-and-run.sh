#!/bin/sh

set -e

./gradlew clean build

cd ./backend
docker build . -t role-auth-system-backend --no-cache
cd ..

cd ./frontend
../gradlew zip
docker build . -t role-auth-system-frontend --no-cache
cd ..

# Comment out the line below, if you don't want to run the app
docker-compose -p role-auth-system up -d

echo "Role-auth-app has been built successfully"
