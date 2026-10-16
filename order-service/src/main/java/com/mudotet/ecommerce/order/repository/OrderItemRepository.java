package com.mudotet.ecommerce.order.repository;

import com.mudotet.ecommerce.order.entity.OrderItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}
