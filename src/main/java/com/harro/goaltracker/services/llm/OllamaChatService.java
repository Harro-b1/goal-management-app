package com.harro.goaltracker.services.llm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;

@Service
public class OllamaChatService {
    private final ChatModel chatModel;
    private final PromptTemplate prompt;

    public OllamaChatService(ChatModel chatModel,
        @Value("classpath:prompts/prompt.txt") Resource promptResource) throws IOException{
            this.chatModel = chatModel;
            String promptText = promptResource.getContentAsString(StandardCharsets.UTF_8);
            this.prompt = PromptTemplate.from(promptText);
        }

    public String chat(String input){

        return chatModel.chat(prompt + input);
    }
}
