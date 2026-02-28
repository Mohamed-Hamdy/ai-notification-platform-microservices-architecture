package com.worker.scheduler;

import com.worker.service.NotificationProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetryScheduler {

    private final NotificationProcessingService processingService;

    @Scheduled(fixedDelay = 60000)
    public void retryFailedNotifications() {
        log.info("Running scheduled retry job");
        processingService.retryFailedNotifications();
    }
}
