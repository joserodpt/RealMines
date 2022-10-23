package josegamerpt.realmines.config;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public class LanguageConfiger {

    private final static int latest = 2;
    private static String errors;

    public static int getConfigVersion() {
        if (Language.file().getInt("Version") == 0) {
            return 1;
        } else {
            return Language.file().getInt("Version");
        }
    }

    public static void updateConfig() {
        while (getConfigVersion() != latest) {
            int newConfig = 0;
            if (getConfigVersion() == 1) {
                //update to 2
                newConfig = 2;
                Language.file().set("GUI.Choose-Name", "&rChoose mine type for: %mine%");
                Language.file().set("GUI.Select-Icon-Name", "&rSelect icon for %mine%");
                Language.file().set("GUI.Pick-New-Block-Name", "&rPick a new block");
                Language.file().set("GUI.Faces.Name", "&3&l%face%");
                Language.file().set("GUI.Faces.Selected-Description", Arrays.asList("&7Selected Block: &f%material%", "&7Press &fQ &7to &cdelete."));
                Language.file().set("GUI.Faces.None-Description", Arrays.asList("&7Selected Block: &fNone", "&7Press &fQ &7to &cdelete."));
                Language.file().set("GUI.Resets.Percentage-On.Name", "&9Reset By Percentage &7(&a&lON&r&7)");
                Language.file().set("GUI.Resets.Percentage-On.Description", Arrays.asList("&7Left click to turn &cOFF", "&fRight Click to input a new percentage.", "&fCurrent Value: &b%value%%"));
                Language.file().set("GUI.Resets.Percentage-Off.Name", "&9Reset By Percentage &7(&c&lOFF&r&7)");
                Language.file().set("GUI.Resets.Percentage-Off.Description", Arrays.asList("&7Left click to turn &aON", "&fRight Click to input a new percentage.", "&fCurrent Value: &b%value%%"));
                Language.file().set("GUI.Resets.Time-On.Name", "&9Reset By Time &7(&a&lON&r&7)");
                Language.file().set("GUI.Resets.Time-On.Description", Arrays.asList("&7Left click to turn &cOFF", "&fRight Click to input a new percentage.", "&fCurrent Value: &b%value%%"));
                Language.file().set("GUI.Resets.Time-Off.Name", "&9Reset By Time &7(&c&lOFF&r&7)");
                Language.file().set("GUI.Resets.Time-Off.Description", Arrays.asList("&7Left click to turn &aON", "&fRight Click to input a new percentage.", "&fCurrent Value: &b%value%%"));
                Language.file().set("GUI.Items.Schematic.Name", "&b&lSchematic");
                Language.file().set("GUI.Items.Go-Back.Name", "&9Go Back");
                Language.file().set("GUI.Items.Go-Back.Description", Arrays.asList("&7Click here to go back."));
                Language.file().set("GUI.Items.Pick.Name", "&3&l%material%");
                Language.file().set("GUI.Items.Pick.Description", Arrays.asList("&fClick to pick this."));
                Language.file().set("GUI.Items.Colors.Red", "&c&lRED");
                Language.file().set("GUI.Items.Colors.Green", "&2&lGREEN");
                Language.file().set("GUI.Items.Colors.Blue", "&9&lBLUE");
                Language.file().set("GUI.Items.Colors.Brown", "&4&lBROWN");
                Language.file().set("GUI.Items.Colors.Gray", "&7&lGRAY");
                Language.file().set("GUI.Items.Colors.White", "&f&lWHITE");
                Language.file().set("GUI.Items.Colors.Orange", "&6&lORANGE");
                Language.file().set("GUI.Items.Colors.Yellow", "&e&lYELLOW");
                Language.file().set("GUI.Items.Colors.Purple", "&d&lPURPLE");
                Language.file().set("GUI.Items.Colors.Description", Arrays.asList("&fClick to set this mine color."));
                Language.file().set("System.Error-Permission", "&fYou &cdo not &fhave permission to execute this command!");
                Language.file().set("System.Error-Command", "&cThe command you're trying to run doesn't exist!");
                Language.file().set("System.Error-Usage", "&cWrong usage for the command!");
                Language.file().set("System.Nothing-Found", "&fNothing found for your results.");
                Language.file().set("System.Invalid-Schematic", "&cThe specified schematic file does not exist. Did you write the extension format? (.schem, etc)");
                Language.file().set("System.Input-Schematic", "&fInput in chat the schematic file name in RealMines/schematics folder (Example: Schem1.schem)");
                Language.file().set("System.Input-Schematic-Warn", "&eWARNING &fDont forget to select the schematic region with WorldEdit and then do %action%");
                Language.file().set("System.Input-Parse", "&cInput a percentage from 0 to 100.");
                Language.file().set("System.Input-Limit-Error", "&cWrong input. Please input a percentage greater than 1 and lower or equal to 100");
                Language.file().set("System.Input-Percentage-Error", "&cInput a percentage from 0 to 1.");
                Language.file().set("System.Input-Percentage-Error-Greater", "&cWrong input. Please input a percentage greater than 0.");
                Language.file().set("System.Input-Percentage-Error-Lower", "&cWrong input. Please input a percentage lower or equal than 100.");
                Language.file().set("System.Input-Limit-Error-Greater", "&cWrong input. Please input a new time greater than 1");
                Language.file().set("System.Input-Seconds", "&cInput a new time in seconds.");
                Language.file().set("System.Percentage-Modified", "&fPercentage modified to &b%value%");
                Language.file().set("System.Time-Modified", "&fTime modified to &b%value% seconds.");
                Language.file().set("System.Type-Input", Arrays.asList("&l&9Type in chat your input", "&fType &4cancel &fto cancel"));
                Language.file().set("Signs.Setting-Not-Found", "&4Setting|&4not|&4found");
                Language.file().set("Signs.Mine-Not-Found", "&4Mine&4|&4not|&4found");
                Language.file().set("Version", newConfig);
                Language.save();
                Bukkit.getLogger().log(Level.INFO, "Language file updated to version " + newConfig + ".");
            }
        }
        if (getConfigVersion() == latest) {
            Bukkit.getLogger().log(Level.INFO, "Your language file is updated to the latest version.");
        }
    }

    public static String getErrors() {
        return errors;
    }

    public static boolean checkForErrors() {
        ArrayList<String> errs = new ArrayList<>();
        if (!Language.file().contains("Version")) {
            errs.add("Missing Version in language.yml | Please restart server to apply changes");
            Language.file().set("Version", 1);
        }
        errors = String.join(", ", errs);
        return errs.size() > 0;
    }
}
