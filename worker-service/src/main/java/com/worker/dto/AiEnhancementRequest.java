package com.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiEnhancementRequest {
    private String subject;
    private String message;
    private String channel;
}
