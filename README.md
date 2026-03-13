# 🚀 Real-Time Event Streaming & Notification Platform

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black?style=flat-square&logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-7.2-red?style=flat-square&logo=redis)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-orange?style=flat-square&logo=rabbitmq)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?style=flat-square&logo=docker)
![Prometheus](https://img.shields.io/badge/Prometheus-Grafana-orange?style=flat-square&logo=prometheus)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

> A production-grade microservices backend platform that ingests events, processes them asynchronously via Apache Kafka, and delivers real-time notifications through WebSocket and push channels — built to demonstrate system design thinking, not just CRUD skills.

---

## 📋 Table of Contents

- [Architecture](#-architecture)
- [Microservices](#-microservices)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Monitoring](#-monitoring)
- [Design Decisions](#-design-decisions)
- [Resume Highlights](#-resume-highlights)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT / MOBILE APP                       │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTP + WebSocket
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│              API GATEWAY SERVICE  :8080                          │
│         Spring Cloud Gateway + JWT Validation                    │
│         Rate Limiting · Routing · Auth Filter                    │
└──────────────┬──────────────────────────┬───────────────────────┘
               │                          │
               ▼                          ▼
┌──────────────────────┐    ┌─────────────────────────┐
│   USER SERVICE :8081  │    │ EVENT PRODUCER  :8082   │
│  JWT Auth · MySQL    │    │  REST API · Kafka       │
│  Registration/Login  │    │  Publishes user-events  │
└──────────────────────┘    └────────────┬────────────┘
         │                               │
         ▼                               ▼
    ┌─────────┐                ┌──────────────────┐
    │  MySQL  │                │   KAFKA TOPIC    │
    │  user_db│                │   user-events    │
    └─────────┘                └────────┬─────────┘
                                        │
                          ┌─────────────┴──────────────┐
                          ▼                             ▼
          ┌───────────────────────┐    ┌───────────────────────────┐
          │ EVENT CONSUMER :8083  │    │ NOTIFICATION SVC  :8084   │
          │ Kafka Consumer        │    │ Kafka Consumer            │
          │ MySQL · Redis Cache   │    │ WebSocket (STOMP)         │
          └──────────┬────────────┘    │ RabbitMQ Producer         │
                     │                 └──────────┬────────────────┘
              ┌──────┴──────┐                     │
              ▼             ▼              ┌───────┴────────┐
          ┌───────┐    ┌────────┐          ▼                ▼
          │ MySQL │    │ Redis  │   ┌────────────┐  ┌─────────────┐
          │event_db    │ Cache  │   │email.queue │  │ push.queue  │
          └───────┘    └────────┘   └────────────┘  └─────────────┘
                                           │
                                    ┌──────┴──────┐
                                    ▼             ▼
                               📧 Email      📱 Push FCM
```

---

## 📦 Microservices

| Service                  | Port | Responsibility                                    |
| ------------------------ | ---- | ------------------------------------------------- |
| `api-gateway-service`    | 8080 | Single entry point, JWT validation, routing       |
| `user-service`           | 8081 | Registration, login, JWT issuance                 |
| `event-producer-service` | 8082 | Accepts events via REST, publishes to Kafka       |
| `event-consumer-service` | 8083 | Consumes Kafka events, persists to MySQL + Redis  |
| `notification-service`   | 8084 | WebSocket delivery + RabbitMQ async notifications |

### Infrastructure

| Service      | Port         | Purpose                                 |
| ------------ | ------------ | --------------------------------------- |
| MySQL        | 3307         | Persistent storage for users and events |
| Redis        | 6379         | Event deduplication cache (24hr TTL)    |
| Apache Kafka | 9092 / 29092 | Event streaming backbone                |
| RabbitMQ     | 5672 / 15672 | Async notification queue                |
| Prometheus   | 9090         | Metrics collection                      |
| Grafana      | 3000         | Metrics dashboards                      |
| Kafka UI     | 8091         | Visual Kafka topic browser              |

---

## 🛠️ Tech Stack

| Technology            | Version     | Why Used                              |
| --------------------- | ----------- | ------------------------------------- |
| Java                  | 17          | LTS, modern features                  |
| Spring Boot           | 3.x         | Microservice framework                |
| Spring Cloud Gateway  | 2023.x      | API gateway with filters              |
| Spring Security + JWT | JJWT 0.12.3 | Stateless authentication              |
| Apache Kafka          | 3.x         | High-throughput event streaming       |
| Spring Kafka          | 3.x         | Kafka producer/consumer integration   |
| Redis                 | 7.2         | Fast caching, deduplication           |
| RabbitMQ              | 3.12        | Reliable async message delivery       |
| WebSocket + STOMP     | -           | Real-time bidirectional communication |
| MySQL                 | 8.0         | Relational data persistence           |
| Docker Compose        | -           | One-command local deployment          |
| Prometheus + Grafana  | -           | Observability stack                   |

---

## 🚀 Getting Started

### Prerequisites

- Docker Desktop installed and running
- Git

### Run the entire platform with one command:

```bash
git clone https://github.com/SagarPokale2000/notification-service.git
cd event-stream-notification-platform
docker-compose up -d --build
```

Wait ~60 seconds for all services to initialize, then verify:

```bash
docker-compose ps
```

All 13 containers should be running ✅

### Service URLs

| Service              | URL                                                |
| -------------------- | -------------------------------------------------- |
| API Gateway          | http://localhost:8080                              |
| User Service         | http://localhost:8081                              |
| Event Producer       | http://localhost:8082                              |
| Event Consumer       | http://localhost:8083                              |
| Notification Service | http://localhost:8084                              |
| Kafka UI             | http://localhost:8091                              |
| RabbitMQ Dashboard   | http://localhost:15672 (rabbit_user / rabbit_pass) |
| Prometheus           | http://localhost:9090                              |
| Grafana              | http://localhost:3000 (admin / admin123)           |

---

## 📡 API Documentation

All requests go through **API Gateway on port 8080**.

### Authentication

#### Register

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Sagar Pokale",
  "email": "sagar@example.com",
  "password": "password123"
}
```

#### Login

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "sagar@example.com",
  "password": "password123"
}
```

Response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "Sagar Pokale",
  "email": "sagar@example.com",
  "role": "ROLE_USER"
}
```

#### Get Current User (Protected)

```http
GET http://localhost:8080/api/users/me
Authorization: Bearer <token>
```

### Events

#### Publish Event (Protected)

```http
POST http://localhost:8080/api/events
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": "123",
  "eventType": "ORDER_PLACED",
  "payload": "{\"orderId\": \"ORD-001\", \"amount\": 500}"
}
```

Supported event types:

- `ORDER_PLACED`
- `PAYMENT_SUCCESS`
- `PAYMENT_FAILED`
- `USER_REGISTERED`
- `NOTIFICATION_SENT`

Response:

```json
{
  "status": "accepted",
  "eventId": "uuid",
  "eventType": "ORDER_PLACED",
  "userId": "123",
  "timestamp": "2026-03-13T..."
}
```

### WebSocket

Connect to WebSocket endpoint:

```
ws://localhost:8084/ws
```

Subscribe to notifications:

```javascript
// All notifications (broadcast)
stompClient.subscribe("/topic/notifications", callback);

// User-specific notifications
stompClient.subscribe("/user/queue/notifications", callback);
```

---

## 📊 Monitoring

### Prometheus

Access metrics at: http://localhost:9090

All Spring Boot services expose metrics at `/actuator/prometheus`.

### Grafana

Access dashboards at: http://localhost:3000

Login: `admin` / `admin123`

Metrics available:

- HTTP request rate and latency per service
- JVM memory and CPU usage
- Kafka consumer lag
- Active connections

---

## 💡 Design Decisions

### Why Kafka for events (not RabbitMQ)?

Kafka provides **persistent, replayable event logs** with consumer group support — meaning multiple services (event-consumer + notification-service) can independently consume the same events without coordination. RabbitMQ deletes messages after consumption, making it better suited for task queues like notification delivery.

### Why two separate consumer groups?

```
event-consumer-group       → saves to MySQL + Redis
notification-consumer-group → sends WebSocket + queues notifications
```

Each group gets every event independently. If notification-service goes down, it replays missed events from Kafka when it restarts — zero message loss.

### Why Redis for deduplication?

Kafka guarantees **at-least-once** delivery — the same event could arrive twice during failures. Redis provides O(1) lookups with TTL expiry to deduplicate events before DB writes, preventing duplicate records without expensive DB queries.

### Why RabbitMQ for notifications (not Kafka)?

Notifications are **task-oriented** — each notification should be processed by exactly one consumer, with retry logic and dead-letter queues for failures. RabbitMQ's competing consumers model is perfect for this, whereas Kafka's log model would require more complex offset management.

### Multi-stage Docker builds

All services use multi-stage Dockerfiles (build stage with full JDK + Maven, run stage with JRE-only Alpine) reducing final image sizes from ~600MB to ~180MB.

## 📁 Project Structure

```
event-stream-notification-platform/
│
├── api-gateway-service/
│   ├── src/main/java/com/platform/api_gateway_service/
│   │   ├── config/GatewayConfig.java
│   │   ├── filter/AuthFilter.java
│   │   └── util/JwtUtil.java
│   ├── Dockerfile
│   └── pom.xml
│
├── user-service/
│   ├── src/main/java/com/platform/user_service/
│   │   ├── config/SecurityConfig.java
│   │   ├── controller/AuthController.java
│   │   ├── dto/
│   │   ├── entity/User.java
│   │   ├── repository/UserRepository.java
│   │   ├── security/JwtUtil.java + JwtAuthFilter.java
│   │   └── service/UserService.java
│   ├── Dockerfile
│   └── pom.xml
│
├── event-producer-service/
│   ├── src/main/java/com/platform/event_producer_service/
│   │   ├── config/KafkaConfig.java
│   │   ├── controller/EventController.java
│   │   ├── dto/EventRequest.java + KafkaEvent.java
│   │   └── service/EventProducerService.java
│   ├── Dockerfile
│   └── pom.xml
│
├── event-consumer-service/
│   ├── src/main/java/com/platform/event_consumer_service/
│   │   ├── consumer/EventConsumer.java
│   │   ├── dto/KafkaEvent.java
│   │   ├── entity/ProcessedEvent.java
│   │   ├── repository/ProcessedEventRepository.java
│   │   └── service/EventProcessingService.java
│   ├── Dockerfile
│   └── pom.xml
│
├── notification-service/
│   ├── src/main/java/com/platform/notification_service/
│   │   ├── config/RabbitMQConfig.java + WebSocketConfig.java
│   │   ├── consumer/KafkaEventConsumer.java + RabbitMQNotificationConsumer.java
│   │   ├── dto/KafkaEvent.java + NotificationMessage.java
│   │   └── service/NotificationService.java
│   ├── Dockerfile
│   └── pom.xml
│
├── docker-compose.yml
├── prometheus/prometheus.yml
├── grafana/provisioning/
├── init-scripts/01_init.sql
└── README.md
```

---

## 🤝 Author

**Sagar Pokale**

- GitHub: [@SagarPokale2000](https://github.com/SagarPokale2000/notification-service)
- LinkedIn: [@SagarPokale](https://www.linkedin.com/in/sagar-pokale-2707a5135/)

---