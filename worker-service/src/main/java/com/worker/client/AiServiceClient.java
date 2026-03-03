package com.worker.client;

import com.worker.dto.AiEnhancementRequest;
import com.worker.dto.AiEnhancementResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "ai-service",
        url = "${ai-service.url:http://localhost:8083}"
)
public interface AiServiceClient {
    @PostMapping("/ai/optimize")
    @CircuitBreaker(
            name = "aiServiceCircuitBreaker",
            fallbackMethod = "enhanceMessageFallback"
    )
    AiEnhancementResponse enhanceMessage(@RequestBody AiEnhancementRequest request);

    default AiEnhancementResponse enhanceMessageFallback(AiEnhancementRequest request, Exception ex) {
        return new AiEnhancementResponse(
                request.getSubject(),
                request.getSubject(),
                request.getMessage(),
                request.getMessage(),
                "Fallback: AI service unavailable",
                0.0
        );
    }
}
