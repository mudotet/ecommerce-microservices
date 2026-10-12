package com.example.order_services.repository;

import com.example.order_services.entity.OrderItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}
