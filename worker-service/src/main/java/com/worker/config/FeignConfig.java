package com.worker.config;

import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            if (response.status() >= 500) {
                return new Exception("AI Service unavailable: " + response.status());
            }
            if (response.status() == 400) {
                return new Exception("Invalid AI enhancement request");
            }
            if (response.status() == 404) {
                return new Exception("AI Service endpoint not found");
            }
            return new Exception("AI Service error: " + response.status());
        };
    }
}
