package com.mudotet.ecommerce.product.repository;

import com.mudotet.ecommerce.product.entity.Inventory;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select i from Inventory i where i.productVariant.id in :variantIds order by i.productVariant.id")
  List<Inventory> findAllForUpdate(@Param("variantIds") List<UUID> variantIds);

}
