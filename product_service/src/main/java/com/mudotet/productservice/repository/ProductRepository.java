package com.mudotet.productservice.repository;

import com.mudotet.productservice.dto.response.ProductResponse;
import com.mudotet.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
  boolean existsByName(String name);
}
