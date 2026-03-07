---
layout: default
title: Ollama Integration
nav_order: 8
---

# Ollama-Powered Notification System

## Overview

Ollama LLM is integrated as the intelligent message generator for the notification system. It generates context-aware messages for multiple notification channels, ensuring appropriate tone, length, and format for each delivery method.

## Architecture

```
Notification Request
    │
    ├─ User Data
    ├─ Event Context
    ├─ Channel Type
    └─ Message Type
         │
         ▼
  Message Generation Service
    │
    ├─ Build Prompt (channel-specific)
    ├─ Send to Ollama LLM
    └─ Parse Response
         │
         ▼
  Channel Adapters
    │
    ├─ Email Adapter
    ├─ SMS Adapter
    ├─ Push Adapter
    ├─ In-App Adapter
    ├─ WhatsApp Adapter
    └─ Telegram Adapter
         │
         ▼
  Notification Delivery
    │
    ├─ Queue to Kafka
    └─ Deliver to Users
```

## Supported Channels

### 1. Email
- **Format**: Subject + detailed body with HTML
- **Use Case**: Transactional, Marketing, Alerts
- **Ollama Task**: Generate professional email with greeting, body, and call-to-action

### 2. SMS
- **Format**: Short text (max 160 characters)
- **Use Case**: Alerts, OTP, Confirmations
- **Ollama Task**: Generate concise message with essential info only

### 3. Push Notification
- **Format**: Title (max 65 chars) + Body (max 240 chars)
- **Use Case**: Real-time alerts, App updates
- **Ollama Task**: Generate attention-grabbing title and brief description

### 4. In-App Notification
- **Format**: Message with optional metadata (icon, color, priority)
- **Use Case**: System updates, User messages
- **Ollama Task**: Generate contextual message with metadata

### 5. WhatsApp
- **Format**: Conversational message with optional media
- **Use Case**: Customer support, Marketing
- **Ollama Task**: Generate friendly, conversational message

### 6. Telegram
- **Format**: Markdown-formatted message
- **Use Case**: Alerts, Updates, Bot commands
- **Ollama Task**: Generate formatted message with emojis and markdown

## Message Types

### 1. Alerts
- **Purpose**: Urgent system notifications
- **Tone**: Urgent, action-required
- **Example**: "⚠️ Your account activity alert: Login from unknown device"

### 2. Transactional
- **Purpose**: Order confirmations, receipts, status updates
- **Tone**: Professional, informative
- **Example**: "Order #12345 confirmed. Delivery expected by tomorrow."

### 3. System Updates
- **Purpose**: Maintenance, new features, policy changes
- **Tone**: Informative, professional
- **Example**: "Platform maintenance scheduled for tonight 2-4 AM UTC"

### 4. Marketing
- **Purpose**: Promotions, new products, campaigns
- **Tone**: Engaging, persuasive
- **Example**: "🎉 Exclusive offer: 50% off for 24 hours only!"

## Ollama Integration Points

### Setup

```bash
# Install Ollama
curl https://ollama.ai/install.sh | sh

# Pull model (llama2 recommended for balance of quality/speed)
ollama pull llama2

# Or use mistral for faster responses
ollama pull mistral

# Start Ollama service
ollama serve
```

Ollama API available at: `http://localhost:11434`

### Prompt Templates

Each channel has a specific prompt template that instructs Ollama how to format the message.

#### Email Template
```
You are a professional email writer. Generate an email for:
User: {user_name}
Event: {event_context}
Type: {message_type}

Requirements:
- Professional tone
- Clear subject line
- Structured body with greeting, content, and CTA
- Keep under 500 words

Generate valid JSON with: subject, body_text, body_html
```

#### SMS Template
```
You are a concise SMS writer. Generate a text message for:
User: {user_name}
Event: {event_context}
Type: {message_type}

Requirements:
- Maximum 160 characters
- Essential information only
- Include any code/reference if applicable
- NO HTML or special formatting

Generate valid JSON with: message
```

#### Push Notification Template
```
You are a push notification expert. Generate a notification for:
User: {user_name}
Event: {event_context}
Type: {message_type}

Requirements:
- Title: max 65 characters, attention-grabbing
- Body: max 240 characters, clear action
- Use emojis strategically

Generate valid JSON with: title, body, priority (high/normal/low)
```

#### In-App Template
```
You are an in-app messaging specialist. Generate an in-app notification for:
User: {user_name}
Event: {event_context}
Type: {message_type}

Requirements:
- Friendly, conversational tone
- Concise but informative
- Suggest appropriate color: info/success/warning/error
- Suggest priority: high/normal/low

Generate valid JSON with: message, color, icon, priority
```

#### WhatsApp Template
```
You are a WhatsApp conversation specialist. Generate a WhatsApp message for:
User: {user_name}
Event: {event_context}
Type: {message_type}

Requirements:
- Conversational, friendly tone
- Use emojis appropriately
- Max 4000 characters
- Can include formatting (bold, italic, code blocks)
- Suggest action buttons if applicable

Generate valid JSON with: message, buttons (optional)
```

