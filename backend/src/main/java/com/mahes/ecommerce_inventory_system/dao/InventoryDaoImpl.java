package com.mahes.ecommerce_inventory_system.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class InventoryDaoImpl implements InventoryDao {

    private final JdbcTemplate jdbcTemplate;

    // Constructor injection: Spring automatically supplies a working
    // JdbcTemplate instance here at startup — we don't create it ourselves.
    public InventoryDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Maps one row of a ResultSet into our plain InventoryRecord object.
    // This replaces what Hibernate normally does for us automatically.
    private final RowMapper<InventoryRecord> rowMapper = (ResultSet rs, int rowNum) -> {
        InventoryRecord record = new InventoryRecord();
        record.setInventoryId(rs.getLong("inventory_id"));
        record.setProductId(rs.getLong("product_id"));
        record.setQuantityOnHand(rs.getInt("quantity_on_hand"));
        record.setQuantityReserved(rs.getInt("quantity_reserved"));
        record.setReorderThreshold(rs.getInt("reorder_threshold"));
        record.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return record;
    };

    @Override
    public Optional<InventoryRecord> findByProductId(Long productId) {
        String sql = "SELECT * FROM inventory WHERE product_id = ?";
        return jdbcTemplate.query(sql, rowMapper, productId)
                .stream()
                .findFirst();
    }

    @Override
    public boolean reserveStock(Long productId, int quantity) {
        // THE KEY LINE: this single SQL statement only succeeds if there's
        // enough available stock (on_hand - reserved >= requested quantity).
        // The check and the update happen ATOMICALLY in one database
        // operation — no gap in time where two requests could both "pass"
        // the check before either updates the row. This is what actually
        // prevents overselling under concurrent load.
        String sql = """
                UPDATE inventory
                SET quantity_reserved = quantity_reserved + ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE product_id = ?
                  AND (quantity_on_hand - quantity_reserved) >= ?
                """;

        int rowsAffected = jdbcTemplate.update(sql, quantity, productId, quantity);

        // If rowsAffected is 0, the WHERE condition failed — not enough
        // stock was available, so nothing was updated. Return false so
        // the calling code knows to reject the order.
        return rowsAffected > 0;
    }

    @Override
    public void releaseReservedStock(Long productId, int quantity) {
        String sql = """
                UPDATE inventory
                SET quantity_reserved = quantity_reserved - ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE product_id = ?
                """;
        jdbcTemplate.update(sql, quantity, productId);
    }

    @Override
    public void confirmStockDeduction(Long productId, int quantity) {
        String sql = """
                UPDATE inventory
                SET quantity_on_hand = quantity_on_hand - ?,
                    quantity_reserved = quantity_reserved - ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE product_id = ?
                """;
        jdbcTemplate.update(sql, quantity, quantity, productId);
    }
}