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
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class RMMinesOldConfig implements Listener {

    private static final String name = "mines.yml";
    private static YamlDocument configFile;
    private static boolean fileExists;

    public static void setup(final JavaPlugin rm) {
        try {
            File file = new File(rm.getDataFolder(), name);
            fileExists = file.exists();
            if (fileExists) {
                configFile = YamlDocument.create(file,
                        GeneralSettings.DEFAULT,
                        LoaderSettings.builder().setMaxCollectionAliases(200).setAutoUpdate(true).build(),
                        DumperSettings.DEFAULT,
                        UpdaterSettings.builder().setVersioning(new BasicVersioning("Version")).build());

                //if it doesn't exist, create a minesBACKUP.yml file that is a copy of the mines.yml file
                File backupFile = new File(rm.getDataFolder(), "minesBACKUP.yml");
                if (!backupFile.exists()) {
                    configFile.save(backupFile);
                }
            }
        } catch (final IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Couldn't setup " + name + "!");
        }
    }

    public static YamlDocument file() {
        return configFile;
    }

    public static boolean fileExists() {
        return fileExists;
    }

    public static void save() {
        try {
            configFile.save();
        } catch (final IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Couldn't save " + name + "!");
        }
    }

    public static void delete() {
        if (configFile.getFile().delete()) {
            fileExists = false;
        }
        configFile = null;
    }
}