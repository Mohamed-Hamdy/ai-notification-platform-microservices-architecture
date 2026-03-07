package com.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class OllamaService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String ollamaBaseUrl;
    private final String modelName;
    private final Double temperature;
    private final Double topP;
    private final Integer timeoutSeconds;

    public OllamaService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${ollama.base-url:http://localhost:11434}") String ollamaBaseUrl,
            @Value("${ollama.model:llama2}") String modelName,
            @Value("${ollama.temperature:0.7}") Double temperature,
            @Value("${ollama.top-p:0.9}") Double topP,
            @Value("${ollama.timeout-seconds:30}") Integer timeoutSeconds
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.ollamaBaseUrl = ollamaBaseUrl;
        this.modelName = modelName;
        this.temperature = temperature;
        this.topP = topP;
        this.timeoutSeconds = timeoutSeconds;
    }

    public GenerationResult generateMessage(String prompt) {
        try {
            long startTime = System.currentTimeMillis();

            Map<String, Object> request = buildRequest(prompt);
            String response = callOllama(request);

            long duration = System.currentTimeMillis() - startTime;
            String content = extractContent(response);

            log.info("Ollama generation successful. Duration: {}ms, Model: {}", duration, modelName);

            return new GenerationResult(
                    true,
                    content,
                    duration,
                    null
            );
        } catch (Exception ex) {
            log.error("Ollama generation failed: {}", ex.getMessage(), ex);
            return new GenerationResult(
                    false,
                    null,
                    0L,
                    ex.getMessage()
            );
        }
    }

    private Map<String, Object> buildRequest(String prompt) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", modelName);
        request.put("prompt", prompt);
        request.put("stream", false);
        request.put("temperature", temperature);
        request.put("top_p", topP);

        Map<String, Object> options = new HashMap<>();
        options.put("num_predict", 512);
        options.put("top_k", 40);
        options.put("top_p", topP);
        request.put("options", options);

        return request;
    }

    private String callOllama(Map<String, Object> request) {
        try {
            String url = ollamaBaseUrl + "/api/generate";
            log.debug("Calling Ollama API: {}", url);

            String jsonRequest = objectMapper.writeValueAsString(request);
            String response = restTemplate.postForObject(url, jsonRequest, String.class);

            return response;
        } catch (RestClientException ex) {
            throw new OllamaException("Failed to call Ollama API", ex);
        } catch (Exception ex) {
            throw new OllamaException("Error preparing Ollama request", ex);
        }
    }

    private String extractContent(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String response_text = root.path("response").asText();

            if (response_text.isEmpty()) {
                log.warn("Empty response from Ollama");
                return null;
            }

            return response_text;
        } catch (Exception ex) {
            log.error("Failed to parse Ollama response", ex);
            return null;
        }
    }

    public boolean isAvailable() {
        try {
            String url = ollamaBaseUrl + "/api/tags";
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return response != null && response.has("models");
        } catch (Exception ex) {
            log.warn("Ollama service not available: {}", ex.getMessage());
            return false;
        }
    }

    public static class GenerationResult {
        public final boolean successful;
        public final String content;
        public final Long durationMs;
        public final String error;

        public GenerationResult(boolean successful, String content, Long durationMs, String error) {
            this.successful = successful;
            this.content = content;
            this.durationMs = durationMs;
            this.error = error;
        }
    }

    public static class OllamaException extends RuntimeException {
        public OllamaException(String message) {
            super(message);
        }

        public OllamaException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
