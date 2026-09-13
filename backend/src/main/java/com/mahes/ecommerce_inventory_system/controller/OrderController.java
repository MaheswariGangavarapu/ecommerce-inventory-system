package com.mahes.ecommerce_inventory_system.controller;

import com.mahes.ecommerce_inventory_system.dto.CreateOrderRequest;
import com.mahes.ecommerce_inventory_system.entity.Order;
import com.mahes.ecommerce_inventory_system.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody CreateOrderRequest request) {
        try {
            Order createdOrder = orderService.placeOrder(request);
            return ResponseEntity.status(201).body(createdOrder);
        } catch (RuntimeException e) {
            // Catches errors like "insufficient stock" or "customer not found"
            // and returns a clean 400 Bad Request with the error message,
            // instead of letting a raw 500 server error leak out.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}