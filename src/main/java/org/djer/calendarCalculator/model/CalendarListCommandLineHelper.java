package org.djer.calendarCalculator.model;

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
public class CalendarListCommandLineHelper {

    private Options options = null;
    private CommandLine line = null;

    /**
     * idenitifiant d'un utilisateur (local) permettant de stocker et ré-utiliser les token d'authentifications.
     */
    private String user;

    public CalendarListCommandLineHelper(final String[] args) {
        defineCommandLineOptions();
        parseCommandLine(args);
    }

    private void defineCommandLineOptions() {
        options = new Options();
        options.addOption("h", "help", false, "Affiche l'aide de l'outil de listing de calendrier.");
        options.addOption("u", "user", true, "Utilisateur LOCAL. Chaque utilisateur est lié à un compte Google");
        options.addOption("a", "action", false,
                "Actiion à effectuer 'liste' pour la liste des calendrier. Toutes autre valeur permet d'extraire des données du calendrier.");
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
    }

    private void displayHelp() {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("affichage de la liste des calendriers Google", options);
    }

    public String getUser() {
        return user;
    }

}
