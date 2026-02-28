package com.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private Long notificationId;
    private String recipient;
    private String subject;
    private String message;
    private String channel;
    private LocalDateTime timestamp;
}