#### Telegram Template
```
You are a Telegram bot expert. Generate a Telegram message for:
User: {user_name}
Event: {event_context}
Type: {message_type}

Requirements:
- Use Markdown formatting
- Include relevant emojis
- Can include inline buttons
- Max 4096 characters
- Professional yet friendly

Generate valid JSON with: message, parse_mode (Markdown), buttons (optional)
```

## Data Flow

```
1. Notification Request Arrives
   │
   ├─ User ID: 123
   ├─ Event Type: order_confirmed
   ├─ Channel: EMAIL
   ├─ Message Type: TRANSACTIONAL
   └─ Context: {order_id: "ORD-789", total: "$99.99"}
   │
   ▼
2. Build Prompt
   │
   ├─ Retrieve user data (name, preferences)
   ├─ Format event context as JSON
   ├─ Select channel-specific template
   └─ Create complete prompt
   │
   ▼
3. Call Ollama API
   │
   ├─ POST /api/generate
   ├─ Send prompt with model parameters
   └─ Stream/receive response
   │
   ▼
4. Parse Response
   │
   ├─ Extract JSON from Ollama output
   ├─ Validate required fields
   ├─ Fallback to template if parsing fails
   └─ Sanitize content
   │
   ▼
5. Adapter Processing
   │
   ├─ Format for specific channel
   ├─ Apply size limits
   ├─ Validate special characters
   └─ Prepare for delivery
   │
   ▼
6. Queue & Deliver
   │
   ├─ Publish to Kafka
   ├─ Store in database
   └─ Deliver to user via channel API
```

## Channel Adapters

### Email Adapter
```java
@Service
public class EmailChannelAdapter implements NotificationChannelAdapter {
    public NotificationPayload adapt(GeneratedMessage message) {
        // Format HTML body
        // Include company branding
        // Add unsubscribe link
        // Return email-specific payload
    }
}
```

### SMS Adapter
```java
@Service
public class SmsChannelAdapter implements NotificationChannelAdapter {
    public NotificationPayload adapt(GeneratedMessage message) {
        // Enforce 160 character limit
        // Remove special characters
        // Add shortcode if needed
        // Return SMS-specific payload
    }
}
```

### Push Notification Adapter
```java
@Service
public class PushChannelAdapter implements NotificationChannelAdapter {
    public NotificationPayload adapt(GeneratedMessage message) {
        // Format for FCM/APNs
        // Set priority level
        // Add deep links
        // Return push-specific payload
    }
}
```

### In-App Adapter
```java
@Service
public class InAppChannelAdapter implements NotificationChannelAdapter {
    public NotificationPayload adapt(GeneratedMessage message) {
        // Store in database
        // Set visibility flags
        // Add tracking metadata
        // Return in-app-specific payload
    }
}
```

### WhatsApp Adapter
```java
@Service
public class WhatsAppChannelAdapter implements NotificationChannelAdapter {
    public NotificationPayload adapt(GeneratedMessage message) {
        // Format with WhatsApp templates
        // Add button interactions
        // Include media if needed
        // Return WhatsApp-specific payload
    }
}
```

### Telegram Adapter
```java
@Service
public class TelegramChannelAdapter implements NotificationChannelAdapter {
    public NotificationPayload adapt(GeneratedMessage message) {
        // Format as Telegram message
        // Convert to Markdown
        // Add inline buttons
        // Return Telegram-specific payload
    }
}
```

## Configuration

### application.yml

```yaml
ollama:
  base-url: ${OLLAMA_URL:http://localhost:11434}
  model: ${OLLAMA_MODEL:llama2}
  temperature: ${OLLAMA_TEMPERATURE:0.7}
  top-p: ${OLLAMA_TOP_P:0.9}
  timeout-seconds: ${OLLAMA_TIMEOUT:30}

notification:
  channels:
    - EMAIL
    - SMS
    - PUSH
    - IN_APP
    - WHATSAPP
    - TELEGRAM

  message-types:
    - ALERT
    - TRANSACTIONAL
    - SYSTEM_UPDATE
    - MARKETING
```

### Environment Variables

```bash
OLLAMA_URL=http://localhost:11434
OLLAMA_MODEL=llama2
OLLAMA_TEMPERATURE=0.7
OLLAMA_TOP_P=0.9
OLLAMA_TIMEOUT=30
```

## Error Handling & Fallbacks

### Ollama Failure Scenarios

1. **Ollama Service Down**
    - Use pre-generated templates
    - Store as fallback message
    - Retry with exponential backoff

2. **Response Parsing Error**
    - Extract plaintext content
    - Apply basic formatting
    - Log for debugging

3. **Timeout**
    - Use template-based message
    - Circuit breaker prevents cascading failures
    - Fallback to original message content

4. **Invalid Response**
    - Validate JSON structure
    - Check required fields
    - Use template if validation fails

## Performance Optimization

### Caching Strategy

```
Similar Requests Cache (24h TTL)
├─ Event Type + Channel + User Segment
├─ Reduces Ollama calls
└─ Improves response time

User Preference Cache (1h TTL)
├─ User language, timezone, preferences
├─ Influences Ollama prompt
└─ Persistent cache key
```

