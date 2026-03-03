---
layout: default
title: Integration Flow Diagrams
nav_order: 6
---

# Integration Flow Diagrams

## Complete End-to-End Flow with AI Enhancement

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ 1. POST /notifications
       │    (recipient, subject, message, channel)
       │
       ▼
┌──────────────────────────────────────────────────┐
│        NOTIFICATION-SERVICE (Port 8081)          │
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │ 1. Validate request                        │ │
│  │ 2. Create Notification(PENDING)            │ │
│  │ 3. Save to PostgreSQL                      │ │
│  │ 4. Publish NotificationEvent to Kafka      │ │
│  │ 5. Return 200 OK with notification ID      │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
└──────────────┬───────────────────────────────────┘
               │
               │ 2. NotificationEvent
               │    (id, recipient, subject, message, channel)
               │
               ▼
     ┌─────────────────────┐
     │   Apache Kafka      │
     │ Topic: notification.│
     │ requested           │
     └─────────────────────┘
               │
               │ 3. Consume event
               │
               ▼
┌──────────────────────────────────────────────────┐
│         WORKER-SERVICE                          │
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │ 1. Receive NotificationEvent from Kafka    │ │
│  │ 2. Find Notification by ID in database     │ │
│  │ 3. Mark as PROCESSING                      │ │
│  │ 4. Call AI Service for enhancement         │ │
│  │                                            │ │
│  │    ┌─────────────────────────────────┐    │ │
│  │    │ AI Enhancement Request:         │    │ │
│  │    │ {                               │    │ │
│  │    │   subject: "Welcome",           │    │ │
│  │    │   message: "Thank you...",      │    │ │
│  │    │   channel: "EMAIL"              │    │ │
│  │    │ }                               │    │ │
│  │    └──────────┬──────────────────────┘    │ │
│  │               │                            │ │
│  │         Call with Timeout (5s)            │ │
│  │         & Circuit Breaker                 │ │
│  │               │                            │ │
│  │               ▼                            │ │
│  │    ┌─────────────────────────────────┐    │ │
│  │    │ AI Service Response:            │    │ │
│  │    │ {                               │    │ │
│  │    │   optimizedSubject: "🚀 Exc...", │   │ │
│  │    │   enhancedMessage: "Thank...",  │    │ │
│  │    │   confidenceScore: 0.87         │    │ │
│  │    │ }                               │    │ │
│  │    └─────────────────────────────────┘    │ │
│  │               │                            │ │
│  │    ┌──────────┴──────────┐                │ │
│  │    │                     │                │ │
│  │    ▼ Success        Failure ▼             │ │
│  │  Use enhanced     Use original content    │ │
│  │  content          (graceful fallback)     │ │
│  │    │                     │                │ │
│  │    └──────────┬──────────┘                │ │
│  │               │                            │ │
│  │               ▼                            │ │
│  │ 5. Update Notification with final content│ │
│  │ 6. Simulate sending (2s delay)            │ │
│  │ 7a. Success: Mark as SENT                │ │
│  │ 7b. Failure: Mark as RETRY/FAILED        │ │
│  │ 8. Save updated Notification             │ │
│  │                                            │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
└──────────────────────────────────────────────────┘
               │
               ▼
         Database Updated
         (PostgreSQL)
```

## Detailed AI Service Call Flow

```
WORKER-SERVICE                           AI-SERVICE
      │                                      │
      │ POST /ai/optimize                    │
      ├──────────────────────────────────────>
      │                                      │
      │                        Processing...  │
      │                        (Enhancing)    │
      │                                      │
      │ 200 OK                               │
      │ {optimized content}                  │
      │<──────────────────────────────────────┤
      │                                      │
      ▼                                      │

OR FAILURE SCENARIOS:

WORKER-SERVICE                           AI-SERVICE
      │                                      │
      │ POST /ai/optimize                    │
      ├──────────────────────────────────────>
      │                                      │
      │                    Timeout (5s)
      │                    ✗ No response
      │                                      │
      │ Timeout Exception                    │
      │ Circuit Breaker OPEN                 │
      │                                      │
      ▼                                      ▼
   Fallback:
   Use original message content
```

## Circuit Breaker State Transitions

```
                    Closed
                      │
                      │ Success rate < 50%
                      │ (in last 10 calls)
                      ▼
                    Open
                      │
                      │ Wait 10 seconds
                      │
                      ▼
                Half-Open
                      │
        ┌─────────────┼─────────────┐
        │                           │
   Success (3 calls)           Failure
        │                           │
        ▼                           ▼
      Closed                      Open
                                    │
                        (restart wait cycle)
```

## Database Update Sequence

```
Kafka Event:
┌─────────────────────────────┐
│ NotificationEvent           │
│ - id: 123                   │
│ - recipient: user@test.com  │
│ - subject: "Welcome"        │
│ - message: "Thanks"         │
│ - channel: "EMAIL"          │
└─────────────────────────────┘

