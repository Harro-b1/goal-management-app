package com.harro.goaltracker.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.harro.goaltracker.properties.OllamaProperties;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

@Configuration
@EnableConfigurationProperties(OllamaProperties.class)
public class OllamaConfig {
    
    @Bean
    public ChatModel chatModel(OllamaProperties props){
        return OllamaChatModel.builder()
                .baseUrl(props.baseUrl())
                .modelName(props.modelName())
                .temperature(props.temperature())
                .timeout(Duration.ofMinutes(3))
                .topP(props.topP())
                .topK(props.topK())
                .build();
    }
}
