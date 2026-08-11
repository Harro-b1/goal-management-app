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
    void simplifyTimeSlots_duplicateIdenticalTimeSlots_collapseToOne() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(9, 0), t(10, 0)),
            new TimeSlot(t(9, 0), t(10, 0))
        );

        List<TimeSlot> result = TimeSlot.simplifyTimeSlots(input);

        assertEquals(List.of(new TimeSlot(t(9, 0), t(10, 0))), result);
    }

    @Test
    void simplifyTimeSlots_threeWayTransitiveOverlap_mergesIntoOneSlot() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(9, 0), t(11, 0)),
            new TimeSlot(t(10, 0), t(13, 0)),
            new TimeSlot(t(12, 0), t(14, 0))
        );

        List<TimeSlot> result = TimeSlot.simplifyTimeSlots(input);

        assertEquals(List.of(new TimeSlot(t(9, 0), t(14, 0))), result);
    }

    @Test
    void getFreeTimeSlots_emptyList_returnsWholeDayFree() {
        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(new ArrayList<>(), LocalTime.MIN, LocalTime.MAX);

        assertEquals(List.of(new TimeSlot(LocalTime.MIN, LocalTime.MAX)), result);
    }

    @Test
    void getFreeTimeSlots_singleBusySlotMidDay_returnsFreeBeforeAndAfter() {
        List<TimeSlot> input = mutable(new TimeSlot(t(9, 0), t(10, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, LocalTime.MIN, LocalTime.MAX);

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

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, LocalTime.MIN, LocalTime.MAX);

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

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, LocalTime.MIN, LocalTime.MAX);

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

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, LocalTime.MIN, LocalTime.MAX);

        assertEquals(List.of(
            new TimeSlot(LocalTime.MIN, t(9, 0)),
            new TimeSlot(t(11, 0), LocalTime.MAX)
        ), result);
    }

    @Test
    void getFreeTimeSlots_busySlotStartsAtMidnight_noSpuriousZeroLengthFreeSlotAtStart() {
        List<TimeSlot> input = mutable(new TimeSlot(LocalTime.MIN, t(9, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, LocalTime.MIN, LocalTime.MAX);

        assertEquals(List.of(new TimeSlot(t(9, 0), LocalTime.MAX)), result);
    }

    @Test
    void getFreeTimeSlots_busySlotEndsAtEndOfDay_noSpuriousZeroLengthFreeSlotAtEnd() {
        List<TimeSlot> input = mutable(new TimeSlot(t(18, 0), LocalTime.MAX));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, LocalTime.MIN, LocalTime.MAX);

        assertEquals(List.of(new TimeSlot(LocalTime.MIN, t(18, 0))), result);
    }

    @Test
    void getFreeTimeSlots_busySlotCoversEntireDay_returnsNoFreeSlots() {
        List<TimeSlot> input = mutable(new TimeSlot(LocalTime.MIN, LocalTime.MAX));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, LocalTime.MIN, LocalTime.MAX);

        assertTrue(result.isEmpty());
    }

    @Test
    void getFreeTimeSlots_noBusySlots_freeStartsAtGivenStartTime() {
        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(new ArrayList<>(), t(9, 0), LocalTime.MAX);

        assertEquals(List.of(new TimeSlot(t(9, 0), LocalTime.MAX)), result);
    }

    @Test
    void getFreeTimeSlots_busySlotEntirelyBeforeStartTime_isIgnored() {
        List<TimeSlot> input = mutable(new TimeSlot(t(6, 0), t(7, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), LocalTime.MAX);

        assertEquals(List.of(new TimeSlot(t(9, 0), LocalTime.MAX)), result);
    }

    @Test
    void getFreeTimeSlots_busySlotOverlappingStartTimeBoundary_freeDoesNotStartBeforeStartTime() {
        List<TimeSlot> input = mutable(new TimeSlot(t(8, 0), t(10, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), LocalTime.MAX);

        assertEquals(List.of(new TimeSlot(t(10, 0), LocalTime.MAX)), result);
        for (TimeSlot slot : result) {
            assertTrue(slot.startTime().compareTo(t(9, 0)) >= 0,
                "free slot " + slot + " starts before startTime 09:00");
        }
    }

    @Test
    void getFreeTimeSlots_busySlotStartsExactlyAtStartTime_noSpuriousZeroLengthSlot() {
        // startTime and the busy slot's start both carry the value 09:15 but are
        // constructed as separate LocalTime instances, unlike whole-hour values
        // (e.g. 09:00) which the JDK caches to the same instance.
        List<TimeSlot> input = mutable(new TimeSlot(t(9, 15), t(10, 15)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 15), LocalTime.MAX);

        assertEquals(List.of(new TimeSlot(t(10, 15), LocalTime.MAX)), result);
    }

    @Test
    void getFreeTimeSlots_multipleBusySlotsWithStartTimeClipping_returnsCorrectGaps() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(6, 0), t(7, 0)),
            new TimeSlot(t(11, 0), t(12, 0)),
            new TimeSlot(t(14, 0), t(15, 0))
        );

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), LocalTime.MAX);

        assertEquals(List.of(
            new TimeSlot(t(9, 0), t(11, 0)),
            new TimeSlot(t(12, 0), t(14, 0)),
            new TimeSlot(t(15, 0), LocalTime.MAX)
        ), result);
    }

    @Test
    void getFreeTimeSlots_startTimeAtMidnight_matchesOldDefaultBehavior() {
        List<TimeSlot> input = mutable(new TimeSlot(t(9, 0), t(10, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, LocalTime.MIN, LocalTime.MAX);

        assertEquals(List.of(
            new TimeSlot(LocalTime.MIN, t(9, 0)),
            new TimeSlot(t(10, 0), LocalTime.MAX)
        ), result);
    }

    @Test
    void getFreeTimeSlots_noBusySlots_freeSpansStartTimeToEndTime() {
        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(new ArrayList<>(), t(9, 0), t(17, 0));

        assertEquals(List.of(new TimeSlot(t(9, 0), t(17, 0))), result);
    }

    @Test
    void getFreeTimeSlots_busySlotEntirelyAfterEndTime_isIgnored() {
        List<TimeSlot> input = mutable(new TimeSlot(t(18, 0), t(19, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), t(17, 0));

        assertEquals(List.of(new TimeSlot(t(9, 0), t(17, 0))), result);
    }

    @Test
    void getFreeTimeSlots_busySlotWithinWindow_returnsGapsOnBothSides() {
        List<TimeSlot> input = mutable(new TimeSlot(t(11, 0), t(12, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), t(17, 0));

        assertEquals(List.of(
            new TimeSlot(t(9, 0), t(11, 0)),
            new TimeSlot(t(12, 0), t(17, 0))
        ), result);
    }

    @Test
    void getFreeTimeSlots_busySlotEndsExactlyAtEndTime_noTrailingFreeSlot() {
        List<TimeSlot> input = mutable(new TimeSlot(t(15, 0), t(17, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), t(17, 0));

        assertEquals(List.of(new TimeSlot(t(9, 0), t(15, 0))), result);
    }

    @Test
    void getFreeTimeSlots_busySlotOverlappingEndTimeBoundary_freeDoesNotExtendPastEndTime() {
        List<TimeSlot> input = mutable(new TimeSlot(t(16, 0), t(18, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), t(17, 0));

        assertEquals(List.of(new TimeSlot(t(9, 0), t(16, 0))), result);
        for (TimeSlot slot : result) {
            assertTrue(slot.endTime().compareTo(t(17, 0)) <= 0,
                "free slot " + slot + " extends past endTime 17:00");
        }
    }

    @Test
    void getFreeTimeSlots_busySlotSpansEntireWindowBeyondBothBoundaries_returnsNoFreeSlots() {
        List<TimeSlot> input = mutable(new TimeSlot(t(8, 0), t(18, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), t(17, 0));

        assertTrue(result.isEmpty());
    }

    @Test
    void getFreeTimeSlots_multipleBusySlotsWithEndTimeClipping_returnsCorrectGaps() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(10, 0), t(11, 0)),
            new TimeSlot(t(16, 0), t(18, 0))
        );

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), t(17, 0));

        assertEquals(List.of(
            new TimeSlot(t(9, 0), t(10, 0)),
            new TimeSlot(t(11, 0), t(16, 0))
        ), result);
    }

    @Test
    void getFreeTimeSlots_zeroWidthWindow_noBusySlots_returnsEmptyList() {
        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(new ArrayList<>(), t(9, 0), t(9, 0));

        assertTrue(result.isEmpty());
    }

    @Test
    void getFreeTimeSlots_zeroWidthWindow_withBusySlotSpanningIt_returnsEmptyList() {
        List<TimeSlot> input = mutable(new TimeSlot(t(8, 0), t(10, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), t(9, 0));

        assertTrue(result.isEmpty());
    }

    @Test
    void getFreeTimeSlots_busySlotExactlyMatchesWholeWindow_returnsEmptyList() {
        List<TimeSlot> input = mutable(new TimeSlot(t(9, 0), t(17, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), t(17, 0));

        assertTrue(result.isEmpty());
    }

    @Test
    void getFreeTimeSlots_busySlotsClippedOnBothStartAndEndBoundaries_returnsMiddleGapOnly() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(8, 0), t(10, 0)),
            new TimeSlot(t(16, 0), t(18, 0))
        );

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), t(17, 0));

        assertEquals(List.of(new TimeSlot(t(10, 0), t(16, 0))), result);
    }

    @Test
    void getFreeTimeSlots_thirdBusySlotExceedsEndTime_firstTwoStillProduceCorrectGaps() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(10, 0), t(11, 0)),
            new TimeSlot(t(12, 0), t(13, 0)),
            new TimeSlot(t(16, 0), t(18, 0))
        );

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), t(17, 0));

        assertEquals(List.of(
            new TimeSlot(t(9, 0), t(10, 0)),
            new TimeSlot(t(11, 0), t(12, 0)),
            new TimeSlot(t(13, 0), t(16, 0))
        ), result);
    }

    @Test
    void getFreeTimeSlots_zeroDurationBusySlotMidWindow_doesNotFragmentFreeTime() {
        List<TimeSlot> input = mutable(new TimeSlot(t(12, 0), t(12, 0)));

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), t(17, 0));

        assertEquals(List.of(new TimeSlot(t(9, 0), t(17, 0))), result);
    }

    @Test
    void getFreeTimeSlots_allResultingSlots_haveNonNegativeDuration() {
        List<TimeSlot> input = mutable(
            new TimeSlot(t(6, 0), t(10, 0)),
            new TimeSlot(t(11, 0), t(11, 30)),
            new TimeSlot(t(16, 30), t(20, 0))
        );

        List<TimeSlot> result = TimeSlot.getFreeTimeSlots(input, t(9, 0), t(17, 0));

        for (TimeSlot slot : result) {
            assertTrue(!slot.duration().isNegative(),
                "free slot " + slot + " has a negative duration");
            assertTrue(slot.startTime().compareTo(t(9, 0)) >= 0,
                "free slot " + slot + " starts before startTime 09:00");
            assertTrue(slot.endTime().compareTo(t(17, 0)) <= 0,
                "free slot " + slot + " ends after endTime 17:00");
        }
    }
}
