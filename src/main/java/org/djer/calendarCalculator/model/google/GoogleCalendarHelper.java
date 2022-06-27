package org.djer.calendarCalculator.model.google;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

/**
 * @author Djer13
 *
 */
public class GoogleCalendarHelper {

    private static DateTimeFormatter RFC3339_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");

    private GoogleCalendarHelper() {
        super();
    }

    public static LocalDateTime extractStartDateTime(Event event) {
        DateTime start = event.getStart().getDateTime();
        if (start == null) {
            start = event.getStart().getDate();
        }
        LocalDateTime startLocalDate = toLocaDate(start);

        return startLocalDate;
    }

    public static LocalDateTime extractEndDateTime(Event event) {
        DateTime end = event.getEnd().getDateTime();
        if (end == null) {
            end = event.getEnd().getDate();
        }
        LocalDateTime endLocalDate = toLocaDate(end);

        return endLocalDate;
    }

    private static LocalDateTime toLocaDate(DateTime gDate) {
        return LocalDateTime.parse(gDate.toStringRfc3339(), RFC3339_FORMATTER);
    }

}
