package com.notification.service;

import com.notification.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private static final String TOPIC = "notification.requested";

    public void sendNotificationEvent(NotificationEvent event) {
        log.info("Sending notification event to Kafka topic: {} for notificationId: {}",
                TOPIC, event.getNotificationId());

        kafkaTemplate.send(TOPIC, event.getNotificationId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully sent notification event to Kafka. NotificationId: {}",
                                event.getNotificationId());
                    } else {
                        log.error("Failed to send notification event to Kafka. NotificationId: {}",
                                event.getNotificationId(), ex);
                    }
                });
    }
}
