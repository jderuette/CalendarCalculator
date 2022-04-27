/**
 * 
 */
package org.djer.calendarCalculator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
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

    private List<Event> getEvents(String user, String calendar) throws GeneralSecurityException, IOException {
        Calendar service = loadedCalendarService.get(user);
        if (null == service) {
            loadCalendarApi(user);
        }

        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary").setMaxResults(10).setTimeMin(now).setOrderBy("startTime")
                .setSingleEvents(true).execute();
        List<Event> items = events.getItems();

        return items;
    }

    private void displayEventInConsole(List<Event> events) {

        if (events.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : events) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }

    }

    public void displayEventInConsole(String user) throws IOException, GeneralSecurityException {
        loadCalendarApi(user);
        List<Event> items = getEvents(user, "primary");
        displayEventInConsole(items);

    }
}
