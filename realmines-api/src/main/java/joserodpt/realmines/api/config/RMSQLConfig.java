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

/**
 * Holds sql.yml, the database connection settings.
 * <p>
 * Note: this file is intentionally <b>not</b> reloadable. Swapping the connection settings of a live
 * database out from under the running caches is not something this plugin supports - change the file
 * and restart the server.
 */
public class RMSQLConfig {

    private static final String name = "sql.yml";
    private static YamlConfig config;

    public static void setup(final JavaPlugin rm) {
        config = YamlConfig.of(rm, name).versioned("Version").load();
    }

    public static YamlDocument file() {
        return config == null ? null : config.file();
    }

    public static void save() {
        config.save();
    }
}
