
# High-Performance Hotel Booking API

![Java](https://img.shields.io/badge/Java-26-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)
![Redis](https://img.shields.io/badge/Redis-Caching-red.svg)
![MySQL](https://img.shields.io/badge/MySQL-Database-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED.svg)
![JMeter](https://img.shields.io/badge/Apache%20JMeter-Performance%20Tested-D22128.svg)

An enterprise-grade, highly concurrent REST API for hotel room management and reservations. Engineered with a focus on **strict transactional integrity**, **stateless security**, and **extreme read-heavy performance optimization**. 

By integrating a Redis caching layer and fine-tuning Spring Data JPA, this architecture successfully processes mixed-traffic loads of 20,000+ requests with sub-10ms read latencies while mathematically eliminating race conditions (double-bookings) during high-concurrency booking spikes.

---

##  System Architecture

```mermaid
flowchart TD
    Client["🌐 Client (Browser / Postman / SPA)"]

    subgraph SEC["🔐 Security Layer"]
        direction TB
        SecConf["SecurityConfig<br/>Stateless · CSRF off · BCrypt"]
        Filter["JwtAuthenticationFilter<br/>(OncePerRequestFilter)"]
        JwtSvc["JwtService<br/>JJWT 0.11.5 · HS256 · 24h expiry"]
        UDS["CustomUserDetailsService"]
    end

    subgraph CTRL["🎮 Controllers"]
        direction TB
        AuthC["AuthController<br/>POST /api/auth/login"]
        RoomC["RoomController<br/>POST /api/rooms (ADMIN)<br/>GET /api/rooms/available (public)"]
        BookC["BookingController<br/>POST /api/bookings (public)<br/>GET /api/bookings"]
    end

    subgraph SVC["⚙️ Services"]
        direction TB
        RoomS["RoomService<br/>@Cacheable / @CacheEvict"]
        BookS["BookingService<br/>@Transactional(timeout=5)"]
    end

    subgraph REPO["🗄️ Repositories (Spring Data JPA)"]
        direction TB
        UserR["UserRepository<br/>findByUsername"]
        RoomR["RoomRepository<br/>findByIdWithLock (PESSIMISTIC_WRITE)<br/>findAvailableRooms"]
        BookR["BookingRepository<br/>countOverlappingBookings"]
    end

    MySQL[("MySQL 8<br/>hotel_booking_db")]
    Redis[("Redis<br/>availableRooms · TTL 10m")]
    EXC["GlobalExceptionHandler"]

    Client -->|"username + password"| AuthC
    AuthC --> SecConf
    SecConf --> UDS
    UDS --> UserR --> MySQL
    AuthC -->|"generateToken()"| JwtSvc
    JwtSvc -->|"JWT returned"| Client

    Client -->|"Authorization: Bearer JWT"| Filter
    Filter --> JwtSvc
    Filter --> UDS
    Filter -->|"sets SecurityContext"| RoomC
    Filter -->|"sets SecurityContext"| BookC
    Client -.->|"public routes bypass auth"| RoomC
    Client -.->|"public routes bypass auth"| BookC

    RoomC --> RoomS
    BookC --> BookS

    RoomS <-->|"cache hit / miss"| Redis
    RoomS --> RoomR --> MySQL

    BookS -->|"1 lock room row"| RoomR
    BookS -->|"2 check date overlap"| BookR
    BookS -->|"3 save booking"| BookR
    BookR --> MySQL

    RoomS -.-> EXC
    BookS -.->|"RoomUnavailableException(409)<br/>IllegalArgumentException(400)<br/>LockTimeout(503)"| EXC
    EXC -.->|"ErrorResponse JSON"| Client

    classDef sec fill:#fde2e2,stroke:#c0392b,color:#000
    classDef ctrl fill:#d6eaf8,stroke:#2980b9,color:#000
    classDef svc fill:#d5f5e3,stroke:#27ae60,color:#000
    classDef repo fill:#fcf3cf,stroke:#b7950b,color:#000
    
    class SecConf,Filter,JwtSvc,UDS sec
    class AuthC,RoomC,BookC ctrl
    class RoomS,BookS svc
    class UserR,RoomR,BookR repo

```

### Core Architectural Choices:

1. **Cache-Aside Pattern (Redis):** Implemented to intercept read-heavy operations (`GET /api/rooms/available`). Offloads repetitive querying from the primary MySQL database, dramatically reducing latency.
2. **Stateless Authentication (JWT):** Utilizes Spring Security with a custom `JwtAuthenticationFilter` placed before standard Spring filters to ensure zero session overhead on the server, allowing horizontal scalability.
3. **Pessimistic Locking / ACID Transactions:** Booking writes (`POST /api/bookings`) are strictly handled to prevent race conditions using `PESSIMISTIC_WRITE` locks in the Repository layer. Conflicts are safely rejected with HTTP 409, maintaining 100% database integrity under massive concurrent load.

---
##  API Documentation (Swagger UI)

This project utilizes SpringDoc OpenAPI for interactive API documentation. Once the application is running, you can explore the endpoints, view request/response schemas, and inject your JWT token to test secured routes directly from your browser.

* **Swagger UI:** `http://localhost:8080/swagger-ui.html`
* **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

**To test secured endpoints in Swagger:**
1. Hit the `POST /api/auth/login` endpoint with the Admin credentials.
2. Copy the returned JWT string.
3. Click the **"Authorize"** button at the top of the Swagger UI.
4. Paste your token (the UI is configured to automatically append `Bearer ` if configured in your OpenAPI config, otherwise paste `Bearer YOUR_TOKEN`).

 ---
 
##  Performance Engineering & Benchmarks

To mathematically prove system scalability, the API was subjected to rigorous industry-standard performance testing using **Apache JMeter** (executed in CLI non-GUI mode to eliminate client-side bottlenecks).

<img width="739" height="473" alt="pic1" src="https://github.com/user-attachments/assets/8cd9bd05-9a6e-407e-9056-8482ab87f3c3" />


### 1. Real-World Mixed Traffic Simulation (The E-Commerce Funnel)

Simulated a peak holiday booking rush: **1,000 concurrent users** generating **20,000 requests** over 60 seconds with uniform random "think time" timers (1-4s). Traffic split was **80% Searches (Reads) and 20% Bookings (Writes)** using parameterized randomized CSV data.

| Metric | Target / Result | Analysis |
| --- | --- | --- |
| **Total Requests** | 20,000 | Successfully processed massive sustained load. |
| **Read (GET) Latency (p95)** | **17.00 ms** | Redis caching ensured 95% of users experienced sub-20ms load times during heavy concurrent writes. |
| **Read (GET) Error Rate** | **0.00%** | Flawless execution on the search endpoint under load. |
| **Write (POST) Integrity** | 3,680 Overlaps Blocked (92%) | Out of 4,000 booking attempts on 60 rooms, the system correctly identified overlapping race conditions, returning `409 Conflict` safely without a single `500 Server Error` or double-booking. |

### 2. The Baseline & Caching Impact (Read-Only Comparison)

Tested the `/api/rooms/available` endpoint purely to measure the delta between a Cache-Miss (MySQL) and a Cache-Hit (Redis).

* **1,000 Requests (Cache-Hit):** * Average Response: **7.72 ms**
* Error Rate: **0.00%**

<img width="756" height="480" alt="image" src="https://github.com/user-attachments/assets/75f32780-ebd8-4405-a8c2-80085f6b02d3" />



* **Comparison:** While a 1,000 user sustained load purely reading from Redis returned a 99th percentile response of 70ms, introducing 4,000 concurrent writes alongside 16,000 reads only increased the read 99th percentile slightly to 91ms, proving the caching layer effectively isolates read performance from write-heavy database locks.

### 3. Spike & Stress Testing Limits

Pushed the system to its breaking point to identify hardware/configuration limits.

* **Stress Test (3,000+ Sustained Load):** Handled continuous moderate load with a 100% pass rate.
* **Flash-Sale Spike (500-1000 Instant Users):** Identified system limits at massive instantaneous spikes (e.g., 500 requests in a single second). Error rates spiked to 25%-75% due to `HttpHostConnectException` as Tomcat max-connections/HikariCP pool limits were exceeded. This provided baseline data for future API Gateway rate-limiting (e.g., Resilience4j) and horizontal scaling requirements.

<img width="738" height="444" alt="image" src="https://github.com/user-attachments/assets/f15d5791-23bb-4542-a415-31b68962270b" />

---

##  Key Features & Architectural Highlights

* **High-Performance Caching (Redis):** Integrated a Cache-Aside pattern for read-heavy search operations (`GET /api/rooms/available`). This intercepts repetitive database queries, dropping read latencies to sub-10ms and drastically reducing MySQL load.
* **Concurrency Control & Double-Booking Prevention:** Implemented strict ACID-compliant transactions using `PESSIMISTIC_WRITE` row-level database locking. The system mathematically eliminates race conditions during traffic spikes, safely rejecting overlapping bookings with a `409 Conflict` rather than throwing server errors.
* **Stateless Security (JWT):** Engineered a custom Spring Security filter chain utilizing JSON Web Tokens. By eliminating server-side sessions, the API is completely stateless, highly secure, and optimized for horizontal scaling.
* **Role-Based Access Control (RBAC):** Enforced distinct permission boundaries, separating `ADMIN` privileges (room creation and system management) from standard `USER` traffic (searching and booking).
* **Dynamic & Validated Search:** Queries available rooms by parsing precise `LocalDate` parameters. Enforces strict DTO validation (e.g., `@FutureOrPresent`, `@NotNull`) to sanitize and verify inputs before they ever reach the service layer.
* **Global Exception Handling:** A centralized `@ControllerAdvice` intercepts internal Java exceptions (e.g., Redis Serialization errors, Lock Timeouts, JWT Expirations) and gracefully maps them to clean, standardized JSON `ErrorResponse` payloads for the client.
* **Automated Database Seeding:** Utilized Spring's `CommandLineRunner` to auto-initialize the MySQL database upon deployment, generating 60 distinct rooms (Standard, Deluxe, Suites) and a default Admin account for immediate testing.

---

##  Tech Stack

* **Backend Core:** Java 26, Spring Boot 4.1.0
* **Security:** Spring Security, JSON Web Tokens (io.jsonwebtoken)
* **Persistence:** Spring Data JPA, Hibernate, MySQL 8
* **Caching:** Spring Data Redis, Redis Server
* **Performance Testing:** Apache JMeter 5.6
* **Deployment (Upcoming):** Docker, Docker Compose

---

## Prerequisites

Before running this project locally, ensure you have the following installed:
* **Java 26** (JDK)
* **Maven 3.8+**
* **Docker & Docker Compose** (For Redis and MySQL containers)

##  Environment Variables
To run this application, you must configure the following properties in your `src/main/resources/application.properties` (or provide them via a `.env` file when using Docker). 

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_booking_db
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT Security
# Must be a 256-bit (32 byte) Base64 encoded string
jwt.secret=YOUR_SUPER_SECRET_BASE64_KEY_HERE
jwt.expiration=86400000
```
---
## Local Setup & Deployment (Docker)

*Configuration for Docker-Compose will be added here once local development environment variables are sanitized.*

```bash
# Clone the repository
git clone [https://github.com/Asmit159/hotel-booking-api.git](https://github.com/Asmit159/hotel-booking-api.git)

# Navigate to project
cd hotel-booking-api

# Build and run with Docker Compose (MySQL, Redis, and Spring Boot)
docker-compose up --build -d

```
