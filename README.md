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

The system adopts a **CQRS architecture** with decoupled services for reading and writing, and adds a lightweight access control layer for enhanced security on reads.:

### 🧱 Architecture Components

| Component       | Technology     | Role                                        |
| --------------- | -------------- | ------------------------------------------- |
| Command Service | MySQL          | Handles posts, comments, and likes (writes) |
| Query Service   | MongoDB, Redis | Serves read requests, supports caching      |
| Access Control  | AWS Lambda     | Verifies token before allowing data access  |

### 🔁 Data Flow

- User creates a post → Command Service writes to MySQL
- Internal sync script/process updates MongoDB and Redis directly
- User requests content →
  ① Request goes through AWS Lambda for token validation →
  ② If valid, Query Service serves data from Redis or MongoDB

---

## ⚙️ Tech Stack

- **MySQL** – Primary write database
- **MongoDB** – Flexible read store
- **Redis** – Hot data cache for low-latency access
- **JWT** – Token-based authentication to control access to GET requests
- **AWS Lambda** – Lightweight access control layer to validate JWT tokens before data access
- **JMeter** – Performance testing under concurrent load

---

## 🚀 Key Features

- **Read-Write Decoupling**: Enables independent scaling
- **Redis Caching**: Reduces latency for popular content
- **JWT Authentication**: Ensures that only authorized users can access query endpoints
- **AWS Lambda Access Control**: Adds a lightweight validation layer before data retrieval
- **Future-Ready**: Architecture supports real-time feeds and notification services

---

## 📊 Performance Evaluation

We use **Apache JMeter** to simulate realistic load and capture metrics.

### Load Testing Setup

| Endpoint       | Threads | Iterations |
| -------------- | ------- | ---------- |
| GET /post/{id} | XXX     | XXX        |
| POST /post     | XX      | XXX        |

### Key Metrics

- 🕒 **P99 Latency** (goal: < 50ms for reads)
- ⚡ **Throughput (TPS)** comparison before/after CQRS
- 📉 **Redis Hit Ratio** & DB load reduction
- 🔎 **Read efficiency** improvements

---

## 📂 Project Structure

- /command-service/ – Handles write operations and syncs data to read stores (MySQL → MongoDB/Redis)
- /query-service/ – Handles read operations with Redis caching and MongoDB fallback
- /access-control/ – AWS Lambda function for JWT validation before read access
- /jmeter-tests/ – Load testing scripts and test plans
- /docs/design.md – System architecture and implementation notes
- README.md – Project overview and usage guide

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
