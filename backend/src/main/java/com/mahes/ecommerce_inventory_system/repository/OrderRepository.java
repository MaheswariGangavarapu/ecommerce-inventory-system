package com.mahes.ecommerce_inventory_system.repository;

import com.mahes.ecommerce_inventory_system.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}