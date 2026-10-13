# Repository Guidelines

## Project Structure & Module Organization

This repository contains two independent Java 21 / Spring Boot Maven services, with no root aggregator POM:

- `product_services/`: catalog, variants, inventory, and an order-event consumer; Java packages start at `com.mudotet.productservice`.
- `order_services/`: order creation, product HTTP client, and Kafka publishing; Java packages start at `com.example.order_services`.

Each service uses `src/main/java`, `src/main/resources`, and `src/test/java`. Keep controllers, services, repositories, entities, DTOs, and MapStruct mappers in their existing packages. Generated output belongs in `target/`. Consult `order_services/AGENTS.md` when changing that module. The root README contains outdated scaffold instructions; verify paths against the current checkout.

## Build, Test, and Development Commands

Run commands inside the service being changed, for example `cd product_services` or `cd order_services`:

- `./mvnw compile`: compile Java and generate Lombok/MapStruct output.
- `./mvnw test`: run the module's automated tests.
- `./mvnw package`: run tests and build the application JAR.
- `./mvnw spring-boot:run`: start the service locally.

Build both modules when changing their shared HTTP or Kafka contracts. Local configuration expects PostgreSQL at `localhost:5433`, with `product_db` and `order_db`, and Kafka at `localhost:9092`. Product listens on port `8081`; order listens on `8080`. The root Dockerfile is an Ubuntu placeholder, not an application launcher.

## Coding Style & Naming Conventions

Match surrounding indentation: product code commonly uses two spaces; order code uses four. Use `UpperCamelCase` classes, `lowerCamelCase` methods/fields, and `UPPER_SNAKE_CASE` constants. Follow names such as `AddOrderRequest`, `OrderResponse`, and `OrderRepository`. Preserve Jakarta validation and service transaction boundaries. Edit mapper interfaces rather than generated implementations. No formatter or linter is configured.

## Testing Guidelines

Use JUnit Jupiter, Mockito, and Spring MVC MockMvc for focused behavior tests. Name new test classes `*Tests`. Order tests mock external dependencies. Product's `BaseClassesCheck` is a standalone assertion program, not a JUnit test. No coverage threshold is configured; cover changed behavior and failure cases.

## Commit & Pull Request Guidelines

History uses imperative subjects such as `Add auditing configuration` and `Implement product management features`. Keep commits focused. PRs should describe changed behavior, affected services, configuration impacts, and validation commands/results. Link relevant issues and include request/response examples for API changes.

## Security & Configuration

Keep credentials in local environment overrides; commit only shareable templates. Spring does not automatically load `.env` files. Verify YAML configuration and dependency readiness before claiming local startup succeeds.
