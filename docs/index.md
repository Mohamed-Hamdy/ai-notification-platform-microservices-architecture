---
layout: default
title: Home
nav_order: 1
---

# AI Notification Platform

A production-ready microservices-based notification platform built with Spring Boot 3.x and Java 17.

## Overview

The AI Notification Platform provides REST APIs for creating notifications, processes them asynchronously via Apache Kafka, and includes AI-powered content optimization capabilities.

## Key Features

- **Asynchronous Processing**: Decoupled notification creation from delivery using Kafka
- **Automatic Retry Mechanism**: Failed notifications are automatically retried up to 3 times
- **AI Optimization**: Enhance notification content with AI-powered optimization
- **Multiple Channels**: Support for EMAIL, SMS, and PUSH notifications
- **Production Ready**: Built with Spring Boot, PostgreSQL, Redis, and Kafka
- **Scalable Architecture**: Microservices design enables horizontal scaling

## Architecture

The platform consists of 3 independent microservices:

1. **notification-service** - REST API entry point for notification creation (Port 8081)
2. **worker-service** - Background processing engine for sending notifications
3. **ai-service** - AI optimization layer for content enhancement (Port 8083)

```
Client → notification-service → Kafka → worker-service
                ↓
            PostgreSQL
```

## Quick Start

```bash
# Start infrastructure
docker-compose up -d

# Start services (3 separate terminals)
cd notification-service && mvn spring-boot:run
cd worker-service && mvn spring-boot:run
cd ai-service && mvn spring-boot:run

# Create a notification
curl -X POST http://localhost:8081/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "user@example.com",
    "subject": "Welcome",
    "message": "Thank you for signing up!",
    "channel": "EMAIL"
  }'
```

## Documentation

- [Getting Started](getting-started.html) - Installation and setup guide
- [API Reference](api-reference.html) - Complete API documentation
- [Architecture](architecture.html) - System architecture and design
- [Development](development.html) - Development and deployment guide

## Tech Stack

- Java 17
- Spring Boot 3.2.0
- Apache Kafka
- PostgreSQL 15
- Redis 7
- Docker & Docker Compose

## License

MIT License
