---
layout: default
title: API Reference
nav_order: 3
---

# API Reference

Complete API documentation for all microservices.

## Notification Service

Base URL: `http://localhost:8081`

### Create Notification

Creates a new notification and queues it for processing.

**Endpoint:** `POST /notifications`

**Request Body:**
```json
{
  "recipient": "user@example.com",
  "subject": "Welcome to our platform",
  "message": "Thank you for signing up!",
  "channel": "EMAIL"
}
```

**Parameters:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| recipient | string | Yes | Email address, phone number, or device ID |
| subject | string | Yes | Notification subject line |
| message | string | Yes | Notification message content |
| channel | string | Yes | Must be EMAIL, SMS, or PUSH |

**Response:** `201 Created`
```json
{
  "id": 1,
  "status": "PENDING",
  "message": "Notification created successfully"
}
```

**Error Response:** `400 Bad Request`
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Recipient is required"
}
```

**Example:**
```bash
curl -X POST http://localhost:8081/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "user@example.com",
    "subject": "Welcome",
    "message": "Thank you for signing up!",
    "channel": "EMAIL"
  }'
```

### Health Check

Check if the notification service is running.

**Endpoint:** `GET /notifications/health`

**Response:** `200 OK`
```
Notification Service is running
```

**Example:**
```bash
curl http://localhost:8081/notifications/health
```

## AI Service

Base URL: `http://localhost:8083`

### Optimize Content

Optimizes notification content using AI-powered enhancement.

**Endpoint:** `POST /ai/optimize`

**Request Body:**
```json
{
  "subject": "Welcome to our platform",
  "message": "Thank you for signing up!",
  "channel": "EMAIL"
}
```

**Parameters:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| subject | string | Yes | Original subject line |
| message | string | Yes | Original message content |
| channel | string | No | Notification channel (EMAIL, SMS, PUSH) |

**Response:** `200 OK`
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

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| originalSubject | string | Original subject line |
| optimizedSubject | string | AI-enhanced subject line |
| originalMessage | string | Original message content |
| enhancedMessage | string | AI-enhanced message content |
| optimizationStrategy | string | Strategy used for optimization |
| confidenceScore | number | Confidence score (0.0 to 1.0) |

**Example:**
```bash
curl -X POST http://localhost:8083/ai/optimize \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Welcome to our platform",
    "message": "Thank you for signing up!",
    "channel": "EMAIL"
  }'
```

### Health Check

Check if the AI service is running.

**Endpoint:** `GET /ai/health`

**Response:** `200 OK`
```
AI Service is running
```

**Example:**
```bash
curl http://localhost:8083/ai/health
```

## Notification Channels

### EMAIL

Used for email notifications.

**Features:**
- Full message content supported
- Subject line optimization
- Engagement footer added by AI

**Example:**
```json
{
  "recipient": "user@example.com",
  "subject": "Account Verification",
  "message": "Please verify your email address",
  "channel": "EMAIL"
}
```

### SMS

Used for SMS/text message notifications.

**Features:**
- Message automatically truncated to 100 characters
- Subject line reformatted for SMS
- No emojis in power words

**Example:**
```json
{
  "recipient": "+1234567890",
  "subject": "Verification Code",
  "message": "Your code is: 123456",
  "channel": "SMS"
}
```

### PUSH

Used for mobile push notifications.

**Features:**
- Message automatically truncated to 150 characters
- Emoji-enhanced subject line
- Optimized for mobile display

**Example:**
```json
{
  "recipient": "device-token-12345",
  "subject": "New Message",
  "message": "You have a new message from John",
  "channel": "PUSH"
}
```

## Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created successfully |
| 400 | Bad request (validation error) |
| 500 | Internal server error |

## Notification Statuses

| Status | Description |
|--------|-------------|
| PENDING | Notification created, waiting to be processed |
| PROCESSING | Currently being sent |
| SENT | Successfully delivered |
| RETRY | Failed, will be retried |
| FAILED | Failed after maximum retry attempts |

## Rate Limiting

Currently, there are no rate limits in place. For production deployment, consider implementing:

- Rate limiting per IP address
- Rate limiting per recipient
- Throttling based on channel type

## Authentication

Currently, the API does not require authentication. For production deployment, implement:

- JWT token authentication
- API key authentication
- OAuth 2.0

## Error Handling

All errors follow this format:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/notifications"
}
```

Common error messages:
- "Recipient is required"
- "Subject is required"
- "Message is required"
- "Channel must be EMAIL, SMS, or PUSH"
- "Recipient must be a valid email"
