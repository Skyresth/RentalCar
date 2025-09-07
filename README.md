# Rental Car System - Development and Decisions Document

## 1. Overview
This project implements a **Car Rental Management System** using Java 21, Spring Boot 3.5, and a hexagonal architecture with vertical slicing.
Reason for non traditional hexagonal and better with vertical slicing can be explained perfectly in this [video](https://youtu.be/eNFAJbWCSww?si=ClUQotR_oaYyJrt5&t=131) and is set in the exact explanation.
We keep the core clean and isolated.
The system supports:
- Managing an inventory of cars
- Renting cars with correct pricing policies
- Returning cars with surcharges for late returns
- Awarding loyalty points to customers
- Querying rental history

We progressively evolved the system from an in-memory prototype to a production-ready service with a real PostgreSQL database.
Desing - Develop - Test - Validate - Reformat flow.
---

## 2. Development Decisions

### 2.1 Initial Focus
First we prioritized **core business functionality** over infrastructure:
- ✅ Implemented domain models (`Car`, `Customer`, `Rental`)
- ✅ Added pricing rules (Premium, SUV, Small cars)
- ✅ Added loyalty rules (points per rental)
- ✅ Created REST controllers for renting, returning, and querying
- ✅ Used an in-memory database (H2) with a `BootstrapData` seeder

This is mainly for a faster local development and validation, later we took did a docker instance with postgres like a production ready MVP project.

### 2.2 Later Improvements
Once the business logic was stable, we upgraded the infrastructure:
- ✅ Introduced **PostgreSQL** as a persistent database, running in Docker
- ✅ Added **Flyway migrations** to manage schema and seed data consistently
- ✅ Configured multiple Spring profiles:
  - `dev-h2`: for fast local development with in-memory DB
  - `postgres`: for real persistence with Dockerized PostgreSQL (also another for test)
- ✅ Added extensive **unit tests** for:
  - Controllers (REST endpoints)
  - Persistence adapters (JPA mappings)
  - Application use cases (`RentCarUseCase`, `ReturnCarUseCase`, `ListRentalsUseCase`)
  - Domain aggregates (`Rental`)
- ✅ Solved Flyway + PostgreSQL 16 compatibility by adding `flyway-database-postgresql`
- ✅ Exported Postman collection for a quick setup to test the endpoints.

### 2.3 Deferred Improvements (Not First Priority)
Some areas were identified as possible enhancements but postponed:
- ❌ Swagger implementation and endpoint documentation (a correct setup would have taken more time that I wanted, got most important endpoints in paper and added additional as I need them)
- ❌ Containerization of the Spring Boot app itself (only DB is containerized)
- ❌ Integration tests with **Testcontainers** (planned but not needed initially)
- ❌ CI/CD pipeline automation
- ❌ Advanced observability (Micrometer, Prometheus, Grafana dashboards)
- ❌ Authentication/Authorization (e.g., Spring Security, Cypher in password, etc)
- ❌ Circuit breaker for db when not available
- ❌ Time handling, we use LocalDate.now fine for this scope, but if multi-TZ users appear we need to inject a Clock (I preview this problem but since is not in spec I ignored it)
- ❌ Concurrent booking flow flips availability in cars atomically, a pessimistic lock on cars(id) or unique constraint would help prevent that problem

These were not prioritized because our main goal was to **get the rental flows working end-to-end**, taking more than that would take several hours and did not want to lose focus on the main objective.

---

## 3. How to Run the System

### 3.1 Requirements
- Java 21
- Maven 3.9+
- Docker & Docker Compose

### 3.2 Run with PostgreSQL (default)
1. Start PostgreSQL via Docker:
   ```bash
   docker compose up -d
   ```
   This starts:
   - PostgreSQL at `localhost:5432` (user: `carrental`, pass: `carrental`)
   - Adminer UI at `http://localhost:8081`

2. Run the Spring Boot app:
   ```bash
   ./mvnw spring-boot:run
   ```

3. API is available at `http://localhost:8080`.

Example requests:
- Rent a car:
  ```bash
  curl -X POST http://localhost:8080/rentals        -H "Content-Type: application/json"        -d '{"customerId":1,"carId":4,"days":9}'
  ```
- Return a car:
  ```bash
  curl -X POST http://localhost:8080/rentals/1/return        -H "Content-Type: application/json"        -d '{"actualReturnDate":"2025-09-18"}'
  ```
- Query customers:
  ```bash
  curl http://localhost:8080/customers
  ```

### 3.3 Run with H2 (in-memory)
For quick testing without Docker:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev-h2
```

---

## 4. Conclusion
We started by focusing on **core rental logic**, then improved **persistence and infrastructure**.  
We deferred advanced topics (CI/CD, observability, security) to keep the scope realistic.  
The system is now stable, tested, and ready for extension.
