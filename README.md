
# 📊 Fear & Greed Tracker Backend

This is the backend for a full-stack web application that tracks and visualizes market sentiment using the **Fear & Greed Index**. It automates data fetching, stores historical records in PostgreSQL, and exposes RESTful APIs for frontend consumption.

---

## ✅ Features

- 🔄 Fetch today's Fear & Greed value via an unofficial API.
- 📅 Automatically fetch today’s data if missing when requested by the frontend.
- 🗃️ Store and manage daily records in PostgreSQL.
- 📈 Provide REST APIs to retrieve:
  - Today’s data
  - Last N days' historical data
  - Data by month and year
- 🕐 Scheduled daily data fetching at **1:00 AM** server time.
- 🧹 Automatic deletion of data older than **5 years** every **1st of the month at 2:00 AM**.
- 🔁 One-time full historical data fetch capability.
- 🌐 Configured for CORS to allow secure frontend access.

---

## 🧰 Technology Stack

| Layer             | Tool/Library                     |
|------------------|----------------------------------|
| Backend Framework| Spring Boot 3.x (Java 17+)       |
| Database         | PostgreSQL                       |
| ORM              | Spring Data JPA (Hibernate)      |
| HTTP Client      | Spring RestTemplate              |
| JSON Handling    | Jackson (Spring Web)             |
| Build Tool       | Maven                            |
| Scheduling       | Spring `@Scheduled`              |
| Boilerplate Tool | Lombok                           |

---

## ⚙️ Architecture Overview

**Controller → Service → Repository → Database**

- `FearGreedService` handles all data fetch, store, and cleanup logic.
- External CNN API integration is handled asynchronously.
- `FearGreedApiController` exposes endpoints.
- Data is stored in a PostgreSQL table.

---

## 🗄️ Database Schema

```sql
CREATE TABLE fear_greed_index (
    id BIGSERIAL PRIMARY KEY,
    record_date DATE NOT NULL UNIQUE,
    fgi_value INTEGER NOT NULL CHECK (fgi_value >= 0 AND fgi_value <= 100),
    sentiment VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);
````

---

## 🛠️ Setup Instructions

### 1️⃣ Prerequisites

* Java 17+
* Apache Maven 3.x+
* PostgreSQL running (e.g., on `localhost:5432`)
* Recommended: IntelliJ IDEA or Eclipse

---

### 2️⃣ Database Setup

Open pgAdmin or terminal:

```sql
CREATE DATABASE fear_greed_db;

-- Optional: Create dedicated user
CREATE USER your_username WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE fear_greed_db TO your_username;
```

---

### 3️⃣ Configuration

Update the file:

📁 `src/main/resources/application.properties`

```properties
spring.application.name=fear-greed-tracker

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/fear_greed_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# API Endpoint
feargreed.api.url=Your Api to use

# Logging
logging.level.com.aurelius.fear_greed_tracker.Service=DEBUG
```

---

### 4️⃣ CORS Configuration

📁 `FearGreedTrackerApplication.java`

```java
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:5173", // Vite frontend
                    "http://127.0.0.1:5500", // Live Server
                    "http://localhost:8000"  // Static server
                    // Add production domain here: e.g., https://your-frontend.netlify.app
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
        }
    };
}
```

---

### 5️⃣ Build & Run

In terminal:

```bash
# Go to root project directory
mvn clean install

# Run the application
mvn spring-boot:run
```

Watch for log:

```log
Started FearGreedTrackerApplication in X.XXX seconds
```

---

## 📡 API Endpoints

All endpoints are prefixed with: `/api/fear-greed`

| Method | Endpoint                              | Description                                    |
| ------ | ------------------------------------- | ---------------------------------------------- |
| GET    | `/today`                              | Get today's FGI value (fetches if not present) |
| GET    | `/history?days=30`                    | Get last 30 days of data                       |
| GET    | `/history-by-month?year=2024&month=6` | Get data for specific month                    |
| GET    | `/fetch-now`                          | \[DEV] Fetch today’s data manually             |
| GET    | `/fetch-history-now`                  | \[DEV] Load full historical data once          |
| GET    | `/cleanup-old-data`                   | \[DEV] Delete entries older than 5 years       |

---

## 💡 Notes

* Data older than 5 years is **automatically purged** on the **1st of every month at 2:00 AM**.
* Daily fetch runs **automatically at 1:00 AM** server time.
* On-demand fetch ensures **frontend never receives stale or missing data**.

---

## 🧠 Credits

Built with ❤️ by \[Aurelius]

---

## 📝 License

MIT License. Free to use and modify.

```

