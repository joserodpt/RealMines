package joserodpt.realmines.api.config;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2026
 * @link https://github.com/joserodpt/RealMines
 */

import dev.dejvokep.boostedyaml.YamlDocument;
import joserodpt.realutils.config.YamlConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class RMMinesOldConfig {

    private static final String name = "mines.yml";
    private static YamlConfig config;
    private static boolean fileExists;

    public static void setup(final JavaPlugin rm) {
        final File file = new File(rm.getDataFolder(), name);
        fileExists = file.exists();
        if (fileExists) {
            //no bundled defaults: this is the pre-1.7 file, only ever read to be converted
            config = YamlConfig.of(rm, file, null).versioned("Version").maxCollectionAliases(200).load();

            //if it doesn't exist, create a minesBACKUP.yml file that is a copy of the mines.yml file
            final File backupFile = new File(rm.getDataFolder(), "minesBACKUP.yml");
            if (config.file() != null && !backupFile.exists()) {
                try {
                    config.file().save(backupFile);
                } catch (final IOException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Couldn't back up " + name + "!");
                }
            }
        }
    }

    public static YamlDocument file() {
        return config == null ? null : config.file();
    }

    public static boolean fileExists() {
        return fileExists;
    }

    public static void save() {
        config.save();
    }

    public static void delete() {
        if (config.file().getFile().delete()) {
            fileExists = false;
        }
        config = null;
    }
}