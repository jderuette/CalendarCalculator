package org.djer.calendarCalculator.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author Djer13
 *
 */
public class CommandLineHelper {

    private Options options = null;
    private CommandLine line = null;

    private String user;
    private String calendar;
    private LocalDateTime from;
    private LocalDateTime to;
    private String query;
    private ZoneId userTimeZone;

    public CommandLineHelper() {
        userTimeZone = ZoneOffset.systemDefault();
        defineCommandLineOptions();
    }

    private void defineCommandLineOptions() {
        options = new Options();
        options.addOption("h", "help", false, "Affiche ce message");
        options.addOption("u", "user", true, "Utilisateur LOCAL. Chaque utilisateur est lié à un compte Google");
        options.addOption("c", "calendar", true, "Calendrier à utiliser pour extraire les évennements");
        options.addOption("f", "from", true,
                "Date de début de recherche des évennements. Jour au format 20220401 (commence au DEBUT), ou format ISO 2022-04-01T12:00:00.");
        options.addOption("t", "to", true,
                "Date de fin de recherche des évennements. Jour au format 20220431 (termeine à la FIN), ou format ISO 2022-04-01T12:00:00.");
        options.addOption("q", "query", true, "Filtre (query) sur les évènnements");
        options.addOption("tz", "timeZone", true,
                "Votre fuseau horraire local. Par defaut celui de votre Systeme d'Exploitation.");

    }

    public void parseCommandLine(String[] args) {
        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
            handle();
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err
                    .println("impossible d'analyse les données de la ligne de commande.  Motif : " + exp.getMessage());
            displayHelp();
        }
    }

    private void handle() {
        if (line.hasOption("h")) {
            displayHelp();
        }
        if (line.hasOption("user")) {
            user = line.getOptionValue("user");
        }
        if (line.hasOption("calendar")) {
            calendar = line.getOptionValue("calendar");
        }

        if (line.hasOption("tz")) {
            userTimeZone = ZoneOffset.of(line.getOptionValue("tz"));
        }

        if (line.hasOption("from")) {
            from = LocalDateTime.parse(parseDateToValideFromDateTime(line.getOptionValue("from")),
                    DateTimeFormatter.ISO_DATE_TIME);
        }
        if (line.hasOption("to")) {
            to = LocalDateTime.parse(parseDateToValideEndDateTime(line.getOptionValue("to")),
                    DateTimeFormatter.ISO_DATE_TIME);
        }
        if (line.hasOption("query")) {
            query = line.getOptionValue("query");
        }
    }

    private String parseDateToValideFromDateTime(String cmdLineDate) {
        return parseDateToValidDateTime(cmdLineDate, "00:00:00");
    }

    private String parseDateToValideEndDateTime(String cmdLineDate) {
        return parseDateToValidDateTime(cmdLineDate, "23:59:59");
    }

    private String parseDateToValidDateTime(String cmdLineDate, String hoursPart) {
        String validFrom = cmdLineDate;
        // [" +userTimeZone.getId() + "]"
        String defaultHourPart = "T" + hoursPart;
        if (cmdLineDate.length() == 8) {
            validFrom = cmdLineDate.substring(0, 4) + "-" + cmdLineDate.substring(4, 6) + "-"
                    + cmdLineDate.substring(6, 8) + defaultHourPart;
        } else if (cmdLineDate.length() == 11) {
            validFrom = cmdLineDate + defaultHourPart;
        }

        return validFrom;
    }

    private void displayHelp() {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Extrait des données d'un calendrier Google", options);
    }

    public String getUser() {
        return user;
    }

    public String getCalendar() {
        return calendar;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public String getQuery() {
        return query;
    }

    public ZoneId getUserTimeZone() {
        return userTimeZone;
    }

}
