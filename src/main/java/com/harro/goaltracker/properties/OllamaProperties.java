package com.harro.goaltracker.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ollama")
public record OllamaProperties(
    String baseUrl, 
    String modelName, 
    Double temperature,
    Double topP, 
    Integer topK
) {}
