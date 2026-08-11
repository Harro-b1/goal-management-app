package com.harro.goaltracker.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class TimeSlotTest {

    private static LocalTime t(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }

    private static List<TimeSlot> mutable(TimeSlot... slots) {
        return new ArrayList<>(List.of(slots));
    }

    @Test
    void simplifyTimeSlots_emptyList_returnsEmptyList() {
        List<TimeSlot> result = TimeSlot.simplifyTimeSlots(new ArrayList<>());

        assertTrue(result.isEmpty());
    }

    @Test
    void simplifyTimeSlots_singleTimeSlot_returnsSameTimeSlot() {
        List<TimeSlot> input = mutable(new TimeSlot(t(9, 0), t(10, 0)));

        List<TimeSlot> result = TimeSlot.simplifyTimeSlots(input);

        assertEquals(List.of(new TimeSlot(t(9, 0), t(10, 0))), result);
    }

    @Test
    void simplifyTimeSlots_twoDisjointTimeSlots_returnsBothUnchanged() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(9, 0), t(10, 0)),
            new TimeSlot(t(11, 0), t(12, 0))
        );

        List<TimeSlot> result = TimeSlot.simplifyTimeSlots(input);

        assertEquals(List.of(
            new TimeSlot(t(9, 0), t(10, 0)),
            new TimeSlot(t(11, 0), t(12, 0))
        ), result);
    }

    @Test
    void simplifyTimeSlots_overlappingTimeSlots_mergeIntoOne() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(9, 0), t(11, 0)),
            new TimeSlot(t(10, 0), t(12, 0))
        );

        List<TimeSlot> result = TimeSlot.simplifyTimeSlots(input);

        assertEquals(List.of(new TimeSlot(t(9, 0), t(12, 0))), result);
    }

    @Test
    void simplifyTimeSlots_adjacentTouchingTimeSlots_mergeIntoOne() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(9, 0), t(10, 0)),
            new TimeSlot(t(10, 0), t(11, 0))
        );

        List<TimeSlot> result = TimeSlot.simplifyTimeSlots(input);

        assertEquals(List.of(new TimeSlot(t(9, 0), t(11, 0))), result);
    }

    @Test
    void simplifyTimeSlots_fullyContainedTimeSlot_mergesIntoContainingSlot() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(9, 0), t(12, 0)),
            new TimeSlot(t(10, 0), t(11, 0))
        );

        List<TimeSlot> result = TimeSlot.simplifyTimeSlots(input);

        assertEquals(List.of(new TimeSlot(t(9, 0), t(12, 0))), result);
    }

    @Test
    void simplifyTimeSlots_unsortedInputWithMultipleGroups_returnsSortedMergedGroups() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(13, 0), t(14, 0)),
            new TimeSlot(t(9, 0), t(10, 0)),
            new TimeSlot(t(9, 30), t(11, 0)),
            new TimeSlot(t(15, 0), t(16, 0))
        );

        List<TimeSlot> result = TimeSlot.simplifyTimeSlots(input);

        assertEquals(List.of(
            new TimeSlot(t(9, 0), t(11, 0)),
            new TimeSlot(t(13, 0), t(14, 0)),
            new TimeSlot(t(15, 0), t(16, 0))
        ), result);
    }

    @Test
    void getFreeTimeSlots_emptyList_returnsWholeDayFree() {
        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(new ArrayList<>());

        assertEquals(List.of(new TimeSlot(LocalTime.MIN, LocalTime.MAX)), result);
    }

    @Test
    void getFreeTimeSlots_singleBusySlotMidDay_returnsFreeBeforeAndAfter() {
        List<TimeSlot> input = mutable(new TimeSlot(t(9, 0), t(10, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input);

        assertEquals(List.of(
            new TimeSlot(LocalTime.MIN, t(9, 0)),
            new TimeSlot(t(10, 0), LocalTime.MAX)
        ), result);
    }

    @Test
    void getFreeTimeSlots_twoDisjointBusySlots_returnsThreeFreeGaps() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(9, 0), t(10, 0)),
            new TimeSlot(t(11, 0), t(12, 0))
        );

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input);

        assertEquals(List.of(
            new TimeSlot(LocalTime.MIN, t(9, 0)),
            new TimeSlot(t(10, 0), t(11, 0)),
            new TimeSlot(t(12, 0), LocalTime.MAX)
        ), result);
    }

    @Test
    void getFreeTimeSlots_overlappingBusySlots_areSimplifiedBeforeInverting() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(9, 0), t(11, 0)),
            new TimeSlot(t(10, 0), t(12, 0))
        );

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input);

        assertEquals(List.of(
            new TimeSlot(LocalTime.MIN, t(9, 0)),
            new TimeSlot(t(12, 0), LocalTime.MAX)
        ), result);
    }

    @Test
    void getFreeTimeSlots_adjacentBusySlots_areMergedBeforeInverting() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(9, 0), t(10, 0)),
            new TimeSlot(t(10, 0), t(11, 0))
        );

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input);

        assertEquals(List.of(
            new TimeSlot(LocalTime.MIN, t(9, 0)),
            new TimeSlot(t(11, 0), LocalTime.MAX)
        ), result);
    }

    @Test
    void getFreeTimeSlots_busySlotStartsAtMidnight_noSpuriousZeroLengthFreeSlotAtStart() {
        List<TimeSlot> input = mutable(new TimeSlot(LocalTime.MIN, t(9, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input);

        assertEquals(List.of(new TimeSlot(t(9, 0), LocalTime.MAX)), result);
    }

    @Test
    void getFreeTimeSlots_busySlotEndsAtEndOfDay_noSpuriousZeroLengthFreeSlotAtEnd() {
        List<TimeSlot> input = mutable(new TimeSlot(t(18, 0), LocalTime.MAX));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input);

        assertEquals(List.of(new TimeSlot(LocalTime.MIN, t(18, 0))), result);
    }

    @Test
    void getFreeTimeSlots_busySlotCoversEntireDay_returnsNoFreeSlots() {
        List<TimeSlot> input = mutable(new TimeSlot(LocalTime.MIN, LocalTime.MAX));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input);

        assertTrue(result.isEmpty());
    }
}
