
import be.libis.GeneralOptionsHandler;
import be.libis.UpdateInfoOptions;
import be.libis.digitool.ToolBox;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author KrisD
 */
public class UpdateInfo {

    static class UpdateAction {

        static class UpdateActionDetail {

            public String tag;
            public String action;
            public String regex = "";
            public String value = "";
        }
        public String usage_type;
        public ArrayList<UpdateActionDetail> details;
    }
    private static ToolBox toolbox = new ToolBox();
    private static final Logger logger = Logger.getLogger(UpdateStream.class.getName());
    private static ArrayList<UpdateAction> updateActions = new ArrayList<UpdateAction>();
    private static Set<String> validTags = new HashSet<String> ();
    private static Set<String> validActions = new HashSet<String> ();


    public static void main(String[] args) {

        validTags.add("label");
        validTags.add("note");
        validTags.add("ingest_id");
        validTags.add("ingest_name");
        validTags.add("entity_type");
        validTags.add("entity_group");
        validTags.add("usage_type");
        validTags.add("preservation_level");
        validTags.add("partition_a");
        validTags.add("partition_b");
        validTags.add("partition_c");
        validTags.add("status");
//        validTags.add("creation_date");
//        validTags.add("creator");
//        validTags.add("modification_date");
//        validTags.add("modified_by");
//        validTags.add("admin_unit");

        validActions.add("append");
        validActions.add("replace");
//        validActions.add("search_and_replace_all");
//        validActions.add("search_and_replace_first");
        validActions.add("delete");

        UpdateInfoOptions options =
                GeneralOptionsHandler.processUpdateInfoOptions(args, logger);

        if (options == null) {
            return; // error messages are printed by GeneralOptionsHandler
        }

        try {

            toolbox.setLogger(logger);

            {
                File updateActionFile = options.getUpdateActionFile();
                FileInputStream fstream = new FileInputStream(updateActionFile);
                Yaml yaml = new Yaml();
                for (Object data : yaml.loadAll(fstream)) {
                    UpdateAction action = new UpdateAction();
                    action.details = new ArrayList<UpdateAction.UpdateActionDetail>();
                    Map map = (Map) data;
                    action.usage_type = (String) map.get("usage_type");
                    @SuppressWarnings(value = "unchecked")
                    List<Map> operations = (List<Map>) map.get("operations");
                    for (Map operation : operations) {
                        UpdateAction.UpdateActionDetail action_detail =
                                new UpdateAction.UpdateActionDetail();
                        action_detail.tag = (String) operation.get("tag");
                        if (!validTags.contains(action_detail.tag) ) {
                            continue;
                        }
                        action_detail.action = (String) operation.get("action");
                        if (!validActions.contains(action_detail.action) ) {
                            continue;
                        }
                        if (!action_detail.action.equals("delete")) {
                            action_detail.value = (String) operation.get("value");
                        }
                        if (action_detail.action.equals("search_and_replace_all") ||
                                action_detail.action.equals("search_and_replace_first")) {
                            action_detail.regex = (String) operation.get("regex");
                        }

                        action.details.add(action_detail);
                    }
                    updateActions.add(action);
                }
                fstream.close();
            }

            if (!options.isInputFile() && options.getPIDs() == null) {
                System.err.println();
                System.err.println("Either --inputFile or a list of PIDs needs to be supplied.");
                System.err.println(CliFactory.createCli(UpdateInfoOptions.class).getHelpMessage());
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
                        updateInfo(pid);
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
                    updateInfo(pid);
                }
            }

        } catch (Exception e) {
            ToolBox.printExceptionInfo(e, logger);
            return;
        }

    }

    static void updateInfo(String master_pid) {
        if (master_pid == null || master_pid.equals("")) {
            return;
        }

        Set<String> failedPids = new HashSet<String>();

        // get all the manifestation pids
        Map<String, List<String>> pidMap = new HashMap<String, List<String>>();
        Set<String> pidSet = new HashSet<String>();
        for (UpdateAction action : updateActions) {
            logger.info("Getting manifestation '" + action.usage_type
                    + "' for PID '" + master_pid + "'");

            List<String> pidList =
                    toolbox.getManifestationPids(master_pid, action.usage_type);
            pidMap.put(action.usage_type, pidList);
            pidSet.addAll(pidList);
            logger.info("Found: " + pidList);
        }

        logger.finest("PID map: " + pidMap.toString());
        logger.finest("PID set: " + pidSet.toString());

        if (pidSet.isEmpty()) {
            logger.severe("FAILED. No PIDs found to update.");
            return;
        }

        // get all the digital entities for the collected pids
        Map<String, String> deMap = new HashMap<String, String>();
        for (String pid : pidSet) {
            String digital_entity = toolbox.retrieveObject(pid);
            if (digital_entity == null) {
                logger.severe("Failed to retrieve digital entity info for " + pid);
                failedPids.add(pid);
                continue;
            }
            deMap.put(pid, digital_entity);
        }

        if (deMap.isEmpty()) {
            logger.severe("FAILED. No digital entities to update.");
            return;
        }

        logger.finest("Digital Entity map : ");
        for (Entry<String, String> entry : deMap.entrySet()) {
            logger.finest(" - " + entry.getKey() + " => \n" + entry.getValue());
        }

        // transform the digital entities
        for (UpdateAction action : updateActions) {
            for (UpdateAction.UpdateActionDetail detail : action.details) {
                ToolBox.TransformationParameter[] parameters = {
                    new ToolBox.TransformationParameter("tag", detail.tag),
                    new ToolBox.TransformationParameter("action", detail.action),
                    new ToolBox.TransformationParameter("value", detail.value)
                };
                for (String pid : pidMap.get(action.usage_type)) {
                    String digital_entity = deMap.get(pid);
                    if (digital_entity == null) {
                        continue;
                    }
                    logger.info("Changing " + pid + "info: "
                            + " '" + detail.tag + "'"
                            + " " + detail.action
                            + " '" + detail.value + "'");
                    digital_entity = toolbox.transformXml(
                            digital_entity, ToolBox.updateInfoXsl, parameters);
                    deMap.put(pid, digital_entity);
                }
            }
        }

        logger.finest("Updated Digital Entity map : ");
        for (Entry<String, String> entry : deMap.entrySet()) {
            logger.finest(" - " + entry.getKey() + " => \n" + entry.getValue());
        }

        if (deMap.isEmpty()) {
            logger.severe("FAILED. No digital entities to update.");
            return;
        }

        // update the digital entities
        logger.info("Updating repository ...");
        for (Entry<String, String> entry : deMap.entrySet()) {
            if (!toolbox.updateDigitalEntity(entry.getValue())) {
                failedPids.add(entry.getKey());
            }
        }

        String updatedPids = "";
        for (String pid : deMap.keySet()) {
            if (!failedPids.contains(pid)) {
                updatedPids += " " + pid;
            }
        }

        if (!updatedPids.equals("")) {
            logger.info("Updated manifestation pids" + updatedPids
                    + " for master PID " + master_pid);
        }

        if (failedPids.size() == 0) {
            logger.info("SUCCESS.");
        } else {
            String message = "";
            for (String pid : failedPids) {
                message += " " + pid;
            }
            logger.severe("FAILED. Failed to update manifestation PIDs"
                    + message + " for master PID " + master_pid);
        }
    }
}

