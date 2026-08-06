package com.harro.goaltracker.services.llm;

import com.harro.goaltracker.dtos.EventDto;

import dev.langchain4j.service.UserMessage;

public interface EventExtractor {

    @UserMessage("Extract structured event fields from: {{it}}. Ignore Id and Schedule fields")
    EventDto chat(String message);
}
