package com.mudotet.productservice.repository;

import com.mudotet.productservice.entity.ProcessedOrder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder, UUID> {}
