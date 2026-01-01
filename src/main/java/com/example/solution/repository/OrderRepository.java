package com.example.solution.repository;

import com.example.solution.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    CustomerOrder findByCustomerId(String customerId);
}
