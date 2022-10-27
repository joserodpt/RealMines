package josegamerpt.realmines.config;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Config implements Listener {

	private static YamlDocument configFile;
	private static final String name = "config.yml";

	public static void setup(JavaPlugin rm) {
		try {
			configFile = YamlDocument.create(new File(rm.getDataFolder(), name), rm.getResource(name),
					GeneralSettings.DEFAULT,
					LoaderSettings.builder().setAutoUpdate(true).build(),
					DumperSettings.DEFAULT,
					UpdaterSettings.builder().setVersioning(new BasicVersioning("Version")).build());
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Couldn't setup " + name + "!");
		}
	}

	public static YamlDocument file() {
		return configFile;
	}

	public static void save() {
		try {
			configFile.save();
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Couldn't save " + name + "!");
		}
	}

	public static void reload() {
		try {
			configFile.reload();
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Couldn't reload " + name + "!");
		}
	}
}