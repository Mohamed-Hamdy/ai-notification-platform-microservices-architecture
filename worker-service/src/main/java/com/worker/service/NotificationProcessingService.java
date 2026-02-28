package com.worker.service;

import com.worker.model.Notification;
import com.worker.model.NotificationStatus;
import com.worker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProcessingService {

    private final NotificationRepository notificationRepository;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private final Random random = new Random();

    @Transactional
    public void processNotification(Long notificationId) {
        log.info("Starting processing for notification ID: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        try {
            notification.setStatus(NotificationStatus.PROCESSING);
            notificationRepository.save(notification);

            simulateNotificationSending(notification);

            notification.setStatus(NotificationStatus.SENT);
            notification.setErrorMessage(null);
            notificationRepository.save(notification);

            log.info("Successfully processed notification ID: {}", notificationId);

        } catch (Exception e) {
            log.error("Failed to process notification ID: {}", notificationId, e);
            handleFailure(notification, e);
        }
    }

    private void simulateNotificationSending(Notification notification) throws InterruptedException {
        log.info("Sending {} notification to: {}", notification.getChannel(), notification.getRecipient());
        log.info("Subject: {}", notification.getSubject());

        Thread.sleep(2000);

        boolean shouldFail = random.nextInt(10) < 2;

        if (shouldFail && notification.getRetryCount() < MAX_RETRY_ATTEMPTS) {
            throw new RuntimeException("Simulated sending failure");
        }

        log.info("Notification sent successfully via {}", notification.getChannel());
    }

    @Transactional
    protected void handleFailure(Notification notification, Exception e) {
        int retryCount = notification.getRetryCount() != null ? notification.getRetryCount() : 0;
        notification.setRetryCount(retryCount + 1);
        notification.setErrorMessage(e.getMessage());

        if (notification.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
            notification.setStatus(NotificationStatus.FAILED);
            log.error("Notification ID {} failed after {} attempts", notification.getId(), MAX_RETRY_ATTEMPTS);
        } else {
            notification.setStatus(NotificationStatus.RETRY);
            log.warn("Notification ID {} marked for retry. Attempt: {}",
                    notification.getId(), notification.getRetryCount());
        }

        notificationRepository.save(notification);
    }

    @Transactional
    public void retryFailedNotifications() {
        log.info("Checking for notifications to retry...");
        var retryNotifications = notificationRepository.findByStatus(NotificationStatus.RETRY);

        for (Notification notification : retryNotifications) {
            if (notification.getRetryCount() < MAX_RETRY_ATTEMPTS) {
                log.info("Retrying notification ID: {} (attempt {})",
                        notification.getId(), notification.getRetryCount() + 1);
                processNotification(notification.getId());
            }
        }
    }
}
