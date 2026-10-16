package com.mudotet.ecommerce.product.repository;

import com.mudotet.ecommerce.product.entity.ProcessedOrder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder, UUID> {}
