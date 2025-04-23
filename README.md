# 🧠 CQRS-Based Scalable Social Media Backend

This project implements a scalable backend architecture for a social media platform using the **CQRS (Command Query Responsibility Segregation)** pattern, aiming to handle high read-to-write ratios and ensure low-latency content delivery.

---

## 📌 Problem Statement

Traditional social media platforms struggle to maintain performance under high read loads:

- Hot content causes **database bottlenecks**.
- Queries for posts, comments, and likes require **low-latency access**.
- Existing monolithic architectures can't scale reads and writes independently.

---

## 🧩 Solution Overview

This architecture follows the **CQRS principle** (Command Query Responsibility Segregation), separates read/write loads, uses Kafka for async reliability, and relies on Redis for performance:
<img width="793" alt="image" src="https://github.com/user-attachments/assets/01846080-8fad-4730-bec9-edc31428bcd0" />


---

## 🔁 Request Types

| Request Type | Processing Path |
|--------------|------------------|
| `GET`        | Query Path       |
| `POST`       | Command Path     |

---

## 🔍 GET Request Flow (Read Path)

1. A user sends a `GET` request.
2. The request is routed through an **Application Load Balancer**.
3. It is distributed to one of the auto-scaled **GET servers**.
4. The server checks **Redis (ElastiCache)**:
   - If a cache hit: returns immediately.
   - If a cache miss: queries the **MySQL Read Replica (Slave DB)**.
5. Read replicas stay in sync with the primary database.

> ✅ This path enables fast reads and reduces pressure on the main DB.

---

## ✍️ POST Request Flow (Write Path)

1. A user sends a `POST` request (e.g., liking a post).
2. The **POST server** receives the request.
3. Instead of writing to the database directly, it:
   - Sends the event to **Kafka** as a message.
4. A **Kafka consumer** processes the event:
   - Writes data to the **MySQL Primary DB**.
   - Updates **Redis** (write-through caching).
   - Optionally logs the action or triggers downstream services.

> ✅ This ensures fast response time, high throughput, and flexible decoupled processing.

---

## 🛢️ Database: Read/Write Split

| Role       | Description                                |
|------------|--------------------------------------------|
| Master DB  | Handles all **write** operations (via Kafka consumer). |
| Slave DBs  | Handles all **read** operations.           |

- Data is replicated from the master to slaves asynchronously.

---

## 🚀 Key Benefits

- ✅ **Read/Write separation** improves load distribution
- ✅ **Redis caching** reduces DB pressure and latency
- ✅ **Kafka async writes** provide better scalability and decoupling
- ✅ **Auto-scaling GET servers** handle traffic spikes
- ✅ **Event-driven design** allows easy integration with new consumers

---

---

## 📊 Performance Evaluation

We use **Apache JMeter** to simulate realistic load and capture metrics.

### Load Testing Setup

| Endpoint       | Threads | Iterations |
| -------------- | ------- | ---------- |
| GET /post/{id} | XXX     | XXX        |
| POST /post     | XX      | XXX        |

### Key Metrics


---


## 🔮 Future Work

- [ ] Add **WebSocket-based real-time notifications**
- [ ] Implement **monitoring dashboards** (e.g., Prometheus + Grafana)
- [ ] Support **hashtags**, **follows**, and **ranked feeds**

---

## 👩‍💻 Author

**Yuxi Chen** — CS Graduate Student @ Northeastern | Product-to-SDE Transitioner  
[GitHub](https://github.com/) | [LinkedIn](https://linkedin.com/)

**Zihe Chen** — CS Graduate Student @ Northeastern |  
[GitHub](https://github.com/) | [LinkedIn](https://linkedin.com/)

**Yinuo Feng** — CS Graduate Student @ Northeastern |  
[GitHub](https://github.com/) | [LinkedIn](https://linkedin.com/)

---

## 💬 Questions?

Feel free to open an issue or message me if you'd like to learn more about CQRS or backend scaling strategies!
