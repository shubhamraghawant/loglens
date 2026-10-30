# 🔭 LogLens — Distributed Log Aggregation & Real-Time Alerting Engine

![Java](https://img.shields.io/badge/Java-21-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=springboot)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-7.5-black?logo=apachekafka)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![OpenAPI](https://img.shields.io/badge/OpenAPI-Swagger-green?logo=swagger)
![License](https://img.shields.io/badge/License-MIT-green)

A centralized **observability and log aggregation platform** engineered to collect, index, and alert on high-velocity distributed microservice logs in real time. Built with **Java 21, Spring Boot 3.2, Apache Kafka, and PostgreSQL**.

---

## 🏛️ Architecture Overview

```
log-producer (:8091) [Simulates Order, Payment, User, Inventory, Notification Services]
        │
        ▼ Kafka topic: 'logs.raw'
        │
log-aggregator (:8092)
        ├──▶ Persists raw structured log to PostgreSQL ('log_entries')
        │
        └──▶ ThresholdAlertEngine (Sliding window deque per service+level)
                 │ If ERROR > 10/min ➔ Severity WARN
                 │ If FATAL > 3/min  ➔ Severity CRITICAL
                 ▼ Kafka topic: 'logs.alerts' + PostgreSQL ('alerts')
log-query-api (:8093)
        ├──▶ GET /api/v1/logs (Filter by service, level, datetime range)
        ├──▶ GET /api/v1/logs/stats (Aggregated 24h error breakdown)
        └──▶ PATCH /api/v1/alerts/{id}/acknowledge (Operator incident triage)
```

### Microservices Breakdown
* **`log-producer`** (`:8091`): Generates synthetic high-throughput JSON logs across 5 business domains (`order-service`, `payment-service`, `user-service`, `inventory-service`, `notification-service`) with configurable error spikes.
* **`log-aggregator`** (`:8092`): High-throughput Kafka consumer that batches raw log persistence into PostgreSQL and feeds an in-memory sliding-window anomaly detection engine.
* **`log-query-api`** (`:8093`): REST query interface providing paginated log search, multi-criteria filtering (service, level, traceId, timestamps), service health stats, and alert acknowledgement workflows.

---

## ✨ Key Technical Highlights

* **Concurrent Sliding-Window Anomaly Detection:** Uses thread-safe deques per service/severity key (`ConcurrentHashMap<String, Deque<Instant>>`) with atomic sliding-window pruning (<60s) to detect error storms (>10 ERRORs/min or >3 FATALs/min) with zero database overhead.
* **High-Throughput Ingestion:** Leverages Kafka consumer groups with JDBC batch inserts (`hibernate.jdbc.batch_size=50`) to sustain high write velocity without broker lag.
* **Operator Alert Triage:** Full lifecycle incident workflows (`TRIGGERED` ➔ `ACKNOWLEDGED` ➔ `RESOLVED`) allowing SREs to investigate correlated logs instantly via trace IDs.
* **Microservices Health Statistics:** Exposes aggregated error ratios and service distribution stats across rolling 24-hour windows.
* **Turnkey Orchestration:** Single-command Docker Compose setup booting Zookeeper, Kafka, PostgreSQL, and all 3 LogLens microservices.

---

## 🚀 Quick Start

### Prerequisites
* **Docker** & **Docker Compose**
* **JDK 21** & **Maven 3.9+** (for local development)

### 1. Clone the repository
```bash
git clone https://github.com/shubhamraghawant/loglens.git
cd loglens
```

### 2. Run with Docker Compose
```bash
docker compose up --build -d
```

### 3. Verify services
* `log-query-api` (Swagger UI): http://localhost:8093/swagger-ui.html
* `log-producer` (Simulated Traffic): http://localhost:8091/swagger-ui.html
* `log-aggregator`: http://localhost:8092/actuator/health

---

## 📡 API Usage Examples

### 1. Trigger Synthetic Log Stream / Anomaly Simulation
```bash
# Simulates traffic or error spikes from producer
curl -X POST http://localhost:8091/api/v1/producer/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "payment-service",
    "logLevel": "ERROR",
    "count": 15
  }'
```

### 2. Search Logs with Filters
```bash
curl "http://localhost:8093/api/v1/logs?serviceName=payment-service&level=ERROR&page=0&size=10"
```

### 3. Retrieve 24-Hour System Stats
```bash
curl http://localhost:8093/api/v1/logs/stats
```

### 4. Acknowledge an Alert
```bash
curl -X PATCH http://localhost:8093/api/v1/alerts/{alert-id}/acknowledge \
  -H "Content-Type: application/json" \
  -d '{"acknowledgedBy": "sre-engineer-shubham"}'
```

---

## 🧪 Testing

Run all unit and integration tests across the multi-module project:
```bash
mvn clean test
```

---

## 📄 License
This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
