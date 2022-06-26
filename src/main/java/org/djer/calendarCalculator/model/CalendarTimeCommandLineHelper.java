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
public class CalendarTimeCommandLineHelper {

    private Options options = null;
    private CommandLine line = null;

    /**
     * idenitifiant d'un utilisateur (local) permettant de stocker et ré-utiliser les token d'authentifications.
     */
    private String user;
    /** identifiant du calendrier dans lequel effectuer la requete. */
    private String calendar;
    /** rechercher à partir de cette date. */
    private LocalDateTime from;
    /** rechercher jusqu'a cette date. */
    private LocalDateTime to;
    /** filtre surles dates (query). */
    private String query;
    /** Fuseau horraire de l'utilisateur utilisant l'appli. */
    private ZoneId userTimeZone;
    /** Nombre d'heurs de travail par jours (pour calcul du TJM). **/
    private float workHourPerDay;
    /** TJM **/
    private float daillyRate;

    public CalendarTimeCommandLineHelper(final String[] args) {
        userTimeZone = ZoneOffset.systemDefault();
        workHourPerDay = 7;
        daillyRate = 300;

        defineCommandLineOptions();
        parseCommandLine(args);
    }

    private void defineCommandLineOptions() {
        options = new Options();
        options.addOption("h", "help", false, "Affiche ce message");
        options.addOption("u", "user", true, "Utilisateur LOCAL. Chaque utilisateur est lié à un compte Google");
        options.addOption("c", "calendar", true, "Calendrier à utiliser pour extraire les évennements");
        options.addOption("f", "from", true,
                "Date de début de recherche des évennements. Jour au format 20220401 (commence au DEBUT), ou forma ISO : 2022-04-01T12:00:00.");
        options.addOption("t", "to", true,
                "Date de fin de recherche des évennements. Jour au format 20220431 (termine à la FIN), ou format ISO : 2022-04-01T12:00:00.");
        options.addOption("q", "query", true, "Filtre (query) sur les évènnements");
        options.addOption("tz", "timeZone", true,
                "Votre fuseau horraire local. Par defaut celui de votre Systeme d'Exploitation.");
        options.addOption("whpt", "workHourPerDay", true, "Heures de travail par jours par defaut 7.");
        options.addOption("dr", "daillyRate", true, "Taux Journalier : TJM par defaut 300.");

    }

    private void parseCommandLine(final String[] args) {
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

        if (line.hasOption("workHourPerDay")) {
            workHourPerDay = Float.parseFloat(line.getOptionValue("workHourPerDay"));
        }

        if (line.hasOption("daillyRate")) {
            daillyRate = Float.parseFloat(line.getOptionValue("daillyRate"));
        }
    }

    private String parseDateToValideFromDateTime(final String cmdLineDate) {
        return parseDateToValidDateTime(cmdLineDate, "00:00:00");
    }

    private String parseDateToValideEndDateTime(final String cmdLineDate) {
        return parseDateToValidDateTime(cmdLineDate, "23:59:59");
    }

    private String parseDateToValidDateTime(final String cmdLineDate, final String hoursPart) {
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

    public float getWorkHourPerDay() {
        return workHourPerDay;
    }

    public float getDaillyRate() {
        return daillyRate;
    }
}
