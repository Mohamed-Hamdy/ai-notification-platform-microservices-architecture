# AI Notification Platform

A production-ready microservices-based notification platform built with Spring Boot 3.x and Java 17. The system provides REST APIs for creating notifications, processes them asynchronously via Kafka, and includes AI-powered content optimization.

## Architecture Overview

This platform consists of 3 independent microservices:

1. **notification-service** - REST API entry point for notification creation
2. **worker-service** - Background processing engine for sending notifications
3. **ai-service** - AI optimization layer for content enhancement

See [Architecture Documentation](docs/architecture.md) for detailed information.

## Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.2.0
- **Build Tool**: Maven
- **Database**: PostgreSQL 15
- **Message Broker**: Apache Kafka
- **Cache**: Redis
- **Containerization**: Docker & Docker Compose

## Prerequisites

### 1. Install Java 17

#### macOS
```bash
brew install openjdk@17
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

#### Windows
Download and install from: https://adoptium.net/temurin/releases/?version=17

Verify installation:
```bash
java -version
```

### 2. Install Maven

#### macOS
```bash
brew install maven
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install maven
```

#### Windows
Download from: https://maven.apache.org/download.cgi
Extract and add to PATH

Verify installation:
```bash
mvn -version
```

### 3. Install Docker

#### macOS
Download Docker Desktop: https://www.docker.com/products/docker-desktop/

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install docker.io docker-compose
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER
```

#### Windows
Download Docker Desktop: https://www.docker.com/products/docker-desktop/

Verify installation:
```bash
docker --version
docker-compose --version
```

## Getting Started

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

Wait for all services to be healthy (approximately 30-60 seconds).

### 3. Start Microservices

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

**Build all services:**
```bash
cd notification-service && mvn clean package -DskipTests && cd ..
cd worker-service && mvn clean package -DskipTests && cd ..
cd ai-service && mvn clean package -DskipTests && cd ..
```

**Run services:**
```bash
java -jar notification-service/target/notification-service-1.0.0.jar &
java -jar worker-service/target/worker-service-1.0.0.jar &
java -jar ai-service/target/ai-service-1.0.0.jar &
```

## Service Endpoints

| Service | Port | Health Check |
|---------|------|--------------|
| notification-service | 8081 | http://localhost:8081/notifications/health |
| ai-service | 8083 | http://localhost:8083/ai/health |
| Kafka UI | 8080 | http://localhost:8080 |

## API Usage Examples

### 1. Create a Notification

```bash
curl -X POST http://localhost:8081/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "user@example.com",
    "subject": "Welcome to our platform",
    "message": "Thank you for signing up!",
    "channel": "EMAIL"
  }'
```

**Response:**
```json
{
  "id": 1,
  "status": "PENDING",
  "message": "Notification created successfully"
}
```

### 2. Optimize Content with AI

```bash
curl -X POST http://localhost:8083/ai/optimize \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Welcome to our platform",
    "message": "Thank you for signing up!",
    "channel": "EMAIL"
  }'
```

**Response:**
```json
{
  "originalSubject": "Welcome to our platform",
  "optimizedSubject": "🚀 Exclusive: Welcome to our platform",
  "originalMessage": "Thank you for signing up!",
  "enhancedMessage": "Thank you for signing up!\n\n✨ This message has been AI-optimized for maximum engagement.",
  "optimizationStrategy": "Power word injection with emoji enhancement",
  "confidenceScore": 0.87
}
```

### 3. Test Complete Flow

```bash
curl -X POST http://localhost:8081/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "test@example.com",
    "subject": "Test Notification",
    "message": "This is a test message",
    "channel": "EMAIL"
  }'
```

Check the worker-service logs to see the processing:
```bash
docker logs -f notification-worker
```

## Monitoring

### View Kafka Messages

Access Kafka UI: http://localhost:8080

Navigate to Topics > notification.requested to see message flow.

### Check Database

Connect to PostgreSQL:
```bash
docker exec -it notification-postgres psql -U postgres -d notification_db
```

Query notifications:
```sql
SELECT * FROM notifications ORDER BY created_at DESC LIMIT 10;
```

### View Logs

**notification-service:**
```bash
tail -f notification-service/logs/application.log
```

**worker-service:**
```bash
tail -f worker-service/logs/application.log
```

## Testing Scenarios

### Test Retry Mechanism

The worker-service simulates random failures (20% chance) to demonstrate retry logic:

1. Create multiple notifications
2. Monitor worker-service logs
3. Observe automatic retry attempts
4. Check notification status changes: PENDING → PROCESSING → SENT/RETRY/FAILED

### Test Different Channels

```bash
curl -X POST http://localhost:8081/notifications \
  -H "Content-Type: application/json" \
  -d '{"recipient": "user@example.com", "subject": "Test", "message": "SMS test", "channel": "SMS"}'

curl -X POST http://localhost:8081/notifications \
  -H "Content-Type: application/json" \
  -d '{"recipient": "user@example.com", "subject": "Test", "message": "Push test", "channel": "PUSH"}'
```

## Stopping Services

### Stop Microservices

If running with Maven, press `Ctrl+C` in each terminal.

If running as JAR files:
```bash
pkill -f notification-service
pkill -f worker-service
pkill -f ai-service
```

### Stop Infrastructure

```bash
docker-compose down
```

To remove volumes (database data):
```bash
docker-compose down -v
```

## Troubleshooting

### Port Already in Use

If ports 8080, 8081, 8083, 5432, 6379, or 9092 are in use:

1. Stop conflicting services
2. Or modify ports in `application.yml` and `docker-compose.yml`

### Kafka Connection Issues

Ensure Kafka is fully started:
```bash
docker logs notification-kafka
```

Wait for the message: "Kafka Server started"

### Database Connection Issues

Check PostgreSQL status:
```bash
docker exec notification-postgres pg_isready
```

## Project Structure

```
ai-notification-platform/
├── notification-service/
│   ├── src/main/java/com/notification/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── dto/
│   │   ├── config/
│   │   └── NotificationServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── worker-service/
│   ├── src/main/java/com/worker/
│   │   ├── consumer/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── dto/
│   │   ├── config/
│   │   ├── scheduler/
│   │   └── WorkerServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── ai-service/
│   ├── src/main/java/com/ai/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dto/
│   │   └── AiServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── docs/
│   └── architecture.md
├── docker-compose.yml
└── README.md
```

## Development

### Building Individual Services

```bash
cd notification-service
mvn clean install

cd ../worker-service
mvn clean install

cd ../ai-service
mvn clean install
```

### Running Tests

```bash
mvn test
```

## Production Considerations

For production deployment, consider:

1. **Security**: Implement authentication/authorization (Spring Security, JWT)
2. **Configuration**: Use external config server (Spring Cloud Config)
3. **Service Discovery**: Add Eureka or Consul
4. **API Gateway**: Implement Spring Cloud Gateway
5. **Monitoring**: Add Prometheus, Grafana, ELK stack
6. **Distributed Tracing**: Implement Sleuth/Zipkin
7. **Circuit Breaker**: Add Resilience4j
8. **Database Migration**: Use Flyway or Liquibase
9. **Load Balancing**: Configure proper load balancers
10. **Kubernetes**: Deploy to K8s cluster with proper scaling

## License

MIT License

## Support

For issues and questions, please open a GitHub issue.
