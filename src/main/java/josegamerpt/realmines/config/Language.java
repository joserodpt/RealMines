package josegamerpt.realmines.config;

import josegamerpt.realmines.RealMines;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Language implements Listener {

	private static File file;
	private static FileConfiguration customFile;
	private static String name = "language.yml";

	public static void setup(Plugin p) {
		file = new File(p.getDataFolder(), name);

		if (!file.exists()) {
			RealMines.getPlugin().saveResource("language.yml", true);
		}
		customFile = YamlConfiguration.loadConfiguration(file);
	}

	public static FileConfiguration file() {
		return customFile;
	}

	public static void save() {
		try {
			customFile.save(file);
		} catch (IOException e) {
			RealMines.log(Level.SEVERE, "Couldn't save " + name + "!");
		}
	}

	public static void reload() {
		customFile = YamlConfiguration.loadConfiguration(file);
	}
}