package com.harro.goaltracker.services;

import com.harro.goaltracker.dtos.EventDto;

import dev.langchain4j.service.UserMessage;

public interface EventExtractor {

    @UserMessage("Extract structured event fields from: {{it}}")
    EventDto chat(String message);
}
