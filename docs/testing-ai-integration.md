---
layout: default
title: Testing AI Integration
nav_order: 7
---

# Testing AI Integration Guide

## Prerequisites

1. All three services running:
    - notification-service (port 8081)
    - worker-service (no port)
    - ai-service (port 8083)

2. Kafka and PostgreSQL running via docker-compose

3. Postman collection imported (with new AI integration requests)

## Test Scenarios

### Test 1: Successful Enhancement (Happy Path)

**Objective**: Verify AI service enhancement works correctly

**Steps**:

1. Ensure all services are running and healthy
2. Create a notification:

```bash
curl -X POST http://localhost:8081/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "test@example.com",
    "subject": "Welcome to our platform",
    "message": "Thank you for signing up!",
    "channel": "EMAIL"
  }'
```

Expected response:
```json
{
  "id": 1,
  "recipient": "test@example.com",
  "subject": "Welcome to our platform",
  "message": "Thank you for signing up!",
  "channel": "EMAIL",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:00:00Z"
}
```

3. Wait 5-10 seconds for Kafka consumer to process

4. Check worker-service logs:

```
INFO  - Requesting AI enhancement for channel: EMAIL
INFO  - Using AI-enhanced content. Confidence: 0.87
INFO  - Sending EMAIL notification to: test@example.com
INFO  - Subject: 🚀 Exclusive: Welcome to our platform
INFO  - Successfully processed notification ID: 1
```

5. Verify database update:

```sql
SELECT subject, message, status FROM notifications WHERE id = 1;

Output:
subject: 🚀 Exclusive: Welcome to our platform
message: Thank you for signing up! ✨ This message has been AI-optimized...
status: SENT
```

**Expected Result**: ✓ Notification enhanced and marked as SENT

---

### Test 2: AI Service Timeout Fallback

**Objective**: Verify graceful fallback when AI service times out

**Setup**:

1. Add delay to AI service response (simulate slow processing):

Edit `ai-service/src/main/java/com/ai/service/AiOptimizationService.java`:

```java
public OptimizationResponse optimize(OptimizationRequest request) {
    Thread.sleep(6000);  // Sleep longer than 5s timeout
    // ... rest of logic
}
```

Recompile and restart AI service.

2. Create a notification:

```bash
curl -X POST http://localhost:8081/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "test@example.com",
    "subject": "Order Confirmation",
    "message": "Your order has been confirmed",
    "channel": "EMAIL"
  }'
```

3. Check worker-service logs within 10 seconds:

```
INFO  - Requesting AI enhancement for channel: EMAIL
WARN  - AI enhancement failed, using fallback. Error: ...timeout...
INFO  - Sending EMAIL notification to: test@example.com
INFO  - Subject: Order Confirmation
INFO  - Successfully processed notification ID: 2
```

4. Verify database:

```sql
SELECT subject, message, status FROM notifications WHERE id = 2;

Output:
subject: Order Confirmation  (original, not enhanced)
message: Your order has been confirmed  (original)
status: SENT
```

**Expected Result**: ✓ Notification sent with original content despite timeout

---

### Test 3: AI Service Unavailable (Circuit Breaker)

**Objective**: Verify circuit breaker prevents cascading failures

**Setup**:

1. Stop the AI service:

```bash
# In the ai-service terminal, press Ctrl+C
```

2. Create 5-10 notifications rapidly:

```bash
for i in {1..10}; do
  curl -X POST http://localhost:8081/notifications \
    -H "Content-Type: application/json" \
    -d "{
      \"recipient\": \"test$i@example.com\",
      \"subject\": \"Test $i\",
      \"message\": \"Message $i\",
      \"channel\": \"EMAIL\"
    }"
  sleep 0.5
done
```

3. Watch worker-service logs:

```
First 3 attempts:
INFO  - Requesting AI enhancement for channel: EMAIL
WARN  - Connection refused (AI service down)
WARN  - AI enhancement failed, using fallback

After ~5 more failures:
INFO  - Requesting AI enhancement for channel: EMAIL
WARN  - Circuit breaker is OPEN. Falling back immediately.
```

4. Verify notifications are still sent:

```sql
SELECT COUNT(*) FROM notifications WHERE status = 'SENT';
-- Should show 10 (all processed despite AI service down)
```

5. Restart AI service:

```bash
cd ai-service
mvn spring-boot:run
```

6. Create more notifications:

```bash
curl -X POST http://localhost:8081/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "recovery@example.com",
    "subject": "Service Recovery Test",
    "message": "Testing recovery",
    "channel": "EMAIL"
  }'
```

7. Check logs - circuit breaker should eventually recover:

```
Circuit breaker transitioning to HALF_OPEN state
Attempting recovery calls...
[after 3 successful calls]
Circuit breaker transitioning to CLOSED state
INFO  - Requesting AI enhancement for channel: EMAIL
INFO  - Using AI-enhanced content. Confidence: 0.87
```

**Expected Result**: ✓ System remains operational, notifications sent, auto-recovery works

---

### Test 4: Invalid Request to AI Service

**Objective**: Verify fallback on malformed AI requests

**Setup**:

1. Modify AI client to send invalid data:

Edit worker-service to intentionally send bad channel:

```java
AiEnhancementRequest request = new AiEnhancementRequest(
    subject,
    message,
    "INVALID_CHANNEL"  // This channel doesn't exist
);
```

2. Create a notification and observe fallback

**Expected Result**: ✓ Fallback to original content, notification still sent

---

### Test 5: Load Test with AI Enhancement

