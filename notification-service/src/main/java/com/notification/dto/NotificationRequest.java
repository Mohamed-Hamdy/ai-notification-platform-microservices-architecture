
package com.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "Recipient is required")
    @Email(message = "Recipient must be a valid email")
    private String recipient;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    @NotBlank(message = "Channel is required")
    @Pattern(regexp = "EMAIL|SMS|PUSH", message = "Channel must be EMAIL, SMS, or PUSH")
    private String channel;
}
