package josegamerpt.realmines.config;

import josegamerpt.realmines.RealMines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public class Configer {

    private final static int latest = 2;
    private static String errors;

    public static int getConfigVersion() {
        if (Config.file().getInt("Version") == 0) {
            return 1;
        } else {
            return Config.file().getInt("Version");
        }
    }

    public static void updateConfig() {
        while (getConfigVersion() != latest) {
            int newconfig = 0;
            switch (getConfigVersion()) {
                case 1:
                    //update to 2
                    newconfig = 2;
                    Config.file().set("Version", newconfig);
                    Config.file().set("RealMines.resetAnnouncement", null);
                    Config.file().set("RealMines.announceTimes", Arrays.asList("30", "20", "10", "5", "4", "3", "2", "1"));
                    Config.save();
                    break;
            }
            RealMines.log(Level.INFO, "Config file updated to version " + newconfig + ".");
        }
        if (getConfigVersion() == latest) {
            RealMines.log(Level.INFO, "Your config file is updated to the latest version.");
        }
    }

    public static String getErrors() {
        return errors;
    }

    public static boolean checkForErrors() {
        ArrayList<String> errs = new ArrayList<>();
        if (!Config.file().contains("RealMines.Prefix")) {
            errs.add("Missing RealMines Prefix on config.yml | Please look at the original config.yml");
        }

        errors = String.join(", ", errs);
        return errs.size() > 0;
    }
}