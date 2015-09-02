package com.tadamski.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.catchThrowable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;


public class MeetingCalendarTest {

    public static final int ALL_SLOTS = Integer.MAX_VALUE;
    public static final Duration ANY_DURATION = Duration.ZERO;

    @Test
    public void shouldRaiseExceptionForPeriodInPast() {
        //given
        MeetingCalendar meetingCalendar = MeetingCalendar.builder().build();

        //when
        Throwable exception = catchThrowable(() ->
                        meetingCalendar.findTimeForMeeting(
                                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2),
                                ALL_SLOTS, ANY_DURATION)
        );

        //then
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldReturnNoFreeTimeSlotsWhenOnePersonCompletelyBooked() {
        //given
        MeetingCalendar meetingCalendar = MeetingCalendar.builder()
                .withPerson("Adam").working("09:00", "17:00").busy(from(9), to(17))
                .build();

        //when
        List<TimeSlot> freeTimeSlots = meetingCalendar.findTimeForMeeting(from(9), to(10), ALL_SLOTS, ANY_DURATION);

        //then
        assertThat(freeTimeSlots).isEmpty();
    }

    @Test
    public void shouldReturnOneTimeSlotWhenOnePersonCompletelyFree() {
        //given
        MeetingCalendar meetingCalendar = MeetingCalendar.builder().withPerson("Adam").working("09:00",
                "17:00").build();

        //when
        List<TimeSlot> freeTimeSlots = meetingCalendar.findTimeForMeeting(from(9), to(17), ALL_SLOTS, ANY_DURATION);

        //then
        assertThat(freeTimeSlots).isNotEmpty();
        assertThat(freeTimeSlots).contains(new TimeSlot(from(9), to(17)));
    }

    @Test
    public void shouldReturnIntersectionOfWokingHoursOfTwoPeopleCompletelyFree() {
        //given
        MeetingCalendar meetingCalendar = MeetingCalendar.builder()
                .withPerson("Adam").working("10:00", "12:00")
                .and()
                .withPerson("Elisabeth").working("11:00", "13:00")
                .build();

        //when
        List<TimeSlot> freeTimeSlots = meetingCalendar.findTimeForMeeting(from(10), to(13), ALL_SLOTS, ANY_DURATION);

        //then
        assertThat(freeTimeSlots).containsExactly(new TimeSlot(from(11), to(12)));
    }

    @Test
    public void shouldReturnTimeSlotRightAfterBookedTime() {
        //given
        MeetingCalendar meetingCalendar = MeetingCalendar.builder()
                .withPerson("Adam").working("10:00", "13:00").busy(from(10), to(12))
                .build();

        //when
        List<TimeSlot> freeTimeSlots = meetingCalendar.findTimeForMeeting(from(9), to(14), ALL_SLOTS, ANY_DURATION);

        //then
        assertThat(freeTimeSlots).containsExactly(new TimeSlot(from(12), to(13)));

    }

    @Test
    public void shouldReturnTimeSlotRightBeforeBookedTime() {
        //given
        MeetingCalendar meetingCalendar = MeetingCalendar.builder()
                .withPerson("Adam").working("10:00", "13:00").busy(from(11), to(13))
                .build();

        //when
        List<TimeSlot> freeTimeSlots = meetingCalendar.findTimeForMeeting(from(9), to(14), ALL_SLOTS, ANY_DURATION);

        //then
        assertThat(freeTimeSlots).containsExactly(new TimeSlot(from(10), to(11)));
    }

    @Test
    public void shouldReturnNoTimeSlotsWhenNoIntersectionBetweenTwoPeopleWorkingHours(){
        //given
        MeetingCalendar meetingCalendar = MeetingCalendar.builder()
                .withPerson("Adam").working("10:00", "13:00")
                .and()
                .withPerson("Elisabeth").working("13:00", "16:00")
                .build();

        //when
        List<TimeSlot> freeTimeSlots = meetingCalendar.findTimeForMeeting(from(9), to(17), ALL_SLOTS, ANY_DURATION);

        //then
        assertThat(freeTimeSlots).isEmpty();
    }

