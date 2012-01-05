/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import be.libis.AddChildOptions;
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
public class AddChild {

    private static final Logger logger = Logger.getLogger(AddChild.class.getName());
    private static String pid = "";
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

        AddChildOptions options =
                new GeneralOptionsManager<AddChildOptions>()
                .processOptions(AddChildOptions.class, args, logger);

        if (options == null) {
            return; // error messages are printed by GeneralOptionsHandler
        }

        try {
            pid = options.getPid();

            if (!options.isInputFile() && options.getFiles() == null) {
                System.err.println();
                System.err.println("Either --inputFile or a list of files needs to be supplied.");
                System.err.println(CliFactory.createCli(AddChildOptions.class).getHelpMessage());
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
                        addChildWorker(strLine);
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
                    addChildWorker(filename);
                }
            }

        } catch (Exception e) {
            ToolBox.printExceptionInfo(e, logger);
        }
        return;
    }

    private static void addChildWorker(String file_usage_pair) {
        String filename = file_usage_pair;
        String usageType = "";
        int i = filename.indexOf(',');
        if (i > 0) {
            usageType = filename.substring(i + 1).trim();
            filename = filename.substring(0, i).trim();
        }
        addChildWorker(filename, usageType);

    }

    private static void addChildWorker(String file_name, String usage_type) {

        ToolBox.INSTANCE.setLogger(logger);

        boolean success = false;

        // Derive the label from the file name
        String fileName = new File(file_name).getPath();

        String new_pid = "";

        // default usage type:
        String usageType = usage_type.equals("")
                ? "ARCHIVE" : usage_type.toUpperCase();

        do {

            logger.info("Adding child '" + fileName + "' as '" + usage_type +
                    "' to pid " + pid);

            ToolBox.DENewParameters new_params = new ToolBox.DENewParameters(
                    fileName, usageType, pid);
            new_params.relation_type = "part_of";

            new_pid = ToolBox.INSTANCE.addDigitalEntity(new_params);

            if (new_pid == null || new_pid.equals("")) {
                break;
            }

            if (pid_dump_new != null) {
                pid_dump_new.println(new_pid);
            }
            
            List<String> child_pids = ToolBox.INSTANCE.getChildPids(pid, usageType);

            ToolBox.DECopyParameters copy_params =
                new ToolBox.DECopyParameters(pid, new_pid);
            copy_params.usage_type = usageType;
            copy_params.copyControl = true;
            copy_params.copyMetadata = false;
            copy_params.copyRelations = false;
            copy_params.clear_complex = true;

            if (child_pids.size() < 1) {
              logger.warning("Could not find any other child objects to copy metadata from. CONTROL info will be copied from the parent object.");
            } else {
              copy_params.from_pid = child_pids.get(0);
              copy_params.copyMetadata = true;
              copy_params.clear_complex = false;
            }
            
            success = ToolBox.INSTANCE.copyDigitalEntityInfo(copy_params);
            if (!success) {
                break;
            }

        } while (false);

        if (success) {
            logger.info("SUCCESS: AddChild '" + file_name + "' '" + usageType
                    + "' : parent object: " + pid + " new object: " + new_pid);
            if (pid_dump != null) {
                pid_dump.println(pid + " ++ " + new_pid);
            }
        } else {
            logger.info("FAILURE: AddChild of '" + file_name
                    + "' & '" + usageType + "' did not succeed");
        }

    }
}
