package josegamerpt.realmines.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Mines implements Listener {

    private static final String name = "mines.yml";
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

        Mines.save();
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