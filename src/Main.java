
import be.libis.DeletePidsOptions;
import be.libis.UpdateInfoOptions;
import be.libis.UpdateStreamOptions;
import java.io.PrintStream;
import java.util.Properties;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author KrisD
 */
public class Main {
    
    static Properties prop = System.getProperties();

    public static void main(String[] args) {
        PrintStream out = System.err;

        String class_path = prop.getProperty("java.class.path", null);

        out.println("\nUsage: java -cp " + class_path + " <command> ...");
        out.println("\ncommand: UpdateStream  - Update file stream(s) in the DigiTool Repository");
        out.println(CliFactory.createCli(UpdateStreamOptions.class).getHelpMessage());
        out.println("\ncommand: DeletePids    - Delete object(s) from the DigiTool repository");
        out.println(CliFactory.createCli(DeletePidsOptions.class).getHelpMessage());
        out.println("\ncommand: UpdateInfo    - Update object info in the DigiTool repository");
        out.println(CliFactory.createCli(UpdateInfoOptions.class).getHelpMessage());
        out.println();
    }

}
