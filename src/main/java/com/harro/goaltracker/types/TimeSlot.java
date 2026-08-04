package com.harro.goaltracker.types;

import java.time.Duration;
import java.time.LocalTime;

public record TimeSlot(LocalTime startTime, LocalTime endTime, Duration duration) {
    public TimeSlot(LocalTime startTime, LocalTime endTime){
        this(startTime, endTime, Duration.between(startTime, endTime));
    }
}
