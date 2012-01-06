/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package be.libis;

import java.io.File;
import java.util.List;
import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 *
 * @author KrisD
 */
@CommandLineInterface(application="java -jar AddStream.jar")
public interface AddStreamOptions extends GeneralOptions {


    @Option(shortName="f",description="File containing list of <filename>,<usagetype> pairs")
    File getInputFile();
    boolean isInputFile();

    @Option(description="Dump new PID numbers in the file (appends if file exists)")
    File getPidDumpFile();
    boolean isPidDumpFile();

    @Unparsed(name="Files")
    List<String> getFiles();

}
