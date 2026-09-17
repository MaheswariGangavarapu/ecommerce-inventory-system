package com.mahes.ecommerce_inventory_system.controller;

import com.mahes.ecommerce_inventory_system.dao.InventoryRecord;
import com.mahes.ecommerce_inventory_system.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryRecord> getInventory(@PathVariable Long productId) {
        return inventoryService.getInventoryByProductId(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreateInventoryRequest(int initialQuantity, int reorderThreshold) {}

    @PostMapping("/{productId}")
    public ResponseEntity<Void> createInventory(@PathVariable Long productId,
                                                  @RequestBody CreateInventoryRequest request) {
        inventoryService.createInventory(productId, request.initialQuantity(), request.reorderThreshold());
        return ResponseEntity.status(201).build();
    }
}