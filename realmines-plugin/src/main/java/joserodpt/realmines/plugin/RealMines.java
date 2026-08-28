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

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMAchievementsConfig;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.plugin.gui.GUIManager;
import joserodpt.realmines.plugin.managers.AchievementsManager;
import joserodpt.realmines.plugin.managers.DatabaseManager;
import joserodpt.realmines.plugin.managers.MineManager;
import joserodpt.realmines.plugin.managers.MineResetTasksManager;
import net.milkbowl.vault.economy.Economy;

import java.sql.SQLException;
import java.util.logging.Logger;

public class RealMines extends RealMinesAPI {

    private final Logger logger;
    private final RealMinesPlugin plugin;
    private final MineManager mineManager;
    private final MineResetTasksManager mineResetTasksManager;
    private final GUIManager guiManager;
    private final AchievementsManager achievementsManager;
    private DatabaseManager databaseManager;

    public RealMines(RealMinesPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        this.mineManager = new MineManager(this);
        this.mineResetTasksManager = new MineResetTasksManager(this);
        this.guiManager = new GUIManager(this);
        this.achievementsManager = new AchievementsManager(this);
    }

    /**
     * Opens the stats database. A failure here only turns stats off - the rest of the plugin
     * carries on working, which is why every caller has to null check {@link #getDatabaseManager()}.
     */
    public void setupDatabase() {
        try {
            this.databaseManager = new DatabaseManager(this);
        } catch (SQLException | RuntimeException e) {
            this.logger.severe("Couldn't connect to the database, player stats and achievements are disabled: " + e.getMessage());
            this.databaseManager = null;
        }
    }

    @Override
    public RealMinesPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    public GUIManager getGUIManager() {
        return this.guiManager;
    }

    @Override
    public MineManager getMineManager() {
        return this.mineManager;
    }

    @Override
    public MineResetTasksManager getMineResetTasksManager() {
        return this.mineResetTasksManager;
    }

    @Override
    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    /**
     * Whether mined blocks should be counted. Turning this off keeps whatever is already saved.
     */
    public boolean isStatsEnabled() {
        return RMConfig.file().getBoolean("RealMines.Stats.Enabled", true);
    }

    @Override
    public AchievementsManager getAchievementsManager() {
        return this.achievementsManager;
    }

    @Override
    public boolean hasNewUpdate() {
        return plugin.newUpdate;
    }

    @Override
    public void reload() {
        RMConfig.reload();
        RMLanguageConfig.reload();
        RMAchievementsConfig.reload();
        this.achievementsManager.loadAchievements();
        this.mineManager.unloadMines();
        this.mineManager.loadMines();
        this.logger.info("[RealMines] Loaded " + this.mineManager.getMines().size() + " mines and " + this.mineManager.getSigns().size() + " mine signs.");
    }

    @Override
    public Economy getEconomy() {
        return this.plugin.getEconomy();
    }

    @Override
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }
}
