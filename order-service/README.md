# Order Service

Creates orders with product price snapshots and tracks asynchronous inventory results. Built with Java 21, Spring Boot, Spring Data JPA, WebClient, and Spring Kafka.

## Workflow

1. Validate the requested variant IDs and positive quantities.
2. Fetch prices from `product-service` through a load-balanced WebClient and Eureka.
3. Persist a `NEW` order and publish `order_created`.
4. Consume `product_locked` or `product_lock_failed` and transition to `PREPARED` or `REJECTED`.

Order totals and item price snapshots use `BigDecimal`. Result handling locks the order row and preserves terminal states when events are repeated. Database writes and Kafka publication are not atomic; see the [project README](../README.md#consistency-and-failure-handling) for the current consistency boundaries.

## API

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/orders` | Create an order |
| `GET` | `/orders/{orderId}` | Retrieve order details and status |
| `GET` | `/orders/health` | HTTP application smoke check |

The default port is `8080`. PostgreSQL uses `order_db` at `localhost:5433`; Kafka uses `localhost:9092`; Eureka uses `localhost:8761`. Set `POSTGRES_USER` and `POSTGRES_PASSWORD` in the application process environment.

For infrastructure, seed data, startup order, and request examples, follow [Run locally](../README.md#run-locally) in the project README.

## Logging

`src/main/resources/logback-spring.xml` keeps console logging and sends JSON events to Logstash over TCP at `localhost:5000`. Each event includes the configured custom field `app_name: "order-service"`. Order creation logs the incoming request. Set `LOGSTASH_DESTINATION` in the application process environment to use another Logstash host and port.

With ELK running, find these events in Kibana using the `ecommerce-logs-*` data view and the filter `app_name: "order-service"`.

## Build and test

Run from this module using JDK 21:

```bash
./mvnw test
./mvnw package
./mvnw spring-boot:run
```

Tests mock external dependencies and cover request validation, product lookup, pricing, persistence, and inventory-driven state transitions. Generated classes and build output belong in `target/`.
