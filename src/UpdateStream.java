/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import be.libis.GeneralOptionsHandler;
import be.libis.UpdateStreamOptions;
import be.libis.digitool.ToolBox;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Logger;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

/**
 *
 * @author kris
 */
public class UpdateStream {

    private static final Logger logger = Logger.getLogger(UpdateStream.class.getName());
    private static boolean delete_old_pid = false;
    private static boolean allow_multiple_hits = false;
    private static PrintStream pid_dump = null;
    private static PrintStream pid_dump_old = null;
    private static PrintStream pid_dump_new = null;

    @Override
    protected void finalize() throws IOException {
        if (pid_dump != null) {
            pid_dump.flush();
            pid_dump.close();
            pid_dump = null;
        }
        if (pid_dump_old != null) {
            pid_dump_old.flush();
            pid_dump_old.close();
            pid_dump_old = null;
        }
        if (pid_dump_new != null) {
            pid_dump_new.flush();
            pid_dump_new.close();
            pid_dump_new = null;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        UpdateStreamOptions options =
                GeneralOptionsHandler.processUpdateStreamOptions(args, logger);

        if (options == null) {
            return; // error messages are printed by GeneralOptionsHandler
        }

        try {

            if (!options.isInputFile() && options.getFiles() == null) {
                System.err.println();
                System.err.println("Either --inputFile or a list of files needs to be supplied.");
                System.err.println(CliFactory.createCli(UpdateStreamOptions.class).getHelpMessage());
                System.err.println();
                return;
            }

            delete_old_pid = options.isDeleteOldPid();
            allow_multiple_hits = options.isMultiplePids();

            if (options.isPidDumpFile()) {
                File pid_dump_file = options.getPidDumpFile();
                File pid_dump_old_file = new File(pid_dump_file.getAbsolutePath() + ".old");
                File pid_dump_new_file = new File(pid_dump_file.getAbsolutePath() + ".new");
                pid_dump = new PrintStream(new FileOutputStream(pid_dump_file, true));
                pid_dump_old = new PrintStream(new FileOutputStream(pid_dump_old_file, true));
                pid_dump_new = new PrintStream(new FileOutputStream(pid_dump_new_file, true));
            }

            if (options.isInputFile()) {
                File inputFile = options.getInputFile();

                try {
                    FileInputStream fstream = new FileInputStream(inputFile);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        updateStreamWorker(strLine);
                    }
                    in.close();

                } catch (Exception ex) {
                    logger.severe("Could not open and read input file '"
                            + inputFile.getPath() + "' : " + ex.getMessage());
                }

            }

            List<String> files = options.getFiles();

            if (files != null) {
                for (String filename : files) {
                    updateStreamWorker(filename);
                }
            }

        } catch (Exception e) {
            ToolBox.printExceptionInfo(e, logger);
        }
        return;
    }

    private static void updateStreamWorker(String file_usage_pair) {
        String filename = file_usage_pair;
        String usageType = "";
        int i = filename.indexOf(',');
        if (i > 0) {
            String fileName = filename.substring(0, i).trim();
            usageType = filename.substring(i + 1).trim();
            filename = fileName;
        }
        updateStreamWorker(filename, usageType);

    }

    private static void updateStreamWorker(String file_name, String usage_type) {

        ToolBox tb = new ToolBox();
        tb.setLogger(logger);

        boolean success = false;

        // Derive the label from the file name
        String fileName = new File(file_name).getPath();

        String label = ToolBox.fileToLabel(fileName);

        String old_pid = "";

        String new_pid = "";

        // default usage type: (note the different query string)
        String usageType = usage_type.equals("")
                ? "VIEW" : usage_type.toUpperCase();

        do {

            String[] pids = tb.getPid(label, usageType);

            if (pids == null || pids.length <= 0) {
                logger.severe("Could not find any object with label: '" + label
                        + "' and usage_type: '" + usageType + "'");
                break;
            }

            if (pids.length > 1) {
                String message = "More than one objects found for"
                        + " label: '" + label
                        + "' usage_type: '" + usageType
                        + "' found:";

                for (int i = 0; i < pids.length; i++) {
                    message += " " + pids[i];
                }

                if (allow_multiple_hits) {

                    logger.warning(message);

                } else {

                    logger.severe(message);
                    return;
                }
            }

            old_pid = pids[pids.length - 1];

            if (pid_dump_old != null) {
                pid_dump_old.println(old_pid);
            }

            logger.info("Replacing pid " + old_pid + " with new stream '" + fileName + "'");

            ToolBox.DENewParameters new_params = new ToolBox.DENewParameters(
                    fileName, usageType, old_pid);

            new_pid = tb.addDigitalEntity(new_params);

            if (new_pid == null || new_pid.equals("")) {
                break;
            }

            if (pid_dump_new != null) {
                pid_dump_new.println(new_pid);
            }

            ToolBox.DECopyParameters copy_params =
                    new ToolBox.DECopyParameters(old_pid, new_pid);
            copy_params.copyControl = true;
            copy_params.copyMetadata = true;
            copy_params.copyRelations = true;

            success = tb.copyDigitalEntityInfo(copy_params);
            if (!success) {
                break;
            }

            if (delete_old_pid) {
                success = tb.deleteDigitalEntity(old_pid);
            }

        } while (false);

        if (success) {
            logger.info("SUCCESS: UpdateStream '" + file_name + "' '" + usageType
                    + "' : old object: " + old_pid + " new object: " + new_pid);
            if (pid_dump != null) {
                pid_dump.println(old_pid + " -> " + new_pid);
            }
        } else {
            logger.info("FAILURE: UpdateStream of '" + file_name
                    + "' & '" + usageType + "' did not succeed");
        }

    }
}