**Objective**: Verify performance under load

**Setup**:

1. Use Postman Collection Runner:
    - Select "Load Test - Create Multiple Notifications"
    - Set iterations: 20
    - Set delay: 100ms

2. Run the collection

3. Monitor logs for timing:

```
Notification 1: Enhancement took 120ms, Send took 2000ms, Total: 2120ms
Notification 2: Enhancement took 95ms, Send took 2000ms, Total: 2095ms
Notification 3: Enhancement took 110ms, Send took 2000ms, Total: 2110ms
...
```

4. Verify all completed:

```sql
SELECT COUNT(*) FROM notifications WHERE status = 'SENT';
-- Should show 20+ notifications
```

**Expected Result**: ✓ All notifications processed, AI enhancement adds minimal overhead

---

### Test 6: Channel-Specific Enhancement

**Objective**: Verify AI service returns different enhancements per channel

**Test EMAIL**:
```bash
curl -X POST http://localhost:8083/ai/optimize \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Update your password",
    "message": "We recommend updating your password for security",
    "channel": "EMAIL"
  }'
```

Expected: Full message with emoji, power words

**Test SMS**:
```bash
curl -X POST http://localhost:8083/ai/optimize \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Update your password",
    "message": "We recommend updating your password for security",
    "channel": "SMS"
  }'
```

Expected: Truncated to ~100 characters

**Test PUSH**:
```bash
curl -X POST http://localhost:8083/ai/optimize \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Update your password",
    "message": "We recommend updating your password for security",
    "channel": "PUSH"
  }'
```

Expected: Truncated to ~150 characters

**Expected Result**: ✓ Different optimization strategies per channel

---

## Debugging

### Check Circuit Breaker Status

Add Spring Boot Actuator to worker-service (optional):

```bash
curl http://localhost:8082/actuator/circuitbreakers/aiServiceCircuitBreaker
```

### Enable Full Debug Logging

Add to worker-service `application.yml`:

```yaml
logging:
  level:
    com.worker: DEBUG
    org.springframework.cloud.openfeign: DEBUG
    io.github.resilience4j: DEBUG
```

### Monitor Kafka Messages

```bash
docker exec kafka kafka-console-consumer.sh \
  --bootstrap-servers localhost:9092 \
  --topic notification.requested \
  --from-beginning
```

### Check Database State

```bash
docker exec postgres psql -U postgres -d notification_db \
  -c "SELECT id, subject, status, created_at FROM notifications ORDER BY id DESC LIMIT 10;"
```

### View Service Logs

**Notification Service**:
```bash
docker logs notification-service
```

**Worker Service**:
```bash
docker logs worker-service
```

**AI Service**:
```bash
docker logs ai-service
```

---

## Performance Benchmarks

### Expected Metrics

| Metric | Expected | Notes |
|--------|----------|-------|
| AI Enhancement Time | 100-200ms | Per notification |
| AI Timeout | 5000ms | Circuit breaker triggers |
| Fallback Time | < 1ms | Immediate return |
| Total Processing Time | 2.1-2.3s | Includes 2s simulated send |
| Throughput | 15-20 msg/sec | With 3 worker threads |
| Success Rate (Normal) | 99%+ | All enhanced and sent |
| Success Rate (AI Down) | 100% | Fallback to original |
| Circuit Breaker Open→Close | 10 sec | Recovery time |

---

## Troubleshooting

### Issue: AI enhancement not happening

**Check**:
1. AI service is running: `curl http://localhost:8083/ai/health`
2. Worker service logs show enhancement attempts
3. Check Feign client configuration
4. Verify network connectivity between services

### Issue: Notifications stuck in PROCESSING

**Check**:
1. Worker service is running
2. Kafka consumer is consuming messages
3. Database connection is working
4. Check for exceptions in logs

### Issue: Circuit breaker always open

**Check**:
1. Verify AI service URL is correct in configuration
2. Check if AI service is actually responding
3. Lower `failureRateThreshold` to 30% for testing
4. Reset by restarting worker service

### Issue: Timeout happening immediately

**Check**:
1. Increase `timeoutDuration` in configuration
2. Check AI service performance
3. Monitor network latency
4. Verify AI service endpoints exist

---

## Test Automation

### Shell Script: End-to-End Test

```bash
#!/bin/bash

echo "Testing AI Integration..."

# Test 1: Send notification
RESPONSE=$(curl -s -X POST http://localhost:8081/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "test@example.com",
    "subject": "Test",
    "message": "Test message",
    "channel": "EMAIL"
  }')

ID=$(echo $RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo "Created notification ID: $ID"

# Wait for processing
sleep 5

# Test 2: Check database
RESULT=$(docker exec postgres psql -U postgres -d notification_db \
  -c "SELECT status FROM notifications WHERE id = $ID;")

if [[ $RESULT == *"SENT"* ]]; then
  echo "✓ Test PASSED: Notification enhanced and sent"
  exit 0
else
  echo "✗ Test FAILED: Notification not sent"
  exit 1
fi
```

---

## Summary

The AI integration should:

1. ✓ Enhance messages successfully when AI service is available
2. ✓ Fallback gracefully when AI service is unavailable
3. ✓ Return to normal operation when AI service recovers
4. ✓ Prevent cascading failures with circuit breaker
5. ✓ Complete notifications regardless of AI service state
6. ✓ Preserve asynchronous architecture
7. ✓ Add minimal performance overhead (< 200ms)
8. ✓ Provide clear logging for debugging
