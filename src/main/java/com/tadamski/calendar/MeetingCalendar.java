package com.tadamski.calendar;

import static java.util.stream.Collectors.toList;
import static net.time4j.range.TimestampInterval.between;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import net.time4j.PlainDate;
import net.time4j.PlainTime;
import net.time4j.PlainTimestamp;
import net.time4j.engine.CalendarDays;
import net.time4j.range.ChronoInterval;
import net.time4j.range.DateInterval;
import net.time4j.range.IntervalCollection;
import net.time4j.range.TimestampInterval;


public class MeetingCalendar {

    private List<Calendar> calendars;

    private MeetingCalendar(List<Calendar> calendars) {
        this.calendars = Collections.unmodifiableList(calendars);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<TimeSlot> findTimeForMeeting(LocalDateTime from, LocalDateTime to, int numberOfSlotsToFind, Duration duration) {
        if (to.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    String.format("Cannot search for meeting in the past (from: %s, to: %s)", from, to)
            );
        }

        TimestampInterval timeWindow = between(PlainTimestamp.from(from), PlainTimestamp.from(to));

        return this.calendars
                .parallelStream()
                .map(pc -> nonFreeTimeSlots(pc, timeWindow))
                .reduce(
                        IntervalCollection.onTimestampAxis(),
                        (nonFree, otherNonFree) -> nonFree.union(otherNonFree)
                )
                .withComplement(timeWindow)
                .withBlocks()
                .getIntervals()
                .stream()
                .map(toTimeSlot())
                .filter(noShorterThan(duration))
                .limit(numberOfSlotsToFind)
                .collect(toList());
    }

    private Function<ChronoInterval<PlainTimestamp>, TimeSlot> toTimeSlot() {
        return tsi -> new TimeSlot(
                tsi.getStart().getTemporal().toTemporalAccessor(),
                tsi.getEnd().getTemporal().toTemporalAccessor()
        );
    }

    private IntervalCollection<PlainTimestamp> nonFreeTimeSlots(Calendar calendar, TimestampInterval timeWindow) {
        return IntervalCollection.onTimestampAxis()
                .withTimeWindow(timeWindow)
                .plus(alreadyBookedTime(calendar))
                .union(workingTimeFor(calendar, timeWindow).withComplement(timeWindow));
    }

    private Predicate<? super TimeSlot> noShorterThan(Duration duration) {
        return slot -> duration.compareTo(slot.getDuration()) <= 0;
    }

    private List<TimestampInterval> alreadyBookedTime(Calendar calendar) {
        return calendar.getBookedTimeSlots().stream()
                .map(timeSlot -> between(
                                        PlainTimestamp.from(timeSlot.getStart()),
                                        PlainTimestamp.from(timeSlot.getStop())
                                )
                ).collect(toList());
    }

    private IntervalCollection<PlainTimestamp> workingTimeFor(Calendar calendar, TimestampInterval timeWindow) {
        DateInterval space = DateInterval.between(
                timeWindow.getStart().getTemporal().getCalendarDate(),
                timeWindow.getEnd().getTemporal().getCalendarDate()
        );

        PlainTime start = PlainTime.from(calendar.getWorkingHoursStart());
        PlainTime stop = PlainTime.from(calendar.getWorkingHoursStop());
        boolean stopNextDay = start.isAfter(stop);

        IntervalCollection<PlainTimestamp> workingTime = IntervalCollection.onTimestampAxis().withTimeWindow(
                timeWindow
        );
        PlainDate day = timeWindow.getStart().getTemporal().getCalendarDate();
        for (long i = 0; i < space.getLengthInDays(); i++) {
            workingTime = workingTime.plus(between(
                    day.at(start), stopNextDay ? day.with(PlainDate.DAY_OF_YEAR.incremented()).at(stop) : day.at(stop)
            ));
            day = day.plus(CalendarDays.ONE);
        }
        return workingTime;
    }

    public static class Builder {

        private List<Calendar> calendars = Collections.EMPTY_LIST;

        private Builder() {
        }

        public MeetingCalendar build() {
            return new MeetingCalendar(calendars);
        }

        public Calendar.Builder withPerson(String name) {
            return new Calendar.Builder(this).withName(name);
        }

        void addPersonalCalendar(Calendar calendar) {
            calendars = new LinkedList<>(calendars);
            calendars.add(calendar);
        }
    }
}
