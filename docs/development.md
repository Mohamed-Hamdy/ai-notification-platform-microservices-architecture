---
layout: default
title: Development
nav_order: 5
---

# Development Guide

Guide for developers contributing to the AI Notification Platform.

## Project Structure

```
ai-notification-platform/
├── notification-service/     # REST API entry point
│   ├── src/main/java/com/notification/
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic
│   │   ├── repository/      # Data access layer
│   │   ├── model/           # Entity models
│   │   ├── dto/             # Data transfer objects
│   │   └── config/          # Configuration classes
│   └── pom.xml
├── worker-service/          # Background processor
│   ├── src/main/java/com/worker/
│   │   ├── consumer/        # Kafka consumers
│   │   ├── service/         # Processing logic
│   │   ├── repository/      # Data access layer
│   │   ├── model/           # Entity models
│   │   ├── scheduler/       # Scheduled tasks
│   │   └── config/          # Configuration classes
│   └── pom.xml
├── ai-service/              # AI optimization
│   ├── src/main/java/com/ai/
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # AI logic
│   │   └── dto/             # Data transfer objects
│   └── pom.xml
├── docs/                    # Documentation
├── docker-compose.yml       # Infrastructure setup
└── README.md
```

## Building the Project

### Build All Services

```bash
mvn clean install
```

### Build Individual Service

```bash
cd notification-service
mvn clean package
```

### Skip Tests

```bash
mvn clean package -DskipTests
```

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Tests for Specific Service

```bash
cd notification-service
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=NotificationServiceTest
```

## Database Operations

### Connect to PostgreSQL

```bash
docker exec -it notification-postgres psql -U postgres -d notification_db
```

### Useful SQL Queries

```sql
-- View all notifications
SELECT * FROM notifications ORDER BY created_at DESC LIMIT 10;

-- Check notification status distribution
SELECT status, COUNT(*) FROM notifications GROUP BY status;

-- View failed notifications
SELECT * FROM notifications WHERE status = 'FAILED';

-- View recent notifications with retries
SELECT id, recipient, status, retry_count, error_message
FROM notifications
WHERE retry_count > 0
ORDER BY updated_at DESC;

-- Clear all notifications (for testing)
TRUNCATE TABLE notifications;
```

### Database Schema

The notifications table is automatically created by Hibernate on startup:

```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    channel VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    retry_count INTEGER DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

## Kafka Operations

### Access Kafka UI

Open http://localhost:8080 in your browser to:
- View topics
- Browse messages
- Monitor consumer groups
- Check partition distribution

### Kafka CLI Commands

Connect to Kafka container:
```bash
docker exec -it notification-kafka bash
```

List topics:
```bash
kafka-topics --list --bootstrap-server localhost:9092
```

Describe topic:
```bash
kafka-topics --describe --topic notification.requested --bootstrap-server localhost:9092
```

Consume messages:
```bash
kafka-console-consumer --topic notification.requested \
  --from-beginning --bootstrap-server localhost:9092
```

## Logging

### Log Levels

Configure in `application.yml`:

```yaml
logging:
  level:
    com.notification: DEBUG
    org.springframework.kafka: INFO
    org.hibernate: INFO
```

### View Logs

If running with Maven:
```bash
# Logs appear in the terminal
```

If running as JAR:
```bash
tail -f notification-service/logs/application.log
```

## Configuration

### Environment Variables

Services can be configured via environment variables:

```bash
# Database
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=notification_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

# Kafka
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Server
export SERVER_PORT=8081
```

### Application Properties

Edit `src/main/resources/application.yml` for each service:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:notification_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
```

## Code Style

### Java Conventions

- Use Lombok annotations to reduce boilerplate
- Follow Spring Boot best practices
- Use constructor injection (not field injection)
- Keep controllers thin, business logic in services
- Use DTOs for API requests/responses

### Example Service Class

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repository;
    private final KafkaProducerService kafkaService;

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification for: {}", request.getRecipient());

        // Implementation

        return response;
    }
}
```

## Adding New Features

### Adding a New Notification Channel

1. Update `NotificationRequest` validation
2. Add processing logic in `NotificationProcessingService`
3. Update AI optimization for the new channel
4. Add tests
5. Update documentation

### Adding New AI Optimization Strategy

1. Create new strategy method in `AiOptimizationService`
2. Add configuration if needed
3. Update response DTOs
4. Add tests
5. Update API documentation

## Testing Scenarios

### Test Successful Notification

```bash
curl -X POST http://localhost:8081/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "test@example.com",
    "subject": "Test",
    "message": "Test message",
    "channel": "EMAIL"
  }'
```

### Test Retry Mechanism

The worker-service simulates random failures (20% chance). Create multiple notifications and observe retry behavior in logs.

### Test AI Optimization

```bash
curl -X POST http://localhost:8083/ai/optimize \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Product Update",
    "message": "We have released new features",
    "channel": "EMAIL"
  }'
```

## Debugging

### Enable Debug Logging

In `application.yml`:
```yaml
logging:
  level:
    root: DEBUG
```

### Debug with IDE

1. Import project as Maven project
2. Set breakpoints in code
3. Run service in debug mode
4. Send API request to trigger breakpoint

### Common Issues

**Issue:** Kafka consumer not receiving messages
- Check Kafka is running: `docker ps`
- Verify topic exists in Kafka UI
- Check consumer group in Kafka UI

**Issue:** Database connection refused
- Verify PostgreSQL is running: `docker ps`
- Check connection details in `application.yml`
- Test connection: `docker exec notification-postgres pg_isready`

**Issue:** Port already in use
- Find process: `lsof -i :8081`
- Kill process: `kill -9 <PID>`
- Or change port in `application.yml`

## Deployment

### Docker Deployment

Build Docker images:
```bash
cd notification-service
docker build -t notification-service:latest .

cd ../worker-service
docker build -t worker-service:latest .

cd ../ai-service
docker build -t ai-service:latest .
```

Run with docker-compose:
```bash
docker-compose up -d
```

### Production Considerations

For production deployment:

1. **Security**
    - Implement Spring Security
    - Add JWT authentication
    - Enable SSL/TLS
    - Use secrets management (Vault, AWS Secrets Manager)

2. **Scalability**
    - Deploy multiple instances
    - Add load balancer
    - Increase Kafka partitions
    - Use read replicas for database

3. **Monitoring**
    - Add Prometheus metrics
    - Set up Grafana dashboards
    - Implement ELK stack for logging
    - Add distributed tracing (Zipkin/Jaeger)

4. **Reliability**
    - Implement circuit breakers (Resilience4j)
    - Add health checks
    - Set up alerts
    - Configure proper backup strategy

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write tests
5. Submit a pull request

## Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)
