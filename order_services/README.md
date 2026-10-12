# Order service

Service dùng Java 21, Spring MVC, JPA và WebClient.

```text
clients/      ProductClient, impl/ProductClientImpl
common/       BaseEntity, BaseRequest, BaseResponse, ErrorCode
config/       JPA auditing, Basic Auth
controller/   OrderController
dto/request/  AddOrderRequest, OrderItemRequest
dto/response/ OrderResponse, OrderItemResponse, ProductItemResponse
entity/       Order, OrderItem
mapper/       OrderMapper
repository/   OrderRepository, OrderItemRepository
service/      OrderServices, impl/OrderServicesImpl
```

`POST /orders` validate items, gọi product lấy giá và lưu order/items bằng `@Transactional`.
`ProductClientImpl` gọi trực tiếp `http://localhost:8081/products/order-items`.
Quantity lấy từ request và giá lấy từ product.
Flow hiện tại chưa xử lý inventory.

`Order` lưu items thông qua cascade và `order.addItem(item)` thiết lập quan hệ hai chiều.
`OrderItem` lưu variant UUID, quantity và giá mua kiểu `BigDecimal`.
`GET /orders/{orderId}` trả DTO, không serialize vòng lặp quan hệ entity.
`GET /orders/health` kiểm tra service.

Cấu hình local nằm trực tiếp trong `src/main/resources/application.yaml`.
Order dùng port `8080`, database `order_db` tại `localhost:5433` và Basic Auth `demo` / `demo123`.
Chạy `mvn spring-boot:run` với Java 21.
Xem [flow và request mẫu](../docs/create-order.md).

Chạy `mvn test` để kiểm tra controller, client và service bằng mock.
Các test này không cần database hoặc product service đang chạy.
