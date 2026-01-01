Integrating kafka transforms your application from a synchronous system(where the user waits for the database updates) to an asynchronous one.
1. Webhook Controller receives the HTTP request -> Pushes it to the Kafka Immediately
2. Kafka acts as a buffer (holding the event)
3. Kafka Consumer (running in background) picks up the event -> Calls PaymentService to update MySQL

@Bean Annotation over KafkaTemplate automatically puts a KafkaTemplate<String, Object> into the application context.
Now, when WebhookController asks for @Autowired KafkaTemplate, Spring will find the exact match and inject the KafkaTemplate bean.

* Steps to run the application with Kafka:
1. run `docker-compose up -d` to start Kafka, Kafka-ui, Zookeeper, MySQL and phpMyAdmin (make sure you have docker),
2. run the Spring Boot application,
3. run the Webhook Simulator
4. Now just observe the logs

Login to localhost:9090/kafka-ui to see the Kafka UI
Login to localhost:8081/phpmyadmin to see the MySQL UI (username: root, password: root)