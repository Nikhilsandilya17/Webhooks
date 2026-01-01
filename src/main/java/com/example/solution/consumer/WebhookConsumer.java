package com.example.solution.consumer;

import com.example.solution.dto.PaymentEvent;
import com.example.solution.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebhookConsumer {
    /**
     * This Class listens to Kafka and then triggers the database logic
     */

    @Autowired
    private PaymentService paymentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "payment-events", groupId = "payment-group")
    public void consume(PaymentEvent paymentEvent){
        log.info("<<< Consumer: Picked up event {} from Kafka.", paymentEvent.getEventId());
        paymentService.processPaymentSuccess(paymentEvent);
    }
}
