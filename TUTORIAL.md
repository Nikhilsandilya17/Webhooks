# 🚀 Event-Driven Webhook System with Kafka & Redis

This project implements a scalable, asynchronous webhook receiver using **Spring Boot**, **Kafka**, **Redis**, and **MySQL**. It transforms a traditional synchronous system into a high-performance event-driven architecture.

---

## 🏗 System Architecture

The system is designed to handle high-throughput payment notifications (webhooks) reliably.

### Workflow

1. **Webhook Simulator:** Simulates an external provider (like PhonePe/Stripe) sending HTTP POST requests.
2. **Webhook Controller:** Receives the HTTP request, validates the signature, and **immediately pushes it to Kafka**.
3. **Apache Kafka:** Acts as a high-throughput buffer to hold events, ensuring no data loss during spikes.
4. **Kafka Consumer:** Asynchronously picks up events from the topic.
5. **Payment Service:** Processes the business logic, using **Redis** for idempotency and **MySQL** for persistence.

### Why Kafka?

Integrating Kafka transforms the application from a **Synchronous System** (where the user waits for database updates) to an **Asynchronous System**.

* **Controller:** Returns `200 OK` immediately after pushing to Kafka.
* **Consumer:** Processes the heavy logic in the background without blocking the HTTP response.

---

## 🛠️ Setup & Execution

### Prerequisites

* Docker & Docker Compose
* Java 17+

### 1. Infrastructure Setup

Run the following command to start Kafka, Zookeeper, Redis, MySQL, and their respective UIs:

```bash
docker-compose up -d

```

### 2. Run the Application

Start the Spring Boot application (`WebhookApplication.java`).

> **Note:** The `@Bean` annotation automatically configures `KafkaTemplate<String, Object>` in the application context. When the controller requests `@Autowired KafkaTemplate`, Spring injects the correctly configured bean.

### 3. Trigger Events

Run the `WebhookSenderSimulator.java` to simulate incoming payment webhooks.

### 4. Monitoring

* **Kafka UI:** [http://localhost:9090](https://www.google.com/search?q=http://localhost:9090) (View Topics & Messages)
* **MySQL UI (phpMyAdmin):** [http://localhost:8081](https://www.google.com/search?q=http://localhost:8081)
* *User:* `root` | *Pass:* `root`


* **Redis UI:** [http://localhost:8082](https://www.google.com/search?q=http://localhost:8082)

---

## ⚡ High-Performance Idempotency (Redis)

To prevent duplicate processing (e.g., if the sender retries the webhook due to network issues) and avoid Race Conditions, we use **Redis** instead of MySQL.

### Why Redis?

* **Speed:** Redis operates in-memory (sub-millisecond response), whereas MySQL involves disk I/O.
* **Thread Safety:** Redis operations are atomic.

### `StringRedisTemplate` vs `RedisTemplate`

We use `StringRedisTemplate` for this specific use case.

| Feature | `RedisTemplate` (Generic) | `StringRedisTemplate` (Specific) |
| --- | --- | --- |
| **Serialization** | Uses `JdkSerializationRedisSerializer` | Uses `StringRedisSerializer` |
| **Data Format** | Binary Bytecode (`\xac\xed\x00\x05...`) | Plain Text (`evt_123`) |
| **Use Case** | Complex Java Objects | Simple Strings / Locks |

**Implementation Strategy:**
Since we are only setting a **Lock Flag** for idempotency, using the heavy generic template is overkill.

```properties
Key:   "idempotency:evt_55"
Value: "LOCKED"

```

* *If the key exists:* Skip processing (Duplicate detected).
* *If the key is new:* Lock it and process the event.

---

## ☕ The Conceptual Model: "Starbucks & PhonePe"

To understand Webhooks, visualize a payment at a coffee shop.

### The Scenario

You are at **Starbucks** and scan a QR code to pay **₹250** via **PhonePe**.

1. **The Transaction:** You pay using the PhonePe App. The money leaves your bank.
2. **The Problem:** The Starbucks POS machine (Cashier) needs to know you paid to print the bill. How does it get this info?

### Approach A: Polling (Bad) ❌

The Starbucks computer keeps asking PhonePe every second:

> *"Did he pay? ... Did he pay? ... Did he pay?"*

* **Result:** Wasted resources and network traffic.

### Approach B: Webhooks (Good) ✅

The Starbucks computer waits quietly. When the payment is successful, **PhonePe's Server calls the Starbucks Server**:

> *"Hey! Payment of ₹250 is SUCCESS for Order #101."*

* **Result:** Instant notification with zero waste. **This phone call is the Webhook.**

---

## 🔄 The Technical Flow

A Webhook is technically just a standard **REST API call**, but the direction is reversed.

| Feature | Standard REST API (Polling) | Webhook (Reverse API) |
| --- | --- | --- |
| **Initiator** | **You** call the Server | **Server** calls You |
| **Analogy** | You calling a restaurant every 5 mins to check for a table. | The restaurant taking your number and calling *you* when the table is ready. |
| **Protocol** | HTTP POST | HTTP POST |
| **Body** | JSON | JSON |
| **Response** | 200 OK | 200 OK |

### Lifecycle of an Event in this Project

1. **The Event (Simulator):**
* *Real Life:* PhonePe processes a transfer.
* *Code:* `WebhookSenderSimulator` creates a JSON packet (`"status": "SUCCESS"`) and signs it with an **HMAC Signature** for security.


2. **The Notification (Webhook Call or CallbackURL):**
* *Real Life:* PhonePe sends a POST request to `api.starbucks.com`.
* *Code:* Simulator POSTs to `localhost:8080/api/webhooks/payment-updates`.


3. **The Handshake (Controller):**
* *Real Life:* Starbucks verifies the signature to prevent fraud.
* *Code:* `WebhookController` verifies `HmacUtil`. If valid, it responds `200 OK`.


4. **The Processing (Async Layer):**
* *Real Life:* Starbucks updates the order status to "PAID".
* *Code:* The Controller pushes the event to **Kafka**. The Consumer reads it, checks **Redis** for duplicates, and updates **MySQL**.