/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.libis;

import be.libis.digitool.ToolBox;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

/**
 *
 * @author KrisD
 */
public class GeneralOptionsHandler {

    private static Handler console_handler = new ConsoleLogger();

    static private void initializeLogger(Logger logger) {
        console_handler.setLevel(Level.INFO);
        logger.setLevel(Level.ALL);
        logger.addHandler(console_handler);
        logger.setUseParentHandlers(false);
    }

    static private boolean processLoggingOptions(GeneralOptions options, Logger logger) {

        if (options.isLogFile()) {
            Level level;
            level = intToLevel(options.getLogLevel());
            if (level == null) {
                logger.severe("Bad logging level: " + options.getLogLevel());
                return false;
            }
            String filename = options.getLogFile();
            Handler filehandler;
            try {
                filehandler = new FileLogger(filename);
            } catch (IOException ex) {
                logger.severe("Could not log to file '" + filename + "' : " + ex.getMessage());
                return false;
            }
            filehandler.setLevel(level);
            logger.addHandler(filehandler);
        }

        if (options.isConsoleLogLevel()) {
            Level level = intToLevel(options.getConsoleLogLevel());
            if (level == null) {
                logger.severe("Bad logging level: " + options.getConsoleLogLevel());
                return false;
            }
            console_handler.setLevel(level);
        }

        return true;
    }

    static public UpdateStreamOptions processUpdateStreamOptions(String[] args, Logger logger) {

        initializeLogger(logger);

        UpdateStreamOptions options = null;

        try {

            options = CliFactory.parseArguments(UpdateStreamOptions.class, args);

            if (options == null
                    || false == processLoggingOptions(options, logger)) {
                System.err.println();
                System.err.println(CliFactory.createCli(UpdateStreamOptions.class).getHelpMessage());
                System.err.println();
                return null;
            }

        } catch (ArgumentValidationException ex) {
            System.err.println();
            System.err.println(ex.getMessage());
            if (!ex.getMessage().startsWith("Usage:")) {
                System.err.println();
                System.err.println(CliFactory.createCli(UpdateStreamOptions.class).getHelpMessage());
            }
            System.err.println();
            return null;
        } catch (Exception e) {
            ToolBox.printExceptionInfo(e, logger);
            return null;
        }

        return options;
    }

    static public DeletePidsOptions processDeletePidsOptions(String[] args, Logger logger) {

        initializeLogger(logger);

        DeletePidsOptions options = null;

        try {

            options = CliFactory.parseArguments(DeletePidsOptions.class, args);

            if (options == null
                    || false == processLoggingOptions(options, logger)) {
                System.err.println();
                System.err.println(CliFactory.createCli(DeletePidsOptions.class).getHelpMessage());
                System.err.println();
                return null;
            }

        } catch (ArgumentValidationException ex) {
            System.err.println();
            System.err.println(ex.getMessage());
            if (!ex.getMessage().startsWith("Usage:")) {
                System.err.println(CliFactory.createCli(DeletePidsOptions.class).getHelpMessage());
            }
            System.err.println();
            return null;
        } catch (Exception e) {
            ToolBox.printExceptionInfo(e, logger);
            return null;
        }

        return options;
    }

    static public UpdateInfoOptions processUpdateInfoOptions(String[] args, Logger logger) {

        initializeLogger(logger);

        UpdateInfoOptions options = null;

        try {

            options = CliFactory.parseArguments(UpdateInfoOptions.class, args);

            if (options == null
                    || false == processLoggingOptions(options, logger)) {
                System.err.println();
                System.err.println(CliFactory.createCli(UpdateInfoOptions.class).getHelpMessage());
                System.err.println();
                return null;
            }

        } catch (ArgumentValidationException ex) {
            System.err.println();
            System.err.println(ex.getMessage());
            if (!ex.getMessage().startsWith("Usage:")) {
                System.err.println(CliFactory.createCli(UpdateInfoOptions.class).getHelpMessage());
            }
            System.err.println();
            return null;
        } catch (Exception e) {
            ToolBox.printExceptionInfo(e, logger);
            return null;
        }

        return options;
    }

    public static Level intToLevel(int i) {
        Level level = null;
        switch (i) {
            case 0:
                level = Level.OFF;
                break;
            case 1:
                level = Level.SEVERE;
                break;
            case 2:
                level = Level.WARNING;
                break;
            case 3:
                level = Level.INFO;
                break;
            case 4:
                level = Level.ALL;
                break;
        }
        return level;
    }
}
