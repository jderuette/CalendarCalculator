package org.djer.calendarCalculator;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.djer.calendarCalculator.model.CalendarListCommandLineHelper;
import org.djer.calendarCalculator.model.CalendarTimeCommandLineHelper;
import org.djer.calendarCalculator.model.GoogleCalendarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Djer13
 *
 */
@SpringBootApplication
public class CalendarCalcultorLauncher implements CommandLineRunner {

    private static Logger LOG = LoggerFactory.getLogger(CalendarCalcultorLauncher.class);

    public static void main(String[] args) {

        LOG.info("STARTING THE APPLICATION");
        System.out.println(
                "Bienvenu si ce n'est déja fait votre naviguateur va s'ouvrir pour vous demander de choisir un compte Google auquel vous connecter");
        SpringApplication.run(CalendarCalcultorLauncher.class, args);
        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) throws IOException, GeneralSecurityException {
        LOG.info("EXECUTING : command line runner");
        Boolean actionParamFound = false;
        Boolean listAction = false;
        for (String s : args) {
            if (!actionParamFound) {
                if (s.contains("-a")) {
                    actionParamFound = true;
                }
            } else {
                if (s.contains("list")) {
                    listAction = true;
                    actionParamFound = false;
                }
            }
        }

        GoogleCalendarService service = new GoogleCalendarService();

        if (listAction) {
            CalendarListCommandLineHelper commandLineHelper = new CalendarListCommandLineHelper(args);
            service.listeAclendars(commandLineHelper.getUser());

        } else {
            CalendarTimeCommandLineHelper commandLineHelper = new CalendarTimeCommandLineHelper(args);

            service.displayEventInConsole(commandLineHelper.getUser(), commandLineHelper.getCalendar(),
                    commandLineHelper.getFrom(), commandLineHelper.getTo(), commandLineHelper.getUserTimeZone(),
                    commandLineHelper.getQuery(), commandLineHelper.getWorkHourPerDay(),
                    commandLineHelper.getDaillyRate());
        }
    }
}
