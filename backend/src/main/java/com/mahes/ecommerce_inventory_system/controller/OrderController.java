package com.mahes.ecommerce_inventory_system.controller;

import com.mahes.ecommerce_inventory_system.dto.CreateOrderRequest;
import com.mahes.ecommerce_inventory_system.dto.OrderResponse;
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
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody CreateOrderRequest request) {
        try {
            OrderResponse createdOrder = orderService.placeOrder(request);
            return ResponseEntity.status(201).body(createdOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}