package joserodpt.realmines.plugin;

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

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Consumer;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private final int resourceId;

    public UpdateChecker(final JavaPlugin plugin, final int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                final URLConnection connection = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openConnection();
                //without these a stalled connection holds an async thread for good
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                try (final InputStream inputStream = connection.getInputStream();
                     final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    consumer.accept(reader.readLine());
                }
            } catch (final IOException exception) {
                this.plugin.getLogger().warning("Cannot look for updates: " + exception.getMessage());
            }
        });
    }
}