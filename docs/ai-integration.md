---
layout: default
title: AI Service Integration
nav_order: 5
---

# AI Service Integration Guide

## Overview

The AI service is now integrated into the notification processing pipeline. Before sending notifications, the worker-service automatically enhances message content using the AI service, with automatic fallback to original content if the AI service is unavailable.

## Integration Architecture

```
notification-service
        │
        ├─ Persist to DB
        └─ Publish to Kafka
                │
                ▼
        Kafka Topic: notification.requested
                │
                ▼
        worker-service
                │
                ├─ Consume event
                ├─ Mark as PROCESSING
                └─ Call AI Service (NEW)
                        │
                    ┌───┴───┐
                    │       │
                Success  Failure
                    │       │
                    └───┬───┘
                        │
                        ▼
                Update with enhanced/original content
                        │
                        ▼
                Simulate sending
                        │
                    ┌───┴───┐
                    │       │
                Success  Failure
                    │       │
                 SENT     RETRY/FAILED
```

## Data Flow

### 1. Message Enhancement Request

When a notification is being processed, the worker-service calls the AI service:

**Request:**
```json
{
  "subject": "Welcome to our platform",
  "message": "Thank you for signing up!",
  "channel": "EMAIL"
}
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

### 2. Processing Flow

```
Step 1: Kafka consumes notification event
    ↓
Step 2: Load notification from database
    ↓
Step 3: Call AI service to enhance content
    ├─ Success: Use enhanced subject and message
    └─ Failure: Use original subject and message
    ↓
Step 4: Update notification with final content
    ↓
Step 5: Simulate sending notification
    ├─ Success: Mark as SENT
    └─ Failure: Mark as RETRY or FAILED
    ↓
Step 6: Save updated notification
```

## Resilience Patterns

### Circuit Breaker

The integration uses Resilience4j's circuit breaker to protect against AI service failures:

**Configuration:**
```yaml
slidingWindowSize: 10              # Track last 10 calls
failureRateThreshold: 50           # Open at 50% failure rate
waitDurationInOpenState: 10000ms   # Wait 10 seconds before half-open
permittedNumberOfCallsInHalfOpenState: 3  # Try 3 calls in half-open
```

**States:**
- **CLOSED**: Normal operation, requests pass through
- **OPEN**: AI service failing, requests immediately fallback
- **HALF_OPEN**: Testing if service recovered, allows limited requests

### Timeout

Each AI enhancement request has a 5-second timeout:

```yaml
timelimiter:
  instances:
    aiServiceCircuitBreaker:
      timeoutDuration: 5s
```

If the AI service doesn't respond within 5 seconds, the request fails and falls back to original content.

### Graceful Fallback

If AI enhancement fails for any reason (network error, timeout, circuit breaker open), the system automatically uses the original message content:

```java
AiEnhancementService.EnhancementResult result = aiEnhancementService.enhance(
    originalSubject,
    originalMessage,
    channel
);

if (result.successful) {
    notification.setSubject(result.enhancedSubject);
    notification.setMessage(result.enhancedMessage);
} else {
    // Continue with original content
    notification.setSubject(result.enhancedSubject);  // Falls back to original
    notification.setMessage(result.enhancedMessage);  // Falls back to original
}
```

## Implementation Details

### OpenFeign Client

The `AiServiceClient` interface defines the contract with the AI service:

```java
@FeignClient(
    name = "ai-service",
    url = "${ai-service.url:http://localhost:8083}"
)
public interface AiServiceClient {
    @PostMapping("/ai/optimize")
    @CircuitBreaker(
        name = "aiServiceCircuitBreaker",
        fallbackMethod = "enhanceMessageFallback"
    )
    AiEnhancementResponse enhanceMessage(@RequestBody AiEnhancementRequest request);

    default AiEnhancementResponse enhanceMessageFallback(
        AiEnhancementRequest request,
        Exception ex
    ) {
        // Returns original content on failure
        return new AiEnhancementResponse(
            request.getSubject(),
            request.getSubject(),
            request.getMessage(),
            request.getMessage(),
            "Fallback: AI service unavailable",
            0.0
        );
    }
}
```

### Enhancement Service

`AiEnhancementService` wraps the Feign client with additional logging and error handling:

```java
public EnhancementResult enhance(String subject, String message, String channel) {
    try {
        AiEnhancementRequest request = new AiEnhancementRequest(
            subject, message, channel
        );
        AiEnhancementResponse response = aiServiceClient.enhanceMessage(request);

        return new EnhancementResult(
            true,  // successful
            response.getOptimizedSubject(),
            response.getEnhancedMessage(),
            response.getConfidenceScore(),
            null
        );
    } catch (Exception ex) {
        return new EnhancementResult(
            false,  // successful
            subject,  // fallback to original
            message,  // fallback to original
            0.0,
            ex.getMessage()
        );
    }
}
```

## Configuration

### Environment Variables

Configure the AI service URL via environment variable:

```bash
export AI_SERVICE_URL=http://localhost:8083
```

Default: `http://localhost:8083`

