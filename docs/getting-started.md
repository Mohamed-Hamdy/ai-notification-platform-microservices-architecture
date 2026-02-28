---
layout: default
title: Getting Started
nav_order: 2
---

# Getting Started

This guide will help you set up and run the AI Notification Platform on your local machine.

## Prerequisites

### Required Software

1. **Java 17**
    - macOS: `brew install openjdk@17`
    - Linux: `sudo apt install openjdk-17-jdk`
    - Windows: Download from [Adoptium](https://adoptium.net/temurin/releases/?version=17)
    - Verify: `java -version`

2. **Maven**
    - macOS: `brew install maven`
    - Linux: `sudo apt install maven`
    - Windows: Download from [Maven](https://maven.apache.org/download.cgi)
    - Verify: `mvn -version`

3. **Docker & Docker Compose**
    - macOS/Windows: [Docker Desktop](https://www.docker.com/products/docker-desktop/)
    - Linux: `sudo apt install docker.io docker-compose`
    - Verify: `docker --version && docker-compose --version`

## Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ai-notification-platform
```

### 2. Start Infrastructure Services

Start PostgreSQL, Redis, Kafka, and Zookeeper:

```bash
docker-compose up -d
```

Verify all services are running:

```bash
docker-compose ps
```

All services should show status as "Up" or "healthy". Wait approximately 30-60 seconds for Kafka to fully initialize.

### 3. Start Microservices

You have two options to start the services:

#### Option A: Run with Maven (Development)

Open 3 separate terminal windows:

**Terminal 1 - notification-service:**
```bash
cd notification-service
mvn spring-boot:run
```

**Terminal 2 - worker-service:**
```bash
cd worker-service
mvn spring-boot:run
```

**Terminal 3 - ai-service:**
```bash
cd ai-service
mvn spring-boot:run
```

#### Option B: Build and Run JAR Files

Build all services:
```bash
cd notification-service && mvn clean package -DskipTests && cd ..
cd worker-service && mvn clean package -DskipTests && cd ..
cd ai-service && mvn clean package -DskipTests && cd ..
```

Run services:
```bash
java -jar notification-service/target/notification-service-1.0.0.jar &
java -jar worker-service/target/worker-service-1.0.0.jar &
java -jar ai-service/target/ai-service-1.0.0.jar &
```

## Verify Installation

### Check Service Health

```bash
# Notification Service
curl http://localhost:8081/notifications/health

# AI Service
curl http://localhost:8083/ai/health

# Kafka UI
open http://localhost:8080
```

### Create a Test Notification

```bash
curl -X POST http://localhost:8081/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "test@example.com",
    "subject": "Test Notification",
    "message": "Hello from the notification platform!",
    "channel": "EMAIL"
  }'
```

Expected response:
```json
{
  "id": 1,
  "status": "PENDING",
  "message": "Notification created successfully"
}
```

### Monitor Processing

Check the worker-service terminal/logs to see the notification being processed. You should see:
- Message consumed from Kafka
- Status changed to PROCESSING
- Simulated sending (2 second delay)
- Status changed to SENT

## Troubleshooting

### Port Already in Use

If you see "Port already in use" errors:

1. Check what's using the port:
   ```bash
   lsof -i :8081  # or :8083, :5432, etc.
   ```

2. Stop the conflicting process or change the port in `application.yml`

### Kafka Connection Issues

If services can't connect to Kafka:

1. Check Kafka status:
   ```bash
   docker logs notification-kafka
   ```

2. Wait for the message: "Kafka Server started"

3. Restart services if they started before Kafka was ready

### Database Connection Issues

If you see database connection errors:

1. Check PostgreSQL status:
   ```bash
   docker exec notification-postgres pg_isready
   ```

2. Verify database exists:
   ```bash
   docker exec -it notification-postgres psql -U postgres -c "\l"
   ```

### Maven Build Issues

If Maven build fails:

1. Clean and retry:
   ```bash
   mvn clean install -U
   ```

2. Delete local Maven repository cache:
   ```bash
   rm -rf ~/.m2/repository
   mvn clean install
   ```

## Next Steps

- [API Reference](api-reference.html) - Learn about available endpoints
- [Architecture](architecture.html) - Understand the system design
- [Development Guide](development.html) - Start contributing
