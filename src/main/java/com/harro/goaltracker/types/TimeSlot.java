package com.harro.goaltracker.types;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record TimeSlot(LocalTime startTime, LocalTime endTime, Duration duration) {
    public TimeSlot(LocalTime startTime, LocalTime endTime){
        this(startTime, endTime, Duration.between(startTime, endTime));
    }

    public static List<TimeSlot> simplifyTimeSlots(List<TimeSlot> timeSlots){
        if(timeSlots.isEmpty()){
            return timeSlots;
        }

        Collections.sort(timeSlots, (t1, t2) -> t1.startTime().compareTo(t2.startTime()));
        List<TimeSlot> simplifiedTimeSlots = new ArrayList<>();
        LocalTime currStart = timeSlots.get(0).startTime;
        LocalTime currEnd = timeSlots.get(0).startTime;

        for(var t : timeSlots){
            if(currEnd.compareTo(t.startTime) < 0){
                simplifiedTimeSlots.add(new TimeSlot(currStart, currEnd));
                currStart = t.startTime;
                currEnd = t.endTime;
                continue;
            }else if(currEnd.compareTo(t.endTime) < 0){
                currEnd = t.endTime;
            }
        }

        if(currStart!=currEnd){
            simplifiedTimeSlots.add(new TimeSlot(currStart, currEnd));
        }

        return simplifiedTimeSlots;
    }

    public static List<TimeSlot> getFreeTimeSlots(List<TimeSlot> timeSlots){
        List<TimeSlot> simplifiedTimeSlots = simplifyTimeSlots(timeSlots);
        List<TimeSlot> freeTimeSlots = new ArrayList<>();
        LocalTime currStart = LocalTime.MIN;
        for(var t : simplifiedTimeSlots){
            if(currStart != t.startTime) freeTimeSlots.add(new TimeSlot(currStart, t.startTime));
            currStart = t.endTime;
        }

        if(currStart != LocalTime.MAX) freeTimeSlots.add(new TimeSlot(currStart, LocalTime.MAX));

        return freeTimeSlots;
    }
}
