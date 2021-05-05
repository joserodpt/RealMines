package josegamerpt.realmines.config;

import josegamerpt.realmines.RealMines;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class MineResetTasks implements Listener {

	private static File file;
	private static FileConfiguration customFile;
	private static String name = "mineresettasks.yml";

	public static void setup(Plugin p) {
		file = new File(p.getDataFolder(), name);

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException ignored) {
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
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "[RealMines] Couldn't save " + name + "!");
		}
	}

	public static void reload() {
		customFile = YamlConfiguration.loadConfiguration(file);
	}

}