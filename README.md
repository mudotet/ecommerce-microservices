# Learning_Microservices

Khung dự án `product_service` dùng Maven, Java 21 và Spring Boot 4.1.1.

Thư viện: Spring Web MVC, Spring Security, Validation, Spring Data JPA, H2, Lombok và ModelMapper 3.2.4.

Các thư mục `dto`, `controller`, `repository`, `service`, `config`, `mapper` nằm trong `product_service/src/main/java/com/mudotet/productservice/` và được giữ bằng `.gitkeep`.

Dự án hiện chỉ là bộ khung, chưa có lớp Java hoặc lớp khởi chạy Spring Boot.

Kiểm tra cấu hình và biên dịch khung dự án:

```sh
cd product_service
./mvnw validate
./mvnw compile
```
