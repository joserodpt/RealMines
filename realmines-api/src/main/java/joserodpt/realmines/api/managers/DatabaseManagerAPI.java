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
 * @author José Rodrigues © 2019-2026
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.database.RMPlayerBlockStat;
import joserodpt.realmines.api.database.RMPlayerData;
import joserodpt.realmines.api.database.RMPlayerStats;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Stores per player mining stats and unlocked achievements.
 * <p>
 * Only the players currently online are held in memory: their rows are read when they log in and
 * dropped when they leave, so memory use follows the player count rather than how many people have
 * ever played. Counting a mined block touches only that cache and flags the player as dirty; the
 * writes happen on a timer, when the player quits and when the server stops.
 * <p>
 * Anything about an offline player has to come off the database, so it goes through
 * {@link #loadStats(UUID, Consumer)}, which does the read on another thread.
 */
public abstract class DatabaseManagerAPI {

    /**
     * The live stats of a player who is loaded, which in practice means online.
     *
     * @return their stats, or null if they aren't loaded - use {@link #loadStats(UUID, Consumer)}
     * for anyone else
     */
    public abstract RMPlayerStats getStats(final UUID uuid);

    /**
     * Fetches a player's stats whether they are online or not, then hands them to the callback on
     * the main thread. Online players are served straight from the cache without touching the
     * database. The callback gets null if that player has never mined anything.
     */
    public abstract void loadStats(final UUID uuid, final Consumer<RMPlayerStats> callback);

    /**
     * Reads a player's rows into the cache. Hits the database, so it must not run on the main
     * thread. Called from the async pre login event.
     */
    public abstract void loadIntoCache(final UUID uuid, final String name);

    /**
     * Makes sure this player is cached and stamps their name and last join. Falls back to a
     * blocking read if the pre login preload didn't happen.
     *
     * @return their stats, or null if the read failed - in which case they go untracked for this
     * session rather than risk overwriting what they already earned
     */
    public abstract RMPlayerStats registerPlayer(final OfflinePlayer player);

    /**
     * Writes this player's pending changes and drops them from the cache. Called when they quit.
     */
    public abstract void unloadPlayer(final UUID uuid);

    /**
     * Adds to both the total and the per material counter of a loaded player, and flags them dirty.
     * Does nothing if the player isn't loaded.
     */
    public abstract void addBlocksMined(final UUID uuid, final Material material, final long amount);

    /**
     * Records an achievement as unlocked. Written to the database right away instead of waiting for
     * the next flush, because losing one means handing out the reward a second time.
     */
    public abstract void grantAchievement(final UUID uuid, final String achievementID);

    /**
     * Looks a player up by name, online players first and then the database. The callback runs on
     * the main thread and gets null if nobody by that name has mining stats.
     */
    public abstract void findPlayer(final String name, final Consumer<RMPlayerData> callback);

    /**
     * Writes this player's pending changes, if any.
     */
    public abstract void flush(final UUID uuid, final boolean async);

    /**
     * Writes every loaded player with pending changes.
     */
    public abstract void flushAll(final boolean async);

    /**
     * Players with the most blocks mined overall, highest first.
     * <p>
     * Served from a snapshot that {@link #refreshLeaderboards()} rebuilds in the background, so
     * this is instant and safe to call from the main thread, at the cost of being up to one refresh
     * interval out of date.
     */
    public abstract List<RMPlayerData> getTopTotalBlocksMined(final int limit);

    /**
     * Players with the most blocks mined of one material, highest first. Same snapshot caveat as
     * {@link #getTopTotalBlocksMined(int)}. A material nobody has been ranked for yet comes back
     * empty and is queued for the next refresh.
     */
    public abstract List<RMPlayerBlockStat> getTopBlocksMined(final Material material, final int limit);

    /**
     * Materials anybody has ever mined, so the leaderboard can offer them. Snapshot backed.
     */
    public abstract List<Material> getTrackedMaterials();

    /**
     * Rebuilds the leaderboard snapshots from the database. Hits the database, so it must not run
     * on the main thread.
     */
    public abstract void refreshLeaderboards();

    public abstract void close();
}
