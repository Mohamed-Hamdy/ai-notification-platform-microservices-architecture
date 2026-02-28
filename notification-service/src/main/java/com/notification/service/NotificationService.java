package com.notification.service;

import com.notification.dto.NotificationEvent;
import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.model.Notification;
import com.notification.model.NotificationStatus;
import com.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification for recipient: {}", request.getRecipient());

        Notification notification = Notification.builder()
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .message(request.getMessage())
                .channel(request.getChannel())
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification saved with ID: {}", savedNotification.getId());

        NotificationEvent event = NotificationEvent.builder()
                .notificationId(savedNotification.getId())
                .recipient(savedNotification.getRecipient())
                .subject(savedNotification.getSubject())
                .message(savedNotification.getMessage())
                .channel(savedNotification.getChannel())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaProducerService.sendNotificationEvent(event);

        return NotificationResponse.builder()
                .id(savedNotification.getId())
                .status(savedNotification.getStatus().name())
                .message("Notification created successfully")
                .build();
    }
}
