package com.example.solution.api;

import com.example.solution.securityUtil.HmacUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class WebhookSenderSimulator {
    private static final String TARGET_URL = "http://localhost:8080/api/webhooks/payment-updates";
    private static final String SHARED_SECRET = "super_secure_secret_key_123";

    public static void main(String[] args) throws Exception {
        // This is a placeholder for the webhook sender simulator.
        // You can implement an HTTP client here to send POST requests
        // to the TARGET_URL with a valid HMAC signature in the headers.

        //Json payload
        try {
            String jsonPayload = """
                    {
                        "eventId": "evt_NEW_004",
                        "eventType": "PAYMENT_SUCCESS",
                        "amount": 500.00,
                        "customerId": "cust_vip_02"
                    }
                    """;

            //Generate signature
            String signature = HmacUtil.calculateHmac(jsonPayload, SHARED_SECRET);
            log.info("Generated Signature: {}", signature);

            //Send Request
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(TARGET_URL))
                    .header("Content-Type", "application/json")
                    .header("X-Signature", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            log.info("Sending Webhook..");
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            log.info("Response: {} - {}", httpResponse.statusCode(), httpResponse.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
