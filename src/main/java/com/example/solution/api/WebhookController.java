package com.example.solution.api;

import com.example.solution.dto.PaymentEvent;
import com.example.solution.securityUtil.HmacUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private static final String SHARED_SECRET = "super_secure_secret_key_123";
    private static final String TOPIC = "payment-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/payment-updates")
    public ResponseEntity<String> handleWebhook(@RequestHeader("X-Signature") String incomingSignature, @RequestBody String rawPayload) throws Exception {
        //Validate Signature
        try {
            String calculatedSignature = HmacUtil.calculateHmac(rawPayload, SHARED_SECRET);
            if(!calculatedSignature.equals(incomingSignature)){
                log.info("Invalid signature. Possible tampering detected.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
            }
            //Parse JSON
            PaymentEvent paymentEvent = objectMapper.readValue(rawPayload, PaymentEvent.class);

            //Publish to Kafka
            //We use the eventId as the key to ensure order for that specific event
            kafkaTemplate.send(TOPIC, paymentEvent.getEventId(), paymentEvent);

            log.info(">>> Controller: Pushed event {} to Kafka.", paymentEvent.getEventId());

            // 4. Return immediately! We don't wait for the DB anymore.
            return ResponseEntity.ok("Webhook received and queued");
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }


    }




}
