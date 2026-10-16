# Repository Guidelines

## Project Structure & Module Organization

This repository contains five independent Java 21 / Spring Boot Maven applications, with no root aggregator POM:

- `product-service/`: catalog, variants, inventory, and an order-event consumer; Java packages start at `com.mudotet.ecommerce.product`.
- `order-service/`: order creation, product HTTP client, and Kafka publishing; Java packages start at `com.mudotet.ecommerce.order`.
- `auth-service/`: Keycloak registration and login under `com.mudotet.ecommerce.auth`.
- `api-gateway/`: reactive gateway under `com.mudotet.ecommerce.gateway`.
- `discovery-server/`: Eureka registry under `com.mudotet.ecommerce.discovery`.

Each service uses `src/main/java`, `src/main/resources`, and `src/test/java`. Keep controllers, services, repositories, entities, DTOs, and MapStruct mappers in their existing packages. Generated output belongs in `target/`. Consult `order-service/AGENTS.md` when changing that module. The root README documents local setup and the current implementation scope.

## Build, Test, and Development Commands

Run commands inside the service being changed, for example `cd product-service` or `cd order-service`:

- `./mvnw compile`: compile Java and generate Lombok/MapStruct output.
- `./mvnw test`: run the module's automated tests.
- `./mvnw package`: run tests and build the application JAR.
- `./mvnw spring-boot:run`: start the service locally.

Build Product and Order when changing their shared HTTP or Kafka contracts. Local configuration expects PostgreSQL at `localhost:5433`, with `product_db` and `order_db`, and Kafka at `localhost:9092`. Product listens on port `8081`; Order listens on `8080`. Compose runs infrastructure; start Java applications separately.

## Coding Style & Naming Conventions

Match surrounding indentation: product code commonly uses two spaces; order code uses four. Use kebab-case service directories and Maven artifact IDs, `UpperCamelCase` classes, `lowerCamelCase` methods/fields, and `UPPER_SNAKE_CASE` constants. Use singular service names such as `OrderService`, and `Request`/`Response` suffixes for API models. Runtime configuration is named `application.yaml` in every module. Preserve Jakarta validation and service transaction boundaries. Edit mapper interfaces rather than generated implementations. No formatter or linter is configured.

## Testing Guidelines

Use JUnit Jupiter, Mockito, and Spring MVC MockMvc for focused behavior tests. Name new test classes `*Tests`. Order tests mock external dependencies. Product's `BaseClassesCheck` is a standalone assertion program, not a JUnit test. No coverage threshold is configured; cover changed behavior and failure cases.

## Commit & Pull Request Guidelines

History uses imperative subjects such as `Add auditing configuration` and `Implement product management features`. Keep commits focused. PRs should describe changed behavior, affected services, configuration impacts, and validation commands/results. Link relevant issues and include request/response examples for API changes.

## Security & Configuration

Keep credentials in local environment overrides; commit only shareable templates. Spring does not automatically load `.env` files. Verify YAML configuration and dependency readiness before claiming local startup succeeds.
