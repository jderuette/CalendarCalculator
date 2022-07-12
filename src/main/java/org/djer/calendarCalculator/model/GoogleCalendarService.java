/**
 * 
 */
package org.djer.calendarCalculator.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.djer.calendarCalculator.model.google.CompletedEvent;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * @author Djer13
 *
 */
/* class to demonstarte use of Calendar events list API */
public class GoogleCalendarService {

    /** Store loaded Services */
    Map<String, Calendar> loadedCalendarService = new HashMap<>();

    /** Application name. */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final Path APP_DATA_FOLDER = Paths.get(System.getProperty("user.home"), "app", "CalendarCalculator",
            "data");
    /** Directory to store authorization tokens for this application. */
    private static final String TOKENS_DIRECTORY_PATH = APP_DATA_FOLDER.resolve("tokens").toString();

    /**
     * Global instance of the scopes required by this quickstart. If modifying these scopes, delete your
     * previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

    private static final String CREDENTIALS_FILE_PATH = APP_DATA_FOLDER.resolve("credentials.json").toString();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    // .withZone(ZoneId.of("UTC"));

    /**
     * Creates an authorized Credential object.
     * 
     * @param HTTP_TRANSPORT
     *                       The network HTTP Transport.
     * 
     * @return An authorized Credential object.
     * 
     * @throws IOException
     *                     If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final String user, final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.

        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            in = new FileInputStream(CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize(user);
        // returns an authorized Credential object.
        return credential;
    }

    /**
     * Build a new authorized API client service.
     * 
     * @param user
     * 
     * @return
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private void loadCalendarApi(final String user) throws GeneralSecurityException, IOException {
        if (!loadedCalendarService.containsKey(user)) {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(user, HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME).build();

            loadedCalendarService.put(user, service);
        }
    }

    private CalendarList getCalendarList(final String user) throws GeneralSecurityException, IOException {

        Calendar service = loadedCalendarService.get(user);
        if (null == service) {
            loadCalendarApi(user);
        }

        CalendarList calendarList = service.calendarList().list().execute();

        return calendarList;
    }

    private List<Event> getEvents(final String user, final String calendar, final LocalDateTime from,
            final LocalDateTime to, final ZoneId userZoneId, final String query)
            throws GeneralSecurityException, IOException {
        Calendar service = loadedCalendarService.get(user);
        if (null == service) {
            loadCalendarApi(user);
        }

        com.google.api.services.calendar.Calendar.Events.List eventQuery = service.events().list(calendar)
                .setMaxResults(100).setOrderBy("startTime").setSingleEvents(true);

        if (null != from) {
            Instant fromInstant = from.toInstant(userZoneId.getRules().getOffset(from));
            DateTime gfrom = DateTime.parseRfc3339(formatter.withZone(userZoneId).format(fromInstant));
            eventQuery.setTimeMin(gfrom);
        }

        if (null != to) {
            Instant toInstant = to.toInstant(userZoneId.getRules().getOffset(to));
            DateTime gTo = DateTime.parseRfc3339(formatter.withZone(userZoneId).format(toInstant));
            eventQuery.setTimeMax(gTo);
        }

        if (null != query) {
            eventQuery.setQ(query);
        }

        System.out.println("recherche des évènnement respectant ces critères : " + humanReadableEventQuery(eventQuery));

        // List the next 10 events from the primary calendar.
        Events events = eventQuery.execute();
        List<Event> items = events.getItems();

        return items;
    }

    private String humanReadableEventQuery(final com.google.api.services.calendar.Calendar.Events.List eventQuery) {

        StringBuilder sb = new StringBuilder();
        sb.append("Calendrier").append(eventQuery.getCalendarId()).append(" entre ").append(eventQuery.getTimeMin())
                .append(" et ").append(eventQuery.getTimeMax());

        if (null != eventQuery.getQ()) {
            sb.append(" contenant : '").append(eventQuery.getQ()).append("'");
        } else {
            sb.append(" Sans aucun autre filtre");
        }

        return sb.toString();
    }

    private void displayEventInConsole(final List<Event> events, Boolean displayEventDuration,
            Boolean displayDaySummary) {

        StringBuilder sb = null;

        int currentDay = -1;
        int nbEventThisDay = 0;
        int cumulativeDurationPerDay = 0;
        boolean isLastEvent = false;

        if (events.isEmpty()) {
            System.out.println("Aucun évennement corespondant aux critères");
        } else {
            System.out.println("Liste des évennements");
            Iterator<Event> itEvents = events.iterator();
            while (itEvents.hasNext()) {
                Event event = itEvents.next();
                sb = new StringBuilder(100);
                CompletedEvent completedEvent = new CompletedEvent(event);
                isLastEvent = !itEvents.hasNext();

                if (displayDaySummary) {
                    int eventDay = completedEvent.getStart().getDayOfMonth();
                    if (currentDay == -1) {
                        currentDay = eventDay;
                    }
                    if (currentDay == eventDay) {
                        nbEventThisDay++;
                        cumulativeDurationPerDay += completedEvent.getDurationinMinutes();
                    } else {

                        CalendarCalculator.addStringSumary(sb, currentDay, nbEventThisDay, cumulativeDurationPerDay);

                        // reset
                        currentDay = eventDay;
                        nbEventThisDay = 0;
                        cumulativeDurationPerDay = 0;
                    }

                }

                sb.append(System.lineSeparator());
                // end of summary

                sb.append(completedEvent.getSummary());
                sb.append(" le ");
                sb.append(completedEvent.getStart());

                if (displayEventDuration) {
                    sb.append("      DUREE : ");
                    sb.append(completedEvent.getDurationinMinutes());
                    sb.append("mins");
                }

                // end of event details

                // specific summary for last Day
                if (displayDaySummary && isLastEvent) {
                    if (nbEventThisDay == 0) {
                        nbEventThisDay++;
                        cumulativeDurationPerDay += completedEvent.getDurationinMinutes();
                    }

                    sb.append(System.lineSeparator());
                    CalendarCalculator.addStringSumary(sb, currentDay, nbEventThisDay, cumulativeDurationPerDay);
                }
                // END specific summary last day

                System.out.println(sb.toString());
            }
        }
    }

    private void displayEventInConsole(final List<Event> events) {
        displayEventInConsole(events, Boolean.TRUE, Boolean.TRUE);

    }

    private void displayDurationInConsole(final List<Event> events, final float nbHourWorkDay, final float daillyRate) {
        long durationInMinutes = CalendarCalculator.sumDurationinMinutes(events);

        long DurationInHourFullPart = durationInMinutes / 60;
        long DurationInHourModulo = durationInMinutes % 60;
        float durationInWorkDay = durationInMinutes / 60 / nbHourWorkDay;
        float totalAmmount = durationInWorkDay * daillyRate;

        System.out.println("Durée total des évènnements : " + durationInMinutes + " minutes, soit : "
                + DurationInHourFullPart + "h" + DurationInHourModulo + "min. Soit : " + durationInWorkDay
                + " jours. Soit un montant de : " + totalAmmount);
    }

    public void displayEventInConsole(final String user, final String calendarId, final LocalDateTime from,
            final LocalDateTime to, final ZoneId userZoneId, final String query, final float nbHourWorkDay,
            final float daillyRate) throws IOException, GeneralSecurityException {

        loadCalendarApi(user);

        List<Event> items = getEvents(user, calendarId, from, to, userZoneId, query);
        displayEventInConsole(items);
        displayDurationInConsole(items, nbHourWorkDay, daillyRate);

    }

    public void listeAclendars(String user) throws GeneralSecurityException, IOException {
        loadCalendarApi(user);

        CalendarList calendarsData = getCalendarList(user);
        displayCalendarInConsole(calendarsData);
    }

    private void displayCalendarInConsole(CalendarList calendarsData) {
        StringBuilder sb = new StringBuilder();
        System.out.println("Liste de vos calendrier : ");
        for (CalendarListEntry calendar : calendarsData.getItems()) {
            sb.append(calendar.getId());
            sb.append(" : ");
            sb.append(calendar.getSummary());
            if (null != calendar.getSummaryOverride()) {
                sb.append(" ==> ");
                sb.append(calendar.getSummaryOverride());
            }
            sb.append(System.lineSeparator());
        }
        System.out.println(sb.toString());

    }

}
