package joserodpt.realmines.api;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import com.google.common.base.Preconditions;
import joserodpt.realmines.api.managers.MineManagerAPI;
import joserodpt.realmines.api.managers.MineResetTasksManagerAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public abstract class RealMinesAPI {

    private static final Random r = new Random();
    private static RealMinesAPI instance;

    /**
     * Gets instance of this API
     *
     * @return RealMinesAPI API instance
     */
    public static RealMinesAPI getInstance() {
        return instance;
    }

    /**
     * Sets the RealMinesAPI instance.
     * <b>Note! This method may only be called once</b>
     *
     * @param instance the new instance to set
     */
    public static void setInstance(RealMinesAPI instance) {
        Preconditions.checkNotNull(instance, "instance");
        Preconditions.checkArgument(RealMinesAPI.instance == null, "Instance already set");
        RealMinesAPI.instance = instance;
    }

    public abstract JavaPlugin getPlugin();

    public abstract MineManagerAPI getMineManager();

    public abstract MineResetTasksManagerAPI getMineResetTasksManager();

    public abstract boolean hasNewUpdate();

    public abstract void reload();

    public static Random getRand() {
        return r;
    }

    public abstract Economy getEconomy();

    public abstract String getVersion();
}
