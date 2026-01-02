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

* We are having idempotency check using Redis instead of MySQL to avoid duplicate processing of same event and to avoid case of Race conditions.
* Redis is thread-safe.

* We are using StringRedisTemplate instead of RedisTemplate.
RedisTemplate (The Generic One): By default, it uses Java's native serialization (JdkSerializationRedisSerializer). It converts your keys and values into Java Bytecode.
What you send: key="evt_123"
What is saved in Redis: \xac\xed\x00\x05t\x00\x07evt_123

StringRedisTemplate (The Specific One): It uses StringRedisSerializer. It treats keys and values as simple text.
What you send: key="evt_123"
What is saved in Redis: evt_123

# If we were caching the entire PaymentEvent object (e.g., saving the user's name, amount, date), we might use RedisTemplate so we could do: template.opsForValue().set("user:1", paymentObject);
But for Idempotency, we are only setting a Lock Flag.
Key: idempotency:evt_55
Value: "LOCKED"

Since both the Key and Value are simple Strings, using the heavy RedisTemplate is overkill. StringRedisTemplate is lighter and faster for this specific job.

# Architecture of whole project

