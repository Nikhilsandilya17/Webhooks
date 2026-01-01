package com.example.solution.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "orders")
@Entity
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerId;;
    private String status;
    private Double amountPaid;

}
