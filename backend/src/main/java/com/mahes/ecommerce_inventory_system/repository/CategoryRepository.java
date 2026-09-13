package com.mahes.ecommerce_inventory_system.repository;

import com.mahes.ecommerce_inventory_system.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}