Database Before Processing:
┌─────────────────────────────────────────┐
│ Notification(id=123)                    │
│ - subject: "Welcome"                    │
│ - message: "Thanks"                     │
│ - status: PENDING                       │
│ - updated_at: 2024-01-15 10:00:00      │
└─────────────────────────────────────────┘

Step 1 - Mark as Processing:
┌─────────────────────────────────────────┐
│ Notification(id=123)                    │
│ - subject: "Welcome"                    │
│ - message: "Thanks"                     │
│ - status: PROCESSING                    │
│ - updated_at: 2024-01-15 10:00:01      │
└─────────────────────────────────────────┘

Step 2 - AI Enhancement:
┌─────────────────────────────────────────┐
│ Notification(id=123)                    │
│ - subject: "🚀 Exclusive: Welcome"      │ ◄─ Enhanced
│ - message: "Thanks! ✨ Optimized"       │ ◄─ Enhanced
│ - status: PROCESSING                    │
│ - updated_at: 2024-01-15 10:00:01      │
└─────────────────────────────────────────┘

Step 3 - Mark as Sent:
┌─────────────────────────────────────────┐
│ Notification(id=123)                    │
│ - subject: "🚀 Exclusive: Welcome"      │
│ - message: "Thanks! ✨ Optimized"       │
│ - status: SENT                          │
│ - error_message: null                   │
│ - updated_at: 2024-01-15 10:00:03      │
└─────────────────────────────────────────┘
```

## Resilience Pattern: Timeout

```
Timeline:
T=0ms     Worker-service sends request to AI-service
  │
  ├─ T=100ms   AI-service processing...
  │
  ├─ T=500ms   AI-service processing...
  │
  ├─ T=1000ms  AI-service processing...
  │
  ├─ T=5000ms  TIMEOUT THRESHOLD
  │           ╔════════════════════════╗
  │           ║ TimeLimiter triggers   ║
  │           ║ Request is cancelled   ║
  │           ║ Exception thrown       ║
  │           ╚════════════════════════╝
  │
  └─ T=5001ms  Fallback executed
              Original content used
              Continue processing
```

## Resilience Pattern: Circuit Breaker

```
Call History (last 10 calls):
├─ Call 1: Success ✓
├─ Call 2: Success ✓
├─ Call 3: Timeout ✗
├─ Call 4: Timeout ✗
├─ Call 5: Timeout ✗
├─ Call 6: Timeout ✗
├─ Call 7: Timeout ✗
├─ Call 8: Success ✓
├─ Call 9: Timeout ✗
└─ Call 10: Timeout ✗

Failure Rate = 8/10 = 80%
Threshold = 50%
Result: CIRCUIT BREAKER OPENS!

┌─────────────────────────────────────┐
│ Circuit State: OPEN                 │
│                                     │
│ Behavior:                           │
│ - Request comes in                  │
│ - Circuit breaker intercepts        │
│ - Immediately returns fallback      │
│ - No request sent to AI-service     │
│ - Saves resources on failing service│
│                                     │
│ After 10 seconds:                   │
│ - Transition to HALF_OPEN           │
│ - Allow 3 test calls                │
│ - If successful: return to CLOSED   │
│ - If fail: return to OPEN           │
└─────────────────────────────────────┘
```

## Concurrent Processing (Asynchronous)

```
Time:        T=0s              T=2s              T=4s

Notification 1:
  Kafka ──> Processing ──> AI Enhance ──> Send
                └─── 2s ────┘
                            └─── 1s ────┘
                                        └─ SENT

Notification 2:
  Kafka ──> Processing ──> AI Enhance ──> Send
                └─── 2s ────┘
                            └─── 1s ────┘
                                        └─ SENT

Notification 3:
  Kafka ──> Processing ──> AI Enhance ──> Send
                └─── 2s ────┘
                            └─── 1s ────┘
                                        └─ SENT

All three notifications processed concurrently:
- Kafka consumer threads: 3 (configurable)
- Each notification independently enhanced
- No blocking of other notifications
- Asynchronous architecture preserved
```

## Failure Recovery Flow

```
Scenario: AI Service becomes unavailable

T=0s:    Normal operation (Circuit: CLOSED)
         Request → AI-service → Response
         Success rate: 100%

T=30s:   AI-service crashes
         Request → AI-service → Timeout

T=35s:   Multiple timeouts
         Circuit breaker calculates failure rate
         Failure rate = 90% > 50% threshold
         Circuit breaker OPENS

T=36s:   New notification arrives
         Request → Circuit breaker (OPEN)
         Fallback: Use original content
         No unnecessary call to AI-service

T=40s:   AI-service recovers
         (Service restarted, issue fixed)

T=46s:   Circuit breaker enters HALF_OPEN state
         Allows 3 test calls to AI-service

T=47s:   Test calls succeed
         Failure rate drops
         Circuit breaker CLOSES

T=48s:   Normal operation restored
         Request → AI-service → Response
         (repeat from T=0s)

Result:  System remained operational throughout,
         notifications sent with original content
         during outage, automatic recovery when
         AI-service comes back online.
```
