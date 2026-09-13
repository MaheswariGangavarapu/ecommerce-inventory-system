package com.mahes.ecommerce_inventory_system.dao;

import java.util.Optional;

public interface InventoryDao {

    Optional<InventoryRecord> findByProductId(Long productId);

    // Attempts to reserve stock for an order. Returns true if successful,
    // false if there wasn't enough available quantity (preventing overselling).
    boolean reserveStock(Long productId, int quantity);

    // Releases previously reserved stock (e.g., if an order is cancelled).
    void releaseReservedStock(Long productId, int quantity);

    // Confirms a sale: moves reserved stock into a permanent deduction
    // from quantity_on_hand (e.g., once payment succeeds and order ships).
    void confirmStockDeduction(Long productId, int quantity);
}