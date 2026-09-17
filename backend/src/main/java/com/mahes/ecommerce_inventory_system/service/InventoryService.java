package com.mahes.ecommerce_inventory_system.service;

import com.mahes.ecommerce_inventory_system.dao.InventoryDao;
import com.mahes.ecommerce_inventory_system.dao.InventoryRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryDao inventoryDao;
    private final JdbcTemplate jdbcTemplate;

    public InventoryService(InventoryDao inventoryDao, JdbcTemplate jdbcTemplate) {
        this.inventoryDao = inventoryDao;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<InventoryRecord> getInventoryByProductId(Long productId) {
        return inventoryDao.findByProductId(productId);
    }

    // Creates the initial inventory row for a product (one-time setup,
    // not something that happens through the normal reserve/confirm flow).
    public void createInventory(Long productId, int initialQuantity, int reorderThreshold) {
        String sql = """
                INSERT INTO inventory (product_id, quantity_on_hand, quantity_reserved, reorder_threshold, updated_at)
                VALUES (?, ?, 0, ?, CURRENT_TIMESTAMP)
                """;
        jdbcTemplate.update(sql, productId, initialQuantity, reorderThreshold);
    }
}