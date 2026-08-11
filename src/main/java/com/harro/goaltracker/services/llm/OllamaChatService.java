package com.harro.goaltracker.services.llm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.harro.goaltracker.dtos.EventDto;
import com.harro.goaltracker.mappers.EventMapper;
import com.harro.goaltracker.types.TimeSlot;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import lombok.AllArgsConstructor;

@Service
public class OllamaChatService {
    private final ChatModel chatModel;
    private final PromptTemplate prompt;
    private final EventMapper eventMapper;

    public OllamaChatService(ChatModel chatModel,
        @Value("classpath:prompts/prompt.txt") Resource promptResource,
        EventMapper eventMapper) throws IOException{
            this.chatModel = chatModel;
            String promptText = promptResource.getContentAsString(StandardCharsets.UTF_8);
            this.prompt = PromptTemplate.from(promptText);
            this.eventMapper = eventMapper;
        }

    public List<EventDto> generateEvents(List<TimeSlot> timeSlots){
        List<EventDto> events = new ArrayList<>();
        for(var t : timeSlots){
            String eventName = chatModel.chat(
                prompt.apply(Map.of(
                    "startTime", t.startTime(),
                    "endTime", t.endTime(),
                    "duration", t.duration())
                ).text()
            );
            events.add(eventMapper.timeSlotToDto(t,eventName));
        }
        return events;
    }
}
