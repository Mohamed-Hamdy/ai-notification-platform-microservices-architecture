package com.ai.controller;

import com.ai.dto.OptimizationRequest;
import com.ai.dto.OptimizationResponse;
import com.ai.service.AiOptimizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiOptimizationService aiOptimizationService;

    @PostMapping("/optimize")
    public ResponseEntity<OptimizationResponse> optimizeContent(
            @Valid @RequestBody OptimizationRequest request) {
        log.info("Received optimization request for channel: {}", request.getChannel());
        OptimizationResponse response = aiOptimizationService.optimizeContent(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Service is running");
    }
}
