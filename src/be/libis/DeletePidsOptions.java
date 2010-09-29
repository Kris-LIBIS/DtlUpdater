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
@CommandLineInterface(application="java -cp DtlUpdate.jar DeletePids")
public interface DeletePidsOptions extends GeneralOptions {

    @Option(description="Input file containing a list of PIDs to delete")
    File getInputFile();
    boolean isInputFile();

    @Unparsed(name="PIDs")
    List<String> getPIDs();

}
