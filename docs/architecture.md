---
layout: default
title: Architecture
nav_order: 4
---

# Architecture Documentation

Complete technical architecture documentation for the AI Notification Platform.

## System Overview

The AI Notification Platform is a production-ready, event-driven microservices architecture designed to handle notification processing at scale. The system decouples notification creation from processing, enabling high throughput and fault tolerance.

## Architecture Diagram

```
┌─────────────────┐
│                 │
│     Client      │
│                 │
└────────┬────────┘
         │
         │ HTTP POST /notifications
         │
┌────────▼──────────────────────────────────────────────────────────────┐
│                                                                        │
│                      NOTIFICATION-SERVICE                              │
│                                                                        │
│  ┌──────────────┐   ┌─────────────┐   ┌────────────────────────┐    │
│  │              │   │             │   │                        │    │
│  │ REST API     ├──▶│  Service    ├──▶│  Kafka Producer        │    │
│  │ Controller   │   │  Layer      │   │                        │    │
│  │              │   │             │   └────────┬───────────────┘    │
│  └──────────────┘   └──────┬──────┘            │                     │
│                             │                   │                     │
│                             │                   │                     │
│                    ┌────────▼─────────┐         │                     │
│                    │                  │         │                     │
│                    │  PostgreSQL      │         │                     │
│                    │  Repository      │         │                     │
│                    │  (JPA)           │         │                     │
│                    └──────────────────┘         │                     │
│                                                  │                     │
└──────────────────────────────────────────────────┼─────────────────────┘
                                                   │
                                                   │ Kafka Topic:
                                                   │ notification.requested
                                                   │
                                          ┌────────▼─────────┐
                                          │                  │
                                          │   Apache Kafka   │
                                          │   + Zookeeper    │
                                          │                  │
                                          └────────┬─────────┘
                                                   │
                                                   │
┌──────────────────────────────────────────────────┼─────────────────────┐
│                                                  │                     │
│                      WORKER-SERVICE                                    │
│                                                  │                     │
│  ┌───────────────────┐                           │                     │
│  │                   │                           │                     │
│  │  Kafka Consumer   │◀──────────────────────────┘                     │
│  │                   │                                                 │
│  └─────────┬─────────┘                                                 │
│            │                                                           │
│            │                                                           │
│  ┌─────────▼──────────────┐          ┌──────────────────┐            │
│  │                        │          │                  │            │
│  │  Processing Service    ├─────────▶│   PostgreSQL     │            │
│  │                        │          │   Repository     │            │
│  │  - Simulate sending    │          │   (JPA)          │            │
│  │  - Retry mechanism     │          │                  │            │
│  │  - Error handling      │          └──────────────────┘            │
│  │                        │                                           │
│  └────────────────────────┘                                           │
│            │                                                           │
│            │                                                           │
│  ┌─────────▼──────────────┐                                           │
│  │                        │                                           │
│  │   Retry Scheduler      │                                           │
│  │   (Every 60 seconds)   │                                           │
│  │                        │                                           │
│  └────────────────────────┘                                           │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘


┌────────────────────────────────────────────────────────────────────────┐
│                                                                        │
│                          AI-SERVICE                                    │
│                                                                        │
│  ┌──────────────┐   ┌─────────────────────────────────────────┐      │
│  │              │   │                                         │      │
│  │ REST API     ├──▶│   AI Optimization Service               │      │
│  │ Controller   │   │                                         │      │
│  │              │   │   - Subject line enhancement            │      │
│  │              │   │   - Message optimization                │      │
│  │              │   │   - Channel-specific formatting         │      │
│  │              │   │   - Engagement prediction               │      │
│  │              │   │                                         │      │
│  └──────────────┘   └─────────────────────────────────────────┘      │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘


                         INFRASTRUCTURE LAYER

┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│              │  │              │  │              │  │              │
│  PostgreSQL  │  │    Redis     │  │    Kafka     │  │  Zookeeper   │
│              │  │              │  │              │  │              │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
```

## Microservices Overview

### 1. Notification Service (Port 8081)

REST API entry point for notification creation. Validates requests, persists to database, and publishes events to Kafka.

**Key Technologies:** Spring Boot, Spring Data JPA, Spring Kafka Producer

### 2. Worker Service

Background processing engine that consumes Kafka messages, processes notifications asynchronously, and implements retry logic.

**Key Technologies:** Spring Boot, Spring Kafka Consumer, Spring Scheduling

### 3. AI Service (Port 8083)

AI-powered content optimization service that enhances notification subject lines and messages.

**Key Technologies:** Spring Boot, Spring Web

## Data Flow

1. Client sends POST request to notification-service
2. Notification saved to PostgreSQL with PENDING status
3. Event published to Kafka topic
4. Worker-service consumes event
5. Notification processed and status updated to SENT/RETRY/FAILED

## Infrastructure Components

- **PostgreSQL 15**: Primary data store
- **Redis 7**: Caching layer
- **Apache Kafka**: Message broker
- **Zookeeper**: Kafka coordination
- **Kafka UI (Port 8080)**: Monitoring interface

## Notification Status Flow

```
PENDING → PROCESSING → SENT
            ↓
          RETRY → PROCESSING → SENT
            ↓
          RETRY → PROCESSING → SENT
            ↓
          FAILED
```

## Scalability

All services are stateless and horizontally scalable:
- Add load balancer for notification-service
- Deploy multiple worker-service instances
- Increase Kafka partitions for parallelism
- Use database read replicas

## Monitoring

- Health endpoints on all services
- Kafka UI for message monitoring
- Actuator endpoints for metrics
- Structured logging with SLF4J

## Security Considerations

Current implementation includes:
- Input validation
- SQL injection prevention
- Parameterized queries

Production enhancements needed:
- Spring Security with JWT
- TLS/SSL encryption
- Rate limiting
- Secrets management

## Performance

Expected throughput:
- notification-service: 1000+ req/sec
- worker-service: 500+ msg/sec
- ai-service: 2000+ req/sec

## Future Enhancements

- Service discovery (Eureka/Consul)
- API Gateway
- Circuit breaker (Resilience4j)
- Distributed tracing (Zipkin)
- Prometheus + Grafana monitoring
- Real AI integration
