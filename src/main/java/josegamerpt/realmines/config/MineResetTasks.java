package josegamerpt.realmines.config;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class MineResetTasks implements Listener {

    private static final String name = "mineresettasks.yml";
    private static File file;
    private static FileConfiguration customFile;

    public static void setup(final Plugin p) {
        file = new File(p.getDataFolder(), name);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (final IOException ignored) {
            }
        }
        customFile = YamlConfiguration.loadConfiguration(file);

        MineResetTasks.save();
    }

    public static FileConfiguration file() {
        return customFile;
    }

    public static void save() {
        try {
            customFile.save(file);
        } catch (final IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[RealMines] Couldn't save " + name + "!");
        }
    }

    public static void reload() {
        customFile = YamlConfiguration.loadConfiguration(file);
    }

}