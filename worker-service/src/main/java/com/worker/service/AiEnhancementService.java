package com.worker.service;

import com.worker.client.AiServiceClient;
import com.worker.dto.AiEnhancementRequest;
import com.worker.dto.AiEnhancementResponse;
import com.worker.model.NotificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiEnhancementService {

    private final AiServiceClient aiServiceClient;

    public AiEnhancementService(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    public EnhancementResult enhance(String subject, String message, String channel) {
        try {
            log.info("Requesting AI enhancement for channel: {}", channel);

            AiEnhancementRequest request = new AiEnhancementRequest(subject, message, channel);
            AiEnhancementResponse response = aiServiceClient.enhanceMessage(request);

            log.info("AI enhancement successful. Strategy: {}, Confidence: {}",
                    response.getOptimizationStrategy(),
                    response.getConfidenceScore());

            return new EnhancementResult(
                    true,
                    response.getOptimizedSubject(),
                    response.getEnhancedMessage(),
                    response.getConfidenceScore(),
                    null
            );
        } catch (Exception ex) {
            log.warn("AI enhancement failed, using fallback. Error: {}", ex.getMessage());

            return new EnhancementResult(
                    false,
                    subject,
                    message,
                    0.0,
                    ex.getMessage()
            );
        }
    }

    public static class EnhancementResult {
        public final boolean successful;
        public final String enhancedSubject;
        public final String enhancedMessage;
        public final Double confidenceScore;
        public final String errorMessage;

        public EnhancementResult(boolean successful, String enhancedSubject, String enhancedMessage,
                                 Double confidenceScore, String errorMessage) {
            this.successful = successful;
            this.enhancedSubject = enhancedSubject;
            this.enhancedMessage = enhancedMessage;
            this.confidenceScore = confidenceScore;
            this.errorMessage = errorMessage;
        }
    }
}
