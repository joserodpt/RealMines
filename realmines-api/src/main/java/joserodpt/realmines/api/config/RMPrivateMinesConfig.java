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
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Global settings for private mines. Lives in private-mines/config.yml, next to the templates and the
 * per-owner folders, so everything the feature owns sits under one folder.
 */
public class RMPrivateMinesConfig {

    public static final String FOLDER = "private-mines";
    public static final String TEMPLATES_FOLDER = "templates";

    private static final String resource = FOLDER + "/config.yml";
    private static YamlConfig config;

    public static void setup(final JavaPlugin rm) {
        config = YamlConfig.of(rm, new File(getFolder(rm), "config.yml"), resource).versioned("Version").load();
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
        return config == null ? null : config.file();
    }

    public static void save() {
        config.save();
    }

    public static void reload() {
        config.reload();
    }
}
