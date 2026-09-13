package com.mahes.ecommerce_inventory_system.dto;

import java.util.List;

public class CreateOrderRequest {
    private Long customerId;
    private List<OrderItemRequest> items;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}