/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package be.libis;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 *
 * @author KrisD
 */
public interface GeneralOptions {

    @Option(shortName={"?", "h"},description="Show this help text",helpRequest=true)
    boolean isHelp();

    @Option(description="File logging level (0:none 1:error 2:warning 3:info 4:all - default 3)",defaultValue="3",pattern="[01234]")
    int getLogLevel();
    boolean isLogLevel();

    @Option(description="Print logging to file")
    String getLogFile();
    boolean isLogFile();

    @Option(description="Console logging level (0:none 1:error 2:warning 3:info 4:all - default:3)",defaultValue="3",pattern="[01234]")
    int getConsoleLogLevel();
    boolean isConsoleLogLevel();

    @Option(shortName="S",description="Simulate repository update")
    boolean isSimulateUpdate();

    /*
    @Option(description="Specifies SQLLite database file to log to")
    String getLogDatabase();
    boolean isLogDatabase();
     * 
     */
    
}
