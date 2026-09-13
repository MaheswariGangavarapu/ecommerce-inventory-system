package com.mahes.ecommerce_inventory_system.service;

import com.mahes.ecommerce_inventory_system.dao.InventoryDao;
import com.mahes.ecommerce_inventory_system.dto.CreateOrderRequest;
import com.mahes.ecommerce_inventory_system.dto.OrderItemRequest;
import com.mahes.ecommerce_inventory_system.entity.*;
import com.mahes.ecommerce_inventory_system.repository.CustomerRepository;
import com.mahes.ecommerce_inventory_system.repository.OrderRepository;
import com.mahes.ecommerce_inventory_system.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryDao inventoryDao;

    public OrderService(OrderRepository orderRepository,
                         CustomerRepository customerRepository,
                         ProductRepository productRepository,
                         InventoryDao inventoryDao) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryDao = inventoryDao;
    }

    @Transactional
    public Order placeOrder(CreateOrderRequest request) {

        // Step 1: Load the customer, or fail fast if they don't exist.
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException(
                        "Customer not found with id: " + request.getCustomerId()));

        // Step 2: Reserve stock for EVERY item first, before creating anything.
        // We track what we've already reserved so we can roll it back manually
        // if a LATER item in the same order fails.
        List<Long> reservedProductIds = new ArrayList<>();
        List<Integer> reservedQuantities = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            boolean reserved = inventoryDao.reserveStock(
                    itemRequest.getProductId(), itemRequest.getQuantity());

            if (!reserved) {
                // Not enough stock for this item — undo every reservation
                // we already made earlier in this same loop, then fail.
                for (int i = 0; i < reservedProductIds.size(); i++) {
                    inventoryDao.releaseReservedStock(
                            reservedProductIds.get(i), reservedQuantities.get(i));
                }
                throw new RuntimeException(
                        "Insufficient stock for product id: " + itemRequest.getProductId());
            }

            reservedProductIds.add(itemRequest.getProductId());
            reservedQuantities.add(itemRequest.getQuantity());
        }

        // Step 3: Build the Order and its OrderItems now that stock is secured.
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found with id: " + itemRequest.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice()); // price snapshot
            orderItem.calculateLineTotal();

            order.addOrderItem(orderItem);
            total = total.add(orderItem.getLineTotal());
        }

        order.setTotalAmount(total);

        // Step 4: Save everything. Because of cascade = ALL on Order.orderItems,
        // saving the Order automatically saves all its OrderItems too.
        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}