package org.djer.calendarCalculator;

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
        options.addOption("f", "from", true, "Date de début de recherche des évennements");
        options.addOption("t", "to", true, "Date de fin de recherche des évennements");
        options.addOption("q", "query", true, "Filtre (query) sur les évènnements");
        options.addOption("tz", "timeZone", true, "Votre fuseau horraire local");

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
        if (line.hasOption("from")) {
            from = LocalDateTime.parse(line.getOptionValue("from"), DateTimeFormatter.BASIC_ISO_DATE);
        }
        if (line.hasOption("to")) {
            to = LocalDateTime.parse(line.getOptionValue("to"), DateTimeFormatter.BASIC_ISO_DATE);
        }
        if (line.hasOption("query")) {
            query = line.getOptionValue("query");
        }
        if (line.hasOption("tz")) {
            userTimeZone = ZoneOffset.of(line.getOptionValue("tz"));
        }
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