### Batch Processing

```
Peak Load Handling
├─ Queue requests during peak hours
├─ Process in batches
├─ Use dedicated Ollama instance
└─ Distribute across worker pool
```

### Rate Limiting

```
Per-User Limits
├─ Max 100 messages per hour
├─ Circuit breaker protection
└─ Graceful degradation

Ollama Limits
├─ Concurrent requests: 5
├─ Queue depth: 1000
└─ Request timeout: 30 seconds
```

## Real-Time Delivery

### WebSocket Integration

```javascript
// Client-side: Listen for in-app notifications
const ws = new WebSocket('ws://localhost:8081/notifications/stream');

ws.onmessage = (event) => {
    const notification = JSON.parse(event.data);
    displayNotification(notification);
};
```

### Server-Side Streaming

```java
@GetMapping("/stream")
public SseEmitter subscribeToNotifications(@RequestParam String userId) {
    SseEmitter emitter = new SseEmitter(300000L);  // 5 min timeout
    notificationService.registerEmitter(userId, emitter);
    return emitter;
}
```

## Extensibility

### Adding New Channels

1. **Create Channel Adapter**
    - Implement `NotificationChannelAdapter`
    - Define channel-specific formatting

2. **Create Prompt Template**
    - Define channel-specific instructions
    - Add to prompt repository

3. **Configure Channel**
    - Add to `notification.channels` config
    - Register in adapter factory

4. **Test & Deploy**
    - Integration tests
    - Load testing
    - Production rollout

### Example: Adding Slack Channel

```java
@Service
public class SlackChannelAdapter implements NotificationChannelAdapter {
    @Override
    public NotificationPayload adapt(GeneratedMessage message) {
        SlackMessage slack = new SlackMessage();
        slack.setChannel("#alerts");
        slack.setText(message.getContent());
        slack.setAttachments(formatAsBlocks(message));
        return new NotificationPayload(slack);
    }
}
```

## Monitoring & Analytics

### Metrics to Track

- **Generation Time**: Ollama response latency
- **Success Rate**: Messages generated successfully
- **Channel Distribution**: Breakdown by channel type
- **Message Type Distribution**: Alert vs Marketing vs etc.
- **Cache Hit Rate**: Prompt template cache efficiency
- **Error Rate**: Failures by type

### Logging

```
INFO  - Message generation requested: user=123, channel=EMAIL
DEBUG - Prompt built: template=EMAIL, context_size=245
DEBUG - Ollama request: model=llama2, tokens=350
INFO  - Message generated successfully: length=1524, time=2.3s
ERROR - Ollama timeout after 30s, using fallback template
```

## Database Schema

```sql
-- Notification Templates
CREATE TABLE notification_templates (
    id UUID PRIMARY KEY,
    channel VARCHAR(50) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    template_text TEXT,
    prompt_instructions TEXT,
    created_at TIMESTAMP
);

-- Generated Messages
CREATE TABLE generated_messages (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    channel VARCHAR(50),
    message_type VARCHAR(50),
    generated_content TEXT,
    ollama_model VARCHAR(100),
    generation_time_ms INT,
    created_at TIMESTAMP
);

-- Channel Configurations
CREATE TABLE channel_configs (
    id UUID PRIMARY KEY,
    channel VARCHAR(50) UNIQUE,
    is_enabled BOOLEAN,
    api_key TEXT ENCRYPTED,
    config_json JSONB,
    created_at TIMESTAMP
);

-- User Channel Preferences
CREATE TABLE user_channel_preferences (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    channel VARCHAR(50),
    is_enabled BOOLEAN,
    created_at TIMESTAMP
);
```

## Security Considerations

1. **API Key Management**
    - Store in encrypted format
    - Rotate regularly
    - Use environment variables

2. **Input Validation**
    - Sanitize user context
    - Prevent prompt injection
    - Validate channel type

3. **Rate Limiting**
    - Per-user limits
    - Per-IP limits
    - Ollama instance limits

4. **Audit Logging**
    - Log all generated messages
    - Track user access
    - Monitor anomalies

## Performance Characteristics

| Operation | Time | Notes |
|-----------|------|-------|
| Ollama Generation | 2-5s | Depends on model, input size |
| Cache Hit | <10ms | Returns pre-generated message |
| Adapter Processing | 100-200ms | Format conversion |
| Kafka Publishing | 50-100ms | Queue to delivery |
| **Total (cold)** | 2.5-5.5s | End-to-end |
| **Total (cached)** | 200-400ms | With cache hit |

## Future Enhancements

1. **Multi-Model Support**
    - llama2, mistral, neural-chat
    - Model selection per channel
    - A/B testing

2. **Fine-Tuning**
    - Custom models for brand voice
    - User preference learning
    - Channel-specific optimization

3. **Advanced Analytics**
    - A/B testing framework
    - Engagement tracking
    - Optimization recommendations

4. **Personalization**
    - User segment targeting
    - Preference learning
    - Dynamic content generation

5. **Integration Ecosystem**
    - Zapier/IFTTT support
    - Webhook triggers
    - Third-party channel APIs
