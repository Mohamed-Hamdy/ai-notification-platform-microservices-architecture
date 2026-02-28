package com.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationResponse {
    private String originalSubject;
    private String optimizedSubject;
    private String originalMessage;
    private String enhancedMessage;
    private String optimizationStrategy;
    private Double confidenceScore;
}
