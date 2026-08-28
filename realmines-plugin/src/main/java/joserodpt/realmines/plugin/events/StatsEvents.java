package joserodpt.realmines.plugin.events;

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

import joserodpt.realmines.api.event.MineBlockBreakEvent;
import joserodpt.realmines.api.managers.DatabaseManagerAPI;
import joserodpt.realmines.api.utils.PlayerHeads;
import joserodpt.realmines.plugin.RealMines;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Counts the blocks players mine, hands out the achievements they earn along the way, and keeps
 * each player's rows in memory only for as long as they are on the server.
 */
public class StatsEvents implements Listener {

    private final RealMines rm;

    public StatsEvents(final RealMines rm) {
        this.rm = rm;
    }

    /**
     * Reads the player's stats while they are still connecting. This event already runs off the
     * main thread, so the database read costs the server nothing.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(final AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        final DatabaseManagerAPI db = this.rm.getDatabaseManager();
        if (db == null || !this.rm.isStatsEnabled()) {
            return;
        }

        try {
            db.loadIntoCache(e.getUniqueId(), e.getName());
        } catch (final Exception ex) {
            //never keep somebody out of the server over their stats
            this.rm.getLogger().warning("Couldn't preload stats for " + e.getName() + ": " + ex.getMessage());
        }
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        final DatabaseManagerAPI db = this.rm.getDatabaseManager();
        if (db == null || !this.rm.isStatsEnabled()) {
            return;
        }

        if (db.registerPlayer(e.getPlayer()) == null) {
            //their stats couldn't be read, so there is nothing to check against
            return;
        }

        //catches up anything earned while the achievement was misconfigured or newly added
        this.rm.getAchievementsManager().checkAllAchievements(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMineBlockBreak(final MineBlockBreakEvent e) {
        //the event also fires when a block is placed inside a mine, which is not something a player
        //should get credit for, and never something that takes credit away
        if (!e.isBroken()) {
            return;
        }

        //no player behind it: this is the explosion path
        final Player player = e.getPlayer();
        if (player == null) {
            return;
        }

        //another plugin running later than the event may still have cancelled the break
        if (e.getCancellable() != null && e.getCancellable().isCancelled()) {
            return;
        }

        final DatabaseManagerAPI db = this.rm.getDatabaseManager();
        if (db == null || !this.rm.isStatsEnabled()) {
            return;
        }

        final Material material = e.getMaterial();
        db.addBlocksMined(player.getUniqueId(), material, 1L);
        this.rm.getAchievementsManager().checkAchievements(player, material);
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        //dropped so a player who changed their skin gets the new one on their next join.
        //done before the database check, since heads work with or without stats being on
        PlayerHeads.forget(e.getPlayer().getUniqueId());

        final DatabaseManagerAPI db = this.rm.getDatabaseManager();
        if (db == null) {
            return;
        }
        //writes whatever they mined, then drops them from memory
        db.unloadPlayer(e.getPlayer().getUniqueId());
    }
}
