package com.mahes.ecommerce_inventory_system.repository;

import com.mahes.ecommerce_inventory_system.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}