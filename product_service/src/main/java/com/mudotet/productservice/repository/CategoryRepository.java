package com.mudotet.productservice.repository;

import com.mudotet.productservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {}
