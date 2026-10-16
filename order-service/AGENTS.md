# Repository Guidelines

## Project Structure & Module Organization

This module contains the Spring Boot order service under `src/main/java/com/mudotet/ecommerce/order/`.
Keep code in the existing packages: `controller`, `service`, `repository`, `entity`, `dto/request`, `dto/response`, `mapper`, `clients`, `config`, and `common`.
Runtime configuration is in `src/main/resources/application.yaml`.
Tests belong in the matching package under `src/test/java/com/mudotet/ecommerce/order/`.
The Maven wrapper (`mvnw`, `mvnw.cmd`) is included for consistent builds.

## Build, Test, and Development Commands

Use Java 21, as specified by `pom.xml`.

- `./mvnw test` compiles the project and runs the test suite.
- `./mvnw package` builds the application jar and runs tests.
- `./mvnw spring-boot:run` starts the service locally.

Tests use Spring Boot's test dependencies and Mockito-style mocks; the existing tests are designed to run without a live database or product service.

## Coding Style & Naming Conventions

Follow the existing Java style: four spaces for indentation, `UpperCamelCase` for classes, `lowerCamelCase` for methods and fields, and descriptive package names in lowercase.
Keep request and response models in their respective DTO packages, and keep database access in repositories.
Use Jakarta validation on incoming request data and preserve the service's transactional boundaries.
No formatter or linter is configured in `pom.xml`; avoid introducing one without a project need.

## Testing Guidelines

Name test classes with the `*Tests` suffix, as in `ProductClientTests` and `OrderServiceTests`.
Add focused tests alongside the existing tests for changed controller, client, mapper, or service behavior.
Run `./mvnw test` before submitting changes.

## Commit & Pull Request Guidelines

Use a concise imperative commit subject, for example `Add order lookup validation`.
Pull requests should explain the behavior changed, note configuration or database impacts, and include test results.
Include a request or response example when changing the HTTP API.

## Security & Configuration

Local service and database settings are in `src/main/resources/application.yaml`.
Do not commit passwords, tokens, or other secrets; use local environment configuration for sensitive values.
Supply `POSTGRES_USER` and `POSTGRES_PASSWORD` through the process environment. The load-balanced product client resolves `http://product-service` through Eureka. Direct Product and Order endpoints currently have no application-level authentication.
