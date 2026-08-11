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

        var sortedTimeSlots = new ArrayList<>(timeSlots); //in case timeSlots is an immutable list
        Collections.sort(sortedTimeSlots, (t1, t2) -> t1.startTime().compareTo(t2.startTime()));
        List<TimeSlot> simplifiedTimeSlots = new ArrayList<>();
        LocalTime currStart = sortedTimeSlots.get(0).startTime;
        LocalTime currEnd = sortedTimeSlots.get(0).startTime;

        for(var t : sortedTimeSlots){
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

    public static List<TimeSlot> getFreeTimeSlots(List<TimeSlot> timeSlots, LocalTime startTime, LocalTime endTime){
        List<TimeSlot> simplifiedTimeSlots = simplifyTimeSlots(timeSlots);
        List<TimeSlot> freeTimeSlots = new ArrayList<>();
        LocalTime currStart = startTime;
        LocalTime currEnd = endTime;

        for(var t : simplifiedTimeSlots){
            if(t.startTime.compareTo(currStart) < 0){
                if(t.endTime.compareTo(currStart) > 0) currStart = t.endTime;
                continue;
            }
                
            if(t.endTime.compareTo(endTime) > 0){
                if(t.startTime.compareTo(endTime) < 0){
                    currEnd = t.startTime;
                }
                break;
            }
            if(!currStart.equals(t.startTime)) freeTimeSlots.add(new TimeSlot(currStart, t.startTime));
            currStart = t.endTime;
        }

        if(currStart.compareTo(currEnd) < 0) freeTimeSlots.add(new TimeSlot(currStart, currEnd));
        
        return freeTimeSlots;
    }
}
