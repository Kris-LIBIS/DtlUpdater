package be.libis;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author KrisD
 */
public class FileLogger extends FileHandler {

    PrintWriter file;

    public FileLogger(String filename) throws IOException {
        this.file = new PrintWriter(new FileOutputStream(filename));
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        file.close();
    }

    @Override
    public void publish(LogRecord record) {
        int level = record.getLevel().intValue();
        if (level < getLevel().intValue()) {
            return;
        }
        String levelTxt = level < Level.INFO.intValue()
                ? "D"
                : level <= Level.INFO.intValue()
                ? "I"
                : level <= Level.WARNING.intValue()
                ? "W"
                : "E";

        file.printf("%s,%d,[%6$tY-%6$tm-%6$td %6$tH:%6$tM:%6$tS.%6$tL],%s,%s,%s\n",
                levelTxt, record.getSequenceNumber(), record.getMessage(),
                record.getSourceClassName(), record.getSourceMethodName(),
                record.getMillis());
        flush();
    }

    @Override
    public void flush() {
        file.flush();
    }

    @Override
    public void close() throws SecurityException {
        file.close();
    }
}
