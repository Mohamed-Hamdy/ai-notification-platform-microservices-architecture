package com.worker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiEnhancementResponse {
    private String originalSubject;
    private String optimizedSubject;
    private String originalMessage;
    private String enhancedMessage;
    private String optimizationStrategy;
    private Double confidenceScore;
}