    @Test
    public void shouldSupportAroundMidnightWorkingHours(){
        //given
        MeetingCalendar meetingCalendar = MeetingCalendar.builder()
                .withPerson("Adam").working("22:00", "06:00")
                .build();

        //when
        List<TimeSlot> freeTimeSlots = meetingCalendar.findTimeForMeeting(from(10), to(10).plusDays(1), ALL_SLOTS,
                ANY_DURATION);

        //then
        assertThat(freeTimeSlots).containsExactly(new TimeSlot(from(22), to(6).plusDays(1)));
    }

    @Test
    public void shouldHandleTwoDayBookings() {
        //given
        MeetingCalendar meetingCalendar = MeetingCalendar.builder().withPerson("Adam").working("09:00", "17:00")
                .busy(from(10), to(16).plusDays(1)).build();

        //when
        List<TimeSlot> freeTimeSlots = meetingCalendar.findTimeForMeeting(from(0), to(23, 59).plusDays(1), ALL_SLOTS,
                ANY_DURATION);

        //then

        assertThat(freeTimeSlots).containsExactly(
                new TimeSlot(from(9), to(10)),
                new TimeSlot(from(16).plusDays(1), to(17).plusDays(1))
        );
    }

    @Test
    public void shouldFindIntersectionInTwoDayCalendar() {
        //given
        MeetingCalendar meetingCalendar = MeetingCalendar.builder()
                .withPerson("Adam").working("09:00", "17:00")
                .busy(from(10), to(14))
                .busy(from(13).plusDays(1), to(17).plusDays(1))
                .and()
                .withPerson("Elisabeth").working("05:00", "13:00")
                .busy(from(5), to(8, 30))
                .busy(from(11).plusDays(1), to(13).plusDays(1))
                .build();

        //when
        List<TimeSlot> freeTimeSlots = meetingCalendar.findTimeForMeeting(from(0), to(23, 59).plusDays(1), ALL_SLOTS,
                ANY_DURATION);

        //then
        assertThat(freeTimeSlots).containsExactly(
                new TimeSlot(from(9), to(10)),
                new TimeSlot(from(9).plusDays(1), to(11).plusDays(1))
        );
    }

    @Test
    public void shouldReturnNoMoreTimeSlotsThanRequested() {
        //given
        MeetingCalendar meetingCalendar = MeetingCalendar.builder()
                .withPerson("Adam").working("09:00", "17:00")
                .busy(from(10), to(11))
                .busy(from(11, 30), to(12, 30))
                .busy(from(14), to(15))
                .build();

        //when
        int numberOfSlotsToFind = 2;
        List<TimeSlot> timeForMeeting = meetingCalendar.findTimeForMeeting(from(9), to(17), numberOfSlotsToFind,
                ANY_DURATION);

        //then
        assertThat(timeForMeeting).hasSize(numberOfSlotsToFind);
    }

    @Test
    public void shouldReturnOnlyTimeSlotsWithDurationNoShorterThanRequested() {
        //given
        MeetingCalendar meetingCalendar = MeetingCalendar.builder()
                .withPerson("Adam").working("09:00", "17:00")
                .busy(from(10), to(11))
                .busy(from(11, 30), to(12, 30))
                .busy(from(13, 30), to(15))
                .build();

        //when
        Duration sixtyMinutesMeeting = Duration.ofMinutes(60);
        List<TimeSlot> timeForMeeting = meetingCalendar.findTimeForMeeting(from(9), to(17), ALL_SLOTS,
                sixtyMinutesMeeting);

        //assertThat
        assertThat(timeForMeeting).hasSize(3);
    }

    private LocalDateTime to(int hour) {
        return to(hour, 0);
    }

    private LocalDateTime to(int h, int m) {
        return time(h, m);
    }

    private LocalDateTime from(int hour) {
        return from(hour, 0);
    }

    private LocalDateTime from(int h, int m) {
        return time(h, m);
    }

    private LocalDateTime time(int h, int m) {
        return LocalDateTime.now().plusDays(1).withHour(h).withMinute(m).withSecond(0).withNano(0);
    }

}
