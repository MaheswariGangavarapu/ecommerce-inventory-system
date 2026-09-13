package com.mahes.ecommerce_inventory_system.service;

import com.mahes.ecommerce_inventory_system.entity.Customer;
import com.mahes.ecommerce_inventory_system.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Customer existing = getCustomerById(id);
        existing.setFirstName(updatedCustomer.getFirstName());
        existing.setLastName(updatedCustomer.getLastName());
        existing.setEmail(updatedCustomer.getEmail());
        existing.setPhone(updatedCustomer.getPhone());
        existing.setAddressLine1(updatedCustomer.getAddressLine1());
        existing.setAddressLine2(updatedCustomer.getAddressLine2());
        existing.setCity(updatedCustomer.getCity());
        existing.setState(updatedCustomer.getState());
        existing.setPostalCode(updatedCustomer.getPostalCode());
        existing.setCountry(updatedCustomer.getCountry());
        return customerRepository.save(existing);
    }
}