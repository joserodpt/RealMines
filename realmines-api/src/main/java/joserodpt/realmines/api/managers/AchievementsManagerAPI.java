package joserodpt.realmines.api.managers;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.achievements.RMAchievement;
import joserodpt.realmines.api.database.RMPlayerStats;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;

public abstract class AchievementsManagerAPI {

    public abstract void loadAchievements();

    public abstract RMAchievement getAchievement(final String id);

    /**
     * Every configured achievement, in the order they appear in achievements.yml.
     */
    public abstract Collection<RMAchievement> getAchievements();

    public abstract int getUnlockedCount(final RMPlayerStats stats);

    /**
     * Gives the player any achievement they have earned but not received yet, but only checks the
     * ones that could have been advanced by mining this material.
     */
    public abstract void checkAchievements(final Player player, final Material mined);

    /**
     * Checks every achievement. Used on join, so that progress made while achievements were
     * misconfigured or while the player was offline still gets handed out.
     */
    public abstract void checkAllAchievements(final Player player);
}