### application.yml

```yaml
ai-service:
  url: ${AI_SERVICE_URL:http://localhost:8083}

resilience4j:
  circuitbreaker:
    instances:
      aiServiceCircuitBreaker:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3
        recordExceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
          - feign.FeignException
  timelimiter:
    instances:
      aiServiceCircuitBreaker:
        timeoutDuration: 5s
        cancelRunningFuture: true
```

## Logging

The integration provides detailed logging at DEBUG level:

```
INFO  - Requesting AI enhancement for channel: EMAIL
INFO  - Using AI-enhanced content. Confidence: 0.87
DEBUG - [AiServiceClient] -> POST /ai/optimize
DEBUG - [AiServiceClient] <- 200 OK
```

Enable full Feign logging:

```yaml
logging:
  level:
    org.springframework.cloud.openfeign: DEBUG
```

## Error Scenarios

### Scenario 1: AI Service Healthy

```
Request → AI Service → Response (200 OK)
Result: Enhanced content is used
Status: ✓ Success
```

### Scenario 2: AI Service Timeout

```
Request → [Waiting...] → 5s timeout
Result: Original content used (fallback)
Status: ✓ Notification still sent with original content
```

### Scenario 3: AI Service Returns Error

```
Request → AI Service → Response (500 Error)
Result: Original content used (fallback)
Status: ✓ Notification still sent with original content
```

### Scenario 4: Circuit Breaker Open (Multiple Failures)

```
Circuit State: OPEN (AI service failing frequently)
Request → Circuit Breaker blocks call
Result: Original content used (fallback), no request to AI service
Status: ✓ Faster fallback, reduces load on failing service
```

## Monitoring and Debugging

### Check Circuit Breaker Status

The Resilience4j actuator endpoint exposes circuit breaker metrics:

```bash
curl http://localhost:8082/actuator/circuitbreakers
```

### View Recent Calls

```bash
curl http://localhost:8082/actuator/circuitbreakers/aiServiceCircuitBreaker
```

### Enable Debug Logging

Add to application.yml:

```yaml
logging:
  level:
    org.springframework.cloud.openfeign: DEBUG
    io.github.resilience4j: DEBUG
```

## Testing the Integration

### Test 1: Successful Enhancement

1. Start all services
2. Create a notification:
```bash
curl -X POST http://localhost:8081/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "test@example.com",
    "subject": "Welcome",
    "message": "Thank you for signing up",
    "channel": "EMAIL"
  }'
```

3. Check logs for enhancement:
```
INFO  - Requesting AI enhancement for channel: EMAIL
INFO  - Using AI-enhanced content. Confidence: 0.87
```

### Test 2: AI Service Unavailable

1. Stop the AI service
2. Create a notification
3. Check logs for fallback:
```
INFO  - Requesting AI enhancement for channel: EMAIL
WARN  - AI enhancement failed, using fallback. Error: Connection refused
```

4. Verify notification is still processed with original content

### Test 3: Timeout Scenario

Add artificial delay to AI service response, then create a notification. The worker-service will timeout after 5 seconds and use fallback content.

## Performance Impact

### Enhancement Overhead

- **Average enhancement time**: 100-200ms
- **Timeout**: 5 seconds max
- **Fallback time**: < 1ms

### Asynchronous Architecture Preserved

The integration doesn't break the asynchronous architecture:

1. notification-service remains stateless and fast (< 100ms)
2. Kafka handles decoupling
3. worker-service processes notifications asynchronously
4. AI enhancement happens during worker processing (background)
5. If AI service is slow/unavailable, worker-service continues normally with fallback

### Database Impact

Enhanced content is stored in the same notification record:

```sql
UPDATE notifications
SET subject = 'Enhanced subject',
    message = 'Enhanced message',
    status = 'SENT'
WHERE id = ?
```

No additional tables or complexity.

## Production Considerations

1. **Scale AI Service**: Deploy multiple AI service instances behind a load balancer
2. **Monitor Circuit Breaker**: Set up alerts for repeated circuit breaker openings
3. **Adjust Timeouts**: Increase from 5s if AI service is consistently slow
4. **Cache Enhancements**: Consider caching enhanced content for repeated messages
5. **Metrics**: Export Resilience4j metrics to Prometheus/Grafana

## Future Enhancements

1. **Async Enhancement**: Enhance in background after sending
2. **Enhancement Caching**: Cache popular message enhancements
3. **A/B Testing**: Compare original vs enhanced engagement metrics
4. **ML Integration**: Learn which enhancements work best per channel
5. **Rate Limiting**: Limit enhancement requests during peak load
