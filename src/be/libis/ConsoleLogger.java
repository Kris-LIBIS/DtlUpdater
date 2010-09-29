/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.libis;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author KrisD
 */
public class ConsoleLogger extends ConsoleHandler {

    public ConsoleLogger() {
    }

    @Override
    public void publish(LogRecord record) {
        int level = record.getLevel().intValue();
        if (level < getLevel().intValue()) {
            return;
        }
        String levelTxt = level < Level.INFO.intValue()
                ? "DEBUG"
                : level <= Level.INFO.intValue()
                ? "INFO"
                : level <= Level.WARNING.intValue()
                ? "WARNING"
                : "ERROR";

        System.err.printf("%7s - %s\n", levelTxt, record.getMessage());
        flush();
    }

    @Override
    public void flush() {
        super.flush();
    }

    @Override
    public void close() {
        super.close();
    }
}
