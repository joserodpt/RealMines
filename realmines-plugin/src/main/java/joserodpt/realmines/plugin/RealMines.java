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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.RMMinesConfig;
import joserodpt.realmines.plugin.gui.GUIManager;
import joserodpt.realmines.plugin.managers.MineManager;
import joserodpt.realmines.plugin.managers.MineResetTasksManager;
import net.milkbowl.vault.economy.Economy;

import java.util.logging.Logger;

public class RealMines extends RealMinesAPI {

    private final Logger logger;
    private final RealMinesPlugin plugin;
    private final MineManager mineManager;
    private final MineResetTasksManager mineResetTasksManager;
    private final GUIManager guiManager;
    private final Economy econ;

    public RealMines(RealMinesPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        this.mineManager = new MineManager(this);
        this.mineResetTasksManager = new MineResetTasksManager(this);
        this.guiManager = new GUIManager(this);
        this.econ = plugin.getEconomy();
    }

    @Override
    public RealMinesPlugin getPlugin() {
        return this.plugin;
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
    public boolean hasNewUpdate() {
        return plugin.newUpdate;
    }

    @Override
    public void reload() {
        RMConfig.reload();
        RMLanguageConfig.reload();
        RMMinesConfig.reload();
        this.mineManager.unloadMines();
        this.mineManager.loadMines();
        this.logger.info("[RealMines] Loaded " + this.mineManager.getMines().size() + " mines and " + this.mineManager.getSigns().size() + " mine signs.");
    }

    @Override
    public Economy getEconomy() {
        return this.econ;
    }

    @Override
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }
}
