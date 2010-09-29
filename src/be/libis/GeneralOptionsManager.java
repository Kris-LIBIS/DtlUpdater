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
public class GeneralOptionsManager<T> {

    private Handler console_handler = new ConsoleLogger();

    private void initializeLogger(Logger logger) {
        console_handler.setLevel(Level.INFO);
        logger.setLevel(Level.ALL);
        logger.addHandler(console_handler);
        logger.setUseParentHandlers(false);
    }

    private boolean processLoggingOptions(GeneralOptions options, Logger logger) {

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

    public T processOptions(Class<T> c, String[] args, Logger logger) {

        initializeLogger(logger);

        T options = null;

        try {

            options = CliFactory.parseArguments(c, args);

            if (options == null
                    || false == processLoggingOptions((GeneralOptions) options, logger)) {
                System.err.println();
                System.err.println(CliFactory.createCli(c).getHelpMessage());
                System.err.println();
                return null;
            }

        } catch (ArgumentValidationException ex) {
            System.err.println();
            System.err.println(ex.getMessage());
            if (!ex.getMessage().startsWith("Usage:")) {
                System.err.println();
                System.err.println(CliFactory.createCli(c).getHelpMessage());
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
