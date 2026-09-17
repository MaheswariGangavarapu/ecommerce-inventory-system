package com.mahes.ecommerce_inventory_system.service;

import com.mahes.ecommerce_inventory_system.dao.InventoryDao;
import com.mahes.ecommerce_inventory_system.dto.CreateOrderRequest;
import com.mahes.ecommerce_inventory_system.dto.OrderItemRequest;
import com.mahes.ecommerce_inventory_system.dto.OrderItemResponse;
import com.mahes.ecommerce_inventory_system.dto.OrderResponse;
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
    public OrderResponse placeOrder(CreateOrderRequest request) {

        // Step 1: Load the customer, or fail fast if they don't exist.
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException(
                        "Customer not found with id: " + request.getCustomerId()));

        // Step 2: Reserve stock for EVERY item first, before creating anything.
        List<Long> reservedProductIds = new ArrayList<>();
        List<Integer> reservedQuantities = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            boolean reserved = inventoryDao.reserveStock(
                    itemRequest.getProductId(), itemRequest.getQuantity());

            if (!reserved) {
                // Not enough stock — undo every reservation made so far, then fail.
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

        Order savedOrder = orderRepository.save(order);

        // Step 4: Convert to a flat DTO while still inside the transaction,
        // so lazy-loaded fields can still be accessed safely.
        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Converts a JPA entity into a flat response DTO.
    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setCustomerId(order.getCustomer().getCustomerId());
        response.setCustomerName(order.getCustomer().getFirstName() + " "
                + order.getCustomer().getLastName());
        response.setCustomerEmail(order.getCustomer().getEmail());
        response.setOrderStatus(order.getOrderStatus().name());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderDate(order.getOrderDate());

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setOrderItemId(item.getOrderItemId());
            itemResponse.setProductId(item.getProduct().getProductId());
            itemResponse.setProductName(item.getProduct().getName());
            itemResponse.setProductSku(item.getProduct().getSku());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setUnitPrice(item.getUnitPrice());
            itemResponse.setLineTotal(item.getLineTotal());
            itemResponses.add(itemResponse);
        }
        response.setItems(itemResponses);

        return response;
    }
}