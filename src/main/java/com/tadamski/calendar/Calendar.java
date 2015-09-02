package com.tadamski.calendar;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class Calendar {

    private String personName;
    private LocalTime workingHoursStart;
    private LocalTime workingHoursStop;
    private List<TimeSlot> bookedTimeSlots;

    Calendar(String personName, LocalTime workingHoursStart, LocalTime workingHoursStop, Collection<TimeSlot> bookedTimeSlots) {
        this.personName = personName;
        this.workingHoursStart = workingHoursStart;
        this.workingHoursStop = workingHoursStop;
        this.bookedTimeSlots = new ArrayList<>(bookedTimeSlots);
    }

    public String getPersonName() {
        return personName;
    }

    public LocalTime getWorkingHoursStart() {
        return workingHoursStart;
    }

    public LocalTime getWorkingHoursStop() {
        return workingHoursStop;
    }

    public Collection<TimeSlot> getBookedTimeSlots() {
        return bookedTimeSlots;
    }

    public static class Builder {

        private final MeetingCalendar.Builder calendarBuilder;
        private String name;
        private LocalTime workingHoursStart;
        private LocalTime workingHoursStop;
        private List<TimeSlot> bookedTimeSlots = new LinkedList<>();

        public Builder(MeetingCalendar.Builder calendarBuilder) {
            this.calendarBuilder = calendarBuilder;
        }


        public Builder working(String from, String to) {
            workingHoursStart = LocalTime.parse(from);
            workingHoursStop = LocalTime.parse(to);
            return this;
        }

        public Builder busy(LocalDateTime from, LocalDateTime to) {
            bookedTimeSlots.add(new TimeSlot(from, to));
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public MeetingCalendar build() {
            calendarBuilder.addPersonalCalendar(buildImmutablePersonalCalendar());
            return calendarBuilder.build();
        }

        public MeetingCalendar.Builder and() {
            calendarBuilder.addPersonalCalendar(buildImmutablePersonalCalendar());
            return calendarBuilder;
        }

        private Calendar buildImmutablePersonalCalendar() {
            return new Calendar(name, workingHoursStart, workingHoursStop, new ArrayList<>(bookedTimeSlots));
        }
    }
}
