/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import be.libis.AddStreamOptions;
import be.libis.GeneralOptionsManager;
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
public class AddStream {

    private static final Logger logger = Logger.getLogger(AddStream.class.getName());
    private static PrintStream pid_dump = null;
    private static PrintStream pid_dump_new = null;

    @Override
    protected void finalize() throws IOException {
        if (pid_dump != null) {
            pid_dump.flush();
            pid_dump.close();
            pid_dump = null;
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

        AddStreamOptions options =
                new GeneralOptionsManager<AddStreamOptions>()
                .processOptions(AddStreamOptions.class, args, logger);

        if (options == null) {
            return; // error messages are printed by GeneralOptionsHandler
        }

        try {

            if (!options.isInputFile() && options.getFiles() == null) {
                System.err.println();
                System.err.println("Either --inputFile or a list of files needs to be supplied.");
                System.err.println(CliFactory.createCli(AddStreamOptions.class).getHelpMessage());
                System.err.println();
                return;
            }

            if (options.isPidDumpFile()) {
                File pid_dump_file = options.getPidDumpFile();
                File pid_dump_new_file = new File(pid_dump_file.getAbsolutePath() + ".new");
                pid_dump = new PrintStream(new FileOutputStream(pid_dump_file, true));
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
                        addStreamWorker(strLine);
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
                    addStreamWorker(filename);
                }
            }

        } catch (Exception e) {
            ToolBox.printExceptionInfo(e, logger);
        }
        return;
    }

    private static void addStreamWorker(String file_usage_pair) {
        String filename = file_usage_pair;
        String usageType = "";
        String referenceType = "";
        int i = filename.indexOf(',');
        if (i > 0) {
            usageType = filename.substring(i + 1).trim();
            referenceType = usageType;
            filename = filename.substring(0, i).trim();
            int j = usageType.indexOf(',');
            if (j > 0) {
                referenceType = usageType.substring(j + 1).trim();
                usageType = usageType.substring(0, j).trim();
            }
        }
        AddStreamWorker(filename, usageType, referenceType);

    }

    private static void AddStreamWorker(String file_name, String usage_type, String reference_type) {

        ToolBox.INSTANCE.setLogger(logger);

        boolean success = false;

        // Derive the label from the file name
        String fileName = new File(file_name).getPath();

        String label = ToolBox.fileToLabel(fileName);

        String old_pid = "";

        String new_pid = "";

        // default usage type:
        String usageType = usage_type.equals("")
                ? "VIEW" : usage_type.toUpperCase();

        String referenceType = reference_type.equals("")
                ? "VIEW" : reference_type.toUpperCase();

        do {

            String[] pids = ToolBox.INSTANCE.getPid(label, referenceType);

            if (pids == null || pids.length <= 0) {
                logger.severe("Could not find any object with label: '" +
                        label + "' and usage type: '" + usageType + "'");
                break;
            }

            if (pids.length > 1) {
                String message = "Found:";

                for (int i = 0; i < pids.length; i++) {
                    message += " " + pids[i];
                }

                    logger.info(message);

            }

            old_pid = pids[0];

            logger.info("Adding stream '" + fileName + "' as '" + usage_type +
                    "' to pid " + old_pid);

            ToolBox.DENewParameters new_params = new ToolBox.DENewParameters(
                    fileName, usageType, old_pid);

            new_pid = ToolBox.INSTANCE.addDigitalEntity(new_params);

            if (new_pid == null || new_pid.equals("")) {
                break;
            }

            if (pid_dump_new != null) {
                pid_dump_new.println(new_pid);
            }

            ToolBox.DECopyParameters copy_params =
                    new ToolBox.DECopyParameters(old_pid, new_pid);
            copy_params.usage_type = usageType;
            copy_params.copyControl = true;
            copy_params.copyMetadata = true;
            copy_params.copyRelations = true;

            success = ToolBox.INSTANCE.copyDigitalEntityInfo(copy_params);
            if (!success) {
                break;
            }

        } while (false);

        if (success) {
            logger.info("SUCCESS: AddStream '" + file_name + "' '" + usageType
                    + "' : existing object: " + old_pid + " new object: " + new_pid);
            if (pid_dump != null) {
                pid_dump.println(old_pid + " ++ " + new_pid);
            }
        } else {
            logger.info("FAILURE: AddStream of '" + file_name
                    + "' & '" + usageType + "' did not succeed");
        }

    }
}
