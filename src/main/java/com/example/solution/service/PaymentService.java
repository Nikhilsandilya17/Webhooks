package com.example.solution.service;

import com.example.solution.dto.PaymentEvent;
import com.example.solution.entity.CustomerOrder;
import com.example.solution.entity.ProcessedEvent;
import com.example.solution.repository.OrderRepository;
import com.example.solution.repository.ProcessedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private OrderRepository customerOrderRepository;

    @Autowired
    private ProcessedEventRepository paymentEventRepository;

    @Autowired
    private StringRedisTemplate redisTemplate; // <--- The new tool

    @Transactional
    public void processPaymentSuccess(PaymentEvent paymentEvent) {
        String paymentEventId = paymentEvent.getEventId();
        String lockKey = "idempotency_" + paymentEventId;

        // --- STEP 1: FAST REDIS CHECK ---
        // Try to save the key. 
        // If it returns TRUE: It's new. We got the lock.
        // If it returns FALSE: It already exists. Duplicate!
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofHours(24));
        if (Boolean.FALSE.equals(isNew)) {
            log.info("Redis: Duplicate Event " + paymentEventId + " blocked.");
            return;
        }
        try {
            // --- STEP 2: PROCESSING ---
            log.info("Processing Payment for: " + paymentEvent.getCustomerId());
            CustomerOrder customerOrder = customerOrderRepository.findByCustomerId(paymentEvent.getCustomerId());
            if(Objects.isNull(customerOrder)){
                customerOrder = new CustomerOrder();
                customerOrder.setCustomerId(paymentEvent.getCustomerId());
            }
            customerOrder.setCustomerId(paymentEvent.getCustomerId());
            customerOrder.setAmountPaid(paymentEvent.getAmount());
            customerOrder.setStatus("PAID");
            customerOrderRepository.save(customerOrder);
            // --- STEP 3: AUDIT LOG (Optional but recommended) We still save to MySQL for permanent history, but we don't READ from it for checking.
            paymentEventRepository.save(new ProcessedEvent(paymentEventId, LocalDateTime.now()));
            log.info("DATABASE: Order updated successfully.");

        } catch (Exception e) {
            // --- STEP 4: ROLLBACK ---
            // If DB save failed (e.g. database went down), we MUST delete the Redis key, so that when we retry later, it is allowed to pass.
            redisTemplate.delete(lockKey);
            log.info("Error processing. Redis lock released.");
            throw e;
        }
    }
}