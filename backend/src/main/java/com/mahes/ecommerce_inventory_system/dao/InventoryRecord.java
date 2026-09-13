package com.mahes.ecommerce_inventory_system.dao;

import java.time.LocalDateTime;

// A plain data holder representing one row from the 'inventory' table.
// Unlike our JPA entities, this is NOT managed by Hibernate — it's just
// a simple container we fill manually from raw SQL query results.
public class InventoryRecord {
    private Long inventoryId;
    private Long productId;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Integer reorderThreshold;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(Integer quantityOnHand) { this.quantityOnHand = quantityOnHand; }

    public Integer getQuantityReserved() { return quantityReserved; }
    public void setQuantityReserved(Integer quantityReserved) { this.quantityReserved = quantityReserved; }

    public Integer getReorderThreshold() { return reorderThreshold; }
    public void setReorderThreshold(Integer reorderThreshold) { this.reorderThreshold = reorderThreshold; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getAvailableQuantity() {
        return quantityOnHand - quantityReserved;
    }
}