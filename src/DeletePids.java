
import be.libis.DeletePidsOptions;
import be.libis.GeneralOptionsManager;
import be.libis.digitool.ToolBox;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author kris
 */
public class DeletePids {

    private static final Logger logger = Logger.getLogger(DeletePids.class.getName());

    public static void main(String[] args) {

        DeletePidsOptions options =
                new GeneralOptionsManager<DeletePidsOptions>().processOptions(
                DeletePidsOptions.class, args, logger);

        if (options == null) {
            return; // error messages are printed by GeneralOptionsHandler
        }

        try {

            ToolBox.INSTANCE.setLogger(logger);

            if (!options.isInputFile() && options.getPIDs() == null) {
                System.err.println();
                System.err.println("Either --inputFile or a list of PIDs needs to be supplied.");
                System.err.println(CliFactory.createCli(DeletePidsOptions.class).getHelpMessage());
                System.err.println();
                return;
            }

            if (options.isInputFile()) {
                File inputFile = options.getInputFile();

                try {
                    FileInputStream fstream = new FileInputStream(inputFile);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String pid;
                    String empty = "";
                    while ((pid = br.readLine()) != null) {
                        if (empty.equals(pid)) {
                            continue;
                        }
                        deletePid(ToolBox.INSTANCE, pid);
                    }
                    in.close();

                } catch (Exception ex) {
                    logger.severe("Could not open and read input file '"
                            + inputFile.getPath() + "' : " + ex.getMessage());
                }
            }

            List<String> pids = options.getPIDs();

            if (pids != null) {
                for (String pid : pids) {
                    deletePid(ToolBox.INSTANCE, pid);
                }
            }

        } catch (Exception e) {
            ToolBox.printExceptionInfo(e, logger);
            return;
        }

    }

    static void deletePid(ToolBox tb, String pid) {
        if (pid == null || pid.equals("")) {
            return;
        }
        logger.info("Deleting object with PID: '" + pid + "'");
        if (tb.deleteDigitalEntity(pid.trim())) {
            logger.info("SUCCESS: Deleted object '" + pid + "'");
        } else {
            logger.info("FAILURE: Could not delete object '" + pid + "'");
        }

    }
}
