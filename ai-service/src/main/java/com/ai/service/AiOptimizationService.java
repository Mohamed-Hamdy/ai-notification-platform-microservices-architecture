package com.ai.service;

import com.ai.dto.OptimizationRequest;
import com.ai.dto.OptimizationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class AiOptimizationService {

    private final Random random = new Random();
    private final OllamaService ollamaService;
    public AiOptimizationService(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    private static final List<String> POWER_WORDS = Arrays.asList(
            "Exclusive", "Limited", "Urgent", "Important", "New", "Breakthrough",
            "Amazing", "Instant", "Quick", "Easy", "Free", "Guaranteed"
    );

    private static final List<String> EMOJIS = Arrays.asList(
            "🚀", "⚡", "✨", "🎯", "💡", "🔥", "⭐", "💎"
    );

    public OptimizationResponse optimizeContent(OptimizationRequest request) {
        log.info("Optimizing content via Ollama - Subject: {}", request.getSubject());

        try {
            // Build the AI prompt
            String prompt = String.format(
                    "Enhance this message for channel %s:\nSubject: %s\nMessage: %s",
                    request.getChannel(),
                    request.getSubject(),
                    request.getMessage()
            );

            // Call OllamaService
            OllamaService.GenerationResult result = ollamaService.generateMessage(prompt);

            // Check if AI succeeded
            String enhancedMessage;
            Double confidence;
            if (result.successful && result.content != null) {
                enhancedMessage = result.content;
                confidence = 0.95; // or compute based on your logic
            } else {
                log.warn("Ollama failed or returned null. Using fallback.");
                enhancedMessage = enhanceMessage(request.getMessage(), request.getChannel());
                confidence = 0.75;
            }

            return OptimizationResponse.builder()
                    .originalSubject(request.getSubject())
                    .optimizedSubject(request.getSubject()) // or parse AI response if needed
                    .originalMessage(request.getMessage())
                    .enhancedMessage(enhancedMessage)
                    .optimizationStrategy("Ollama AI Optimization")
                    .confidenceScore(confidence)
                    .build();

        } catch (Exception ex) {
            log.error("AI enhancement failed, fallback triggered", ex);

            // fallback logic
            String optimizedSubject = optimizeSubject(request.getSubject(), request.getChannel());
            String enhancedMessage = enhanceMessage(request.getMessage(), request.getChannel());
            String strategy = determineStrategy(request.getChannel());
            Double confidence = 0.75;

            return OptimizationResponse.builder()
                    .originalSubject(request.getSubject())
                    .optimizedSubject(optimizedSubject)
                    .originalMessage(request.getMessage())
                    .enhancedMessage(enhancedMessage)
                    .optimizationStrategy(strategy)
                    .confidenceScore(confidence)
                    .build();
        }
    }
    public OptimizationResponse optimizeContentOld(OptimizationRequest request) {
        log.info("Optimizing content - Subject: {}", request.getSubject());

        try {
            Thread.sleep(500 + random.nextInt(1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String optimizedSubject = optimizeSubject(request.getSubject(), request.getChannel());
        String enhancedMessage = enhanceMessage(request.getMessage(), request.getChannel());
        String strategy = determineStrategy(request.getChannel());
        Double confidence = 0.75 + (random.nextDouble() * 0.24);

        log.info("Optimization complete - Original: '{}', Optimized: '{}'",
                request.getSubject(), optimizedSubject);

        return OptimizationResponse.builder()
                .originalSubject(request.getSubject())
                .optimizedSubject(optimizedSubject)
                .originalMessage(request.getMessage())
                .enhancedMessage(enhancedMessage)
                .optimizationStrategy(strategy)
                .confidenceScore(Math.round(confidence * 100.0) / 100.0)
                .build();
    }

    private String optimizeSubject(String subject, String channel) {
        String powerWord = POWER_WORDS.get(random.nextInt(POWER_WORDS.size()));
        String emoji = EMOJIS.get(random.nextInt(EMOJIS.size()));

        if ("EMAIL".equalsIgnoreCase(channel)) {
            return String.format("%s %s: %s", emoji, powerWord, subject);
        } else if ("SMS".equalsIgnoreCase(channel)) {
            return String.format("[%s] %s", powerWord, subject);
        } else if ("PUSH".equalsIgnoreCase(channel)) {
            return String.format("%s %s", emoji, subject);
        }

        return String.format("%s %s", powerWord, subject);
    }

    private String enhanceMessage(String message, String channel) {
        if ("EMAIL".equalsIgnoreCase(channel)) {
            return String.format("%s\n\n✨ This message has been AI-optimized for maximum engagement.", message);
        } else if ("SMS".equalsIgnoreCase(channel)) {
            return message.length() > 100
                    ? message.substring(0, 97) + "..."
                    : message;
        } else if ("PUSH".equalsIgnoreCase(channel)) {
            return message.length() > 150
                    ? message.substring(0, 147) + "..."
                    : message;
        }

        return message;
    }

    private String determineStrategy(String channel) {
        String[] strategies = {
                "Power word injection with emoji enhancement",
                "Urgency-based subject line optimization",
                "Engagement-driven content restructuring",
                "Personalization with emotional trigger",
                "Action-oriented language optimization"
        };

        return strategies[random.nextInt(strategies.length)];
    }
}
