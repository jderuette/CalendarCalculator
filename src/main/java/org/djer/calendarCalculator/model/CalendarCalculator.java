/**
 * 
 */
package org.djer.calendarCalculator.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.google.api.services.calendar.model.Event;

/**
 * @author Djer13
 *
 */
public class CalendarCalculator {

    public static Long sumDurationinMinutes(final List<Event> events) {
        if (null == events) {
            return 0l;
        }

        long nbMins = 0;

        for (Event event : events) {
            nbMins += extracDuration(event, ChronoUnit.MINUTES);
        }
        return nbMins;
    }

    private static Long extracDuration(final Event event, final ChronoUnit tempUnit) {
        Instant start = Instant.ofEpochMilli(event.getStart().getDateTime().getValue());
        Instant end = Instant.ofEpochMilli(event.getEnd().getDateTime().getValue());

        // Period period = Period.between(start, end);

        return tempUnit.between(start, end);
    }

    public static void addStringSumary(StringBuilder sb, int currentDay, int nbEventThisDay,
            int cumulativeDurationPerDay) {

        sb.append("======================= ");
        sb.append(" Jour : ");
        sb.append(currentDay);
        sb.append(" => ");
        sb.append(nbEventThisDay);
        sb.append(" evennements");
        sb.append(" ---- ");
        sb.append(cumulativeDurationPerDay);
        sb.append("mins");
        sb.append(System.lineSeparator());

    }

}
