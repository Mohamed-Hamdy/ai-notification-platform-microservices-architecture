package com.worker.consumer;

import com.worker.dto.NotificationEvent;
import com.worker.service.NotificationProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationProcessingService processingService;

    @KafkaListener(
            topics = "notification.requested",
            groupId = "worker-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received notification event from Kafka - NotificationId: {}, Partition: {}, Offset: {}",
                event.getNotificationId(), partition, offset);
        log.info("Event details - Recipient: {}, Channel: {}", event.getRecipient(), event.getChannel());

        try {
            processingService.processNotification(event.getNotificationId());
        } catch (Exception e) {
            log.error("Error processing notification ID: {}", event.getNotificationId(), e);
        }
    }
}
