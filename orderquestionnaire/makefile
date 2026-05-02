.PHONY: build up down test it publish

build:
\t./gradlew clean build -x test

test:
\t./gradlew test

it:
\t./gradlew integrationTest

docker-build:
\tcd modules/bootstrap && ./gradlew bootJar && docker build -t acme/orderquestionnaire:latest .

up:
\tdocker compose -f docker/docker-compose.yml up -d

down:
\tdocker compose -f docker/docker-compose.yml down -v