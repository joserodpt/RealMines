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
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class RMConfig implements Listener {

    private static final String name = "config.yml";

    /**
     * Where players are sent when the ground goes out from under them - releasing a private mine, or
     * having it expire. Empty means the main world's spawn.
     */
    public static final String DEFAULT_LOCATION = "RealMines.Default-Location";

    private static YamlDocument configFile;

    public static void setup(final JavaPlugin rm) {
        try {
            configFile = YamlDocument.create(new File(rm.getDataFolder(), name), rm.getResource(name),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("Version")).build());
        } catch (final IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Couldn't setup " + name + "!");
        }
    }

    public static YamlDocument file() {
        return configFile;
    }

    /**
     * Somewhere safe to put a player, set with {@code /rm setdefaultlocation}. Falls back to the main
     * world's spawn, which is the one place a server always has.
     *
     * @return the location, or null on a server with no worlds at all
     */
    public static Location getDefaultLocation() {
        final String raw = configFile == null ? null : configFile.getString(DEFAULT_LOCATION, "");
        if (raw != null && !raw.isEmpty()) {
            final String[] parts = raw.split(";");
            //a world that has since been removed falls through to the spawn rather than stranding anybody
            final World world = parts.length == 6 ? Bukkit.getWorld(parts[0]) : null;
            if (world != null) {
                try {
                    return new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]),
                            Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
                } catch (final NumberFormatException ignored) {
                    //a hand-edited value, use the spawn instead
                }
            }
        }

        final List<World> worlds = Bukkit.getWorlds();
        return worlds.isEmpty() ? null : worlds.get(0).getSpawnLocation();
    }

    public static void setDefaultLocation(final Location loc) {
        if (configFile == null || loc == null || loc.getWorld() == null) {
            return;
        }
        configFile.set(DEFAULT_LOCATION, loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";"
                + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch());
        save();
    }

    public static void save() {
        try {
            configFile.save();
        } catch (final IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Couldn't save " + name + "!");
        }
    }

    public static void reload() {
        try {
            configFile.reload();
        } catch (final IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Couldn't reload " + name + "!");
        }
    }
}