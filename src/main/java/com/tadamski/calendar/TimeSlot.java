package com.tadamski.calendar;


import java.time.Duration;
import java.time.LocalDateTime;

public class TimeSlot {

    private LocalDateTime startInclusive;
    private LocalDateTime stopExclusive;

    public TimeSlot(LocalDateTime startInclusive, LocalDateTime stopExclusive) {
        this.startInclusive = startInclusive;
        this.stopExclusive = stopExclusive;
    }

    public LocalDateTime getStart() {
        return startInclusive;
    }

    public LocalDateTime getStop() {
        return stopExclusive;
    }

    public Duration getDuration() {
        return Duration.between(startInclusive, stopExclusive);
    }

    @Override
    public String toString() {
        return "[" + startInclusive + "," + stopExclusive + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeSlot timeSlot = (TimeSlot) o;

        if (startInclusive != null ? !startInclusive.equals(timeSlot.startInclusive) : timeSlot.startInclusive != null)
            return false;
        return !(stopExclusive != null ? !stopExclusive.equals(
                timeSlot.stopExclusive) : timeSlot.stopExclusive != null);

    }

    @Override
    public int hashCode() {
        int result = startInclusive != null ? startInclusive.hashCode() : 0;
        result = 31 * result + (stopExclusive != null ? stopExclusive.hashCode() : 0);
        return result;
    }
}
