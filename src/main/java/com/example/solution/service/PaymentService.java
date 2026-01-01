package com.example.solution.service;

import com.example.solution.dto.PaymentEvent;
import com.example.solution.entity.CustomerOrder;
import com.example.solution.entity.ProcessedEvent;
import com.example.solution.repository.OrderRepository;
import com.example.solution.repository.ProcessedEventRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
public class PaymentService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Transactional
    public void processPaymentSuccess(PaymentEvent paymentEvent){
        //IdempotencyCheck
        if(processedEventRepository.existsById(paymentEvent.getEventId())){
            log.info("Duplicate Event " + paymentEvent.getEventId() + " detected. Skipping.");
            return;
        }

        //BusinessLogic (Update Order)
        CustomerOrder customerOrder = orderRepository.findByCustomerId(paymentEvent.getCustomerId());
        if(Objects.isNull(customerOrder)){
            customerOrder = new CustomerOrder();
            customerOrder.setCustomerId(paymentEvent.getCustomerId());
        }
        customerOrder.setAmountPaid(paymentEvent.getAmount());
        customerOrder.setStatus("PAID");
        orderRepository.save(customerOrder);
        log.info("Order for Customer " + paymentEvent.getCustomerId() + " updated to PAID.");

        //MarkEventAsProcessed
        processedEventRepository.save(new ProcessedEvent(paymentEvent.getEventId(), LocalDateTime.now()));
        log.info("Event " + paymentEvent.getEventId() + " marked as processed.");

    }



}
