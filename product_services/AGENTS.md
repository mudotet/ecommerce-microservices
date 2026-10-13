# Repository Guidelines

## Project Structure & Module Organization

`product_services` is an independent Java 21 Spring Boot service for products, variants, categories, and inventory. Source code lives in `src/main/java/com/mudotet/productservice/`:

- `controller`, `service`, and `service/impl`: HTTP endpoints and business logic.
- `repository` and `entity`: Spring Data JPA queries and persistence models.
- `dto/request`, `dto/response`, and `mapper`: API models and MapStruct mappings.
- `consumer` and `consumer/dto`: Kafka order-event handling.
- `common` and `config`: shared response/entity types, security, and auditing.

Runtime configuration is in `src/main/resources/application.yml`; tests belong in the corresponding packages under `src/test/java`. Maven generates build artifacts and mapper implementations under `target/`.

## Build, Test, and Development Commands

Run these commands from `product_services/` using Java 21:

- `./mvnw compile`: compile sources and run annotation processors.
- `./mvnw test`: compile and execute Maven-discovered tests.
- `./mvnw package`: run tests and build the Spring Boot JAR.
- `./mvnw spring-boot:run`: start the service on port `8081`.

Local startup requires PostgreSQL with `product_db` at `localhost:5433` and a reachable Kafka broker. Verify the configured serializers and consumer settings when troubleshooting startup. For manual readiness checks, call the authenticated `GET /products/health` endpoint.

## Coding Style & Naming Conventions

Follow surrounding Java formatting, typically two-space indentation. Use `UpperCamelCase` classes, `lowerCamelCase` methods/fields, and `UPPER_SNAKE_CASE` constants. Follow existing names such as `AddProductRequest`, `ProductResponse`, and `ProductVariantRepository`. Keep persistence queries in repositories and transactional work in services. Use Spring-managed MapStruct interfaces and Lombok consistently; edit source interfaces rather than generated classes. No formatter or linter is configured.

## Testing Guidelines

Use JUnit Jupiter and Spring MVC test support; name new test classes `*Tests`. Cover changed behavior, validation failures, missing variants, and query projections. `BaseClassesCheck` currently uses a standalone `main` with Java assertions; Maven does not run it as a JUnit test. No coverage threshold is configured. Validate JPA queries with focused integration tests when changing joins, identifier types, or constructor projections.

## Commit & Pull Request Guidelines

Use concise imperative subjects, matching history such as `Implement product management features`. PRs should explain behavior changes, include validation results, and identify database/configuration impacts. Include API examples and linked issues where relevant. Coordinate changes to `/products/order-items` or `order_created` with `../order_services`.

## Security & Configuration

Supply sensitive settings through local environment overrides. Spring does not automatically load `.env`. Confirm database connectivity and application readiness before reporting a successful local run.
