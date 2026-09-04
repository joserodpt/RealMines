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
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Global settings for private mines. Lives in private-mines/config.yml, next to the templates and the
 * per-owner folders, so everything the feature owns sits under one folder.
 */
public class RMPrivateMinesConfig implements Listener {

    public static final String FOLDER = "private-mines";
    public static final String TEMPLATES_FOLDER = "templates";

    private static final String resource = FOLDER + "/config.yml";
    private static YamlDocument configFile;

    public static void setup(final JavaPlugin rm) {
        try {
            configFile = YamlDocument.create(new File(getFolder(rm), "config.yml"), rm.getResource(resource),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("Version")).build());
        } catch (final IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Couldn't setup " + resource + "!");
        }
    }

    /**
     * plugins/RealMines/private-mines/
     */
    public static File getFolder(final JavaPlugin rm) {
        final File folder = new File(rm.getDataFolder(), FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    /**
     * plugins/RealMines/private-mines/templates/
     */
    public static File getTemplatesFolder(final JavaPlugin rm) {
        final File folder = new File(getFolder(rm), TEMPLATES_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    /**
     * plugins/RealMines/private-mines/&lt;owner uuid&gt;/
     */
    public static File getOwnerFolder(final JavaPlugin rm, final java.util.UUID owner) {
        return new File(getFolder(rm), owner.toString());
    }

    public static YamlDocument file() {
        return configFile;
    }

    public static void save() {
        try {
            configFile.save();
        } catch (final IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Couldn't save " + resource + "!");
        }
    }

    public static void reload() {
        try {
            configFile.reload();
        } catch (final IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Couldn't reload " + resource + "!");
        }
    }
}
