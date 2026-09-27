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

public class RMLanguageConfig {

    private static YamlConfig config;

    public static void setup(final JavaPlugin rm) {
        config = YamlConfig.of(rm, "language.yml").versioned("Version").load();
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
