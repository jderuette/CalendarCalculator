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
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private List<Event> getEvents(String user, String calendar, LocalDateTime from, LocalDateTime to, ZoneId userZoneId,
            String query) throws GeneralSecurityException, IOException {
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

    private String humanReadableEventQuery(com.google.api.services.calendar.Calendar.Events.List eventQuery) {

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

    private void displayEventInConsole(List<Event> events) {

        if (events.isEmpty()) {
            System.out.println("Aucun évennement corespondant aux critères");
        } else {
            System.out.println("Liste des évennements");
            for (Event event : events) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }

    }

    private void displayDurationInConsole(List<Event> events) {
        long durationInMinutes = CalendarCalculator.sumDurationinMinutes(events);

        long DurationInHourFullPart = durationInMinutes / 60;
        long DurationInHourModulo = durationInMinutes % 60;

        System.out.println("Durée total des évènnements : " + durationInMinutes + " minutes, soit : "
                + DurationInHourFullPart + "h" + DurationInHourModulo + "min");
    }

    public void displayEventInConsole(String user, String calendarId, LocalDateTime from, LocalDateTime to,
            ZoneId userZoneId, String query) throws IOException, GeneralSecurityException {

        loadCalendarApi(user);

        List<Event> items = getEvents(user, calendarId, from, to, userZoneId, query);
        displayEventInConsole(items);
        displayDurationInConsole(items);

    }

    public class CalendarCalculator {

        public static Long sumDurationinMinutes(List<Event> events) {
            if (null == events) {
                return 0l;
            }

            long nbMins = 0;

            for (Event event : events) {
                nbMins += extracDuration(event);
            }
            return nbMins;
        }

        private static Long extracDuration(Event event) {
            Instant start = Instant.ofEpochMilli(event.getStart().getDateTime().getValue());
            Instant end = Instant.ofEpochMilli(event.getEnd().getDateTime().getValue());

            // Period period = Period.between(start, end);

            return ChronoUnit.MINUTES.between(start, end);

        }
    }
}
