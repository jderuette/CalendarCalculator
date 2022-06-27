/**
 * 
 */
package org.djer.calendarCalculator.model.google;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.google.api.services.calendar.model.Event;

/**
 * @author Djer13
 *
 */
public class CompletedEvent {

    private Event googleEvent;
    private LocalDateTime start;
    private LocalDateTime end;
    private long durationinMinutes;

    public CompletedEvent(Event gEvent) {
        this.googleEvent = gEvent;
        this.start = GoogleCalendarHelper.extractStartDateTime(gEvent);
        this.end = GoogleCalendarHelper.extractEndDateTime(gEvent);
        this.durationinMinutes = ChronoUnit.MINUTES.between(this.start, this.end);
    }

    /**
     * @return the googleEvent
     */
    public Event getGoogleEvent() {
        return googleEvent;
    }

    /**
     * @return the start
     */
    public LocalDateTime getStart() {
        return start;
    }

    /**
     * @return the end
     */
    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * @return the durationinMinutes
     */
    public long getDurationinMinutes() {
        return durationinMinutes;
    }

    /**
     * @return the event Summary
     */
    public String getSummary() {
        return googleEvent.getSummary();
    }

}
