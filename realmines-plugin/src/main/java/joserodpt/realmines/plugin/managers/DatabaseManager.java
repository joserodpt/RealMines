package joserodpt.realmines.plugin.managers;

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

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.jdbc.db.DatabaseTypeUtils;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.logger.NullLogBackend;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMSQLConfig;
import joserodpt.realmines.api.database.RMPlayerAchievement;
import joserodpt.realmines.api.database.RMPlayerBlockStat;
import joserodpt.realmines.api.database.RMPlayerData;
import joserodpt.realmines.api.database.RMPlayerStats;
import joserodpt.realmines.api.managers.DatabaseManagerAPI;
import joserodpt.realmines.plugin.RealMines;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DatabaseManager extends DatabaseManagerAPI {

    private static final String PLAYERDATA_TABLE = "realmines_playerdata";
    private static final String BLOCK_STATS_TABLE = "realmines_player_block_stats";

    private final RealMines rm;
    private final ConnectionSource connectionSource;

    private final Dao<RMPlayerData, UUID> playerDataDao;
    private final Dao<RMPlayerBlockStat, UUID> blockStatsDao;
    private final Dao<RMPlayerAchievement, UUID> achievementsDao;

    /**
     * Loaded players only, which in practice means the ones online right now. Rows come in when a
     * player logs in and go out when they leave, so this never grows with the size of the database.
     */
    private final Map<UUID, RMPlayerStats> cache = new ConcurrentHashMap<>();

    /**
     * Players whose cached rows have changed since the last write, and for each of them which
     * materials moved. Writing only those keeps a flush proportional to how much was actually
     * mined rather than to how many different blocks the player has ever touched.
     */
    private final Set<UUID> dirty = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Set<String>> dirtyMaterials = new ConcurrentHashMap<>();

    /**
     * Leaderboards can't be answered from the player cache any more, since it only holds whoever is
     * online. They are queried in the background instead and answered from these snapshots, so the
     * main thread never waits on the database to draw a GUI or fill in a placeholder.
     */
    private volatile List<RMPlayerData> topTotal = Collections.emptyList();
    private final Map<String, List<RMPlayerBlockStat>> topByMaterial = new ConcurrentHashMap<>();
    private volatile List<Material> trackedMaterials = Collections.emptyList();

    /**
     * Materials someone asked to rank that weren't in the snapshot yet, picked up by the next refresh.
     */
    private final Set<String> requestedMaterials = ConcurrentHashMap.newKeySet();

    /**
     * Players who quit but whose cached rows are still waiting on a write that worked. They are only
     * dropped from the cache once one does, so a failed write on quit is retried by the next flush
     * instead of throwing the whole session away.
     */
    private final Set<UUID> unloaded = ConcurrentHashMap.newKeySet();

    /**
     * Unlocks not yet on disk. Each row leaves this only once it has been written.
     */
    private final Map<UUID, Set<RMPlayerAchievement>> pendingAchievements = new ConcurrentHashMap<>();

    /**
     * Owned by the plugin rather than Bukkit's async pool, so shutdown can wait for whatever is queued
     * or running instead of the scheduler silently dropping it.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        final Thread t = new Thread(r, "RealMines-Database");
        t.setDaemon(true);
        return t;
    });

    /**
     * Held for every write and for the final flush, so close() can't pull the connection out from under
     * a write that the flush timer is still in the middle of.
     */
    private final Object writeLock = new Object();
    private volatile boolean closed = false;

    public DatabaseManager(final RealMines rm) throws SQLException {
        this.rm = rm;

        //ORMLite logs a wall of text on startup otherwise
        LoggerFactory.setLogBackendFactory(new NullLogBackend.NullLogBackendFactory());

        final String url = getDatabaseURL();
        final String username = RMSQLConfig.file().getString("username");
        final String password = RMSQLConfig.file().getString("password");
        if (url.startsWith("jdbc:sqlite:")) {
            //a local file that never times out, and more than one connection to it only buys "database is locked"
            this.connectionSource = new JdbcConnectionSource(url, username, password, DatabaseTypeUtils.createDatabaseType(url));
        } else {
            //a single JdbcConnectionSource connection is never reopened, so once the server drops it (MySQL's
            //wait_timeout, a restart, a network blip) every read and write fails until the next restart
            final JdbcPooledConnectionSource pooled = new JdbcPooledConnectionSource(url, username, password,
                    DatabaseTypeUtils.createDatabaseType(url));
            pooled.setTestBeforeGet(true);
            pooled.setMaxConnectionAgeMillis(TimeUnit.MINUTES.toMillis(30));
            this.connectionSource = pooled;
        }

        TableUtils.createTableIfNotExists(this.connectionSource, RMPlayerData.class);
        TableUtils.createTableIfNotExists(this.connectionSource, RMPlayerBlockStat.class);
        TableUtils.createTableIfNotExists(this.connectionSource, RMPlayerAchievement.class);

        this.playerDataDao = DaoManager.createDao(this.connectionSource, RMPlayerData.class);
        this.blockStatsDao = DaoManager.createDao(this.connectionSource, RMPlayerBlockStat.class);
        this.achievementsDao = DaoManager.createDao(this.connectionSource, RMPlayerAchievement.class);

        //for anyone who ran a build from before the leaderboards stopped needing a player lookup per row
        createColumnIfNotExists(BLOCK_STATS_TABLE, "player_name", "VARCHAR(255)");
    }

    private String getDatabaseURL() {
        final String database = RMSQLConfig.file().getString("database", "RealMines");
        final String host = RMSQLConfig.file().getString("host", "localhost");
        final int port = RMSQLConfig.file().getInt("port", 3306);

        switch (RMSQLConfig.file().getString("driver", "SQLITE").toLowerCase()) {
            case "mysql":
            case "mariadb":
                return "jdbc:mysql://" + host + ":" + port + "/" + database;
            case "postgresql":
                return "jdbc:postgresql://" + host + ":" + port + "/" + database;
            case "sqlserver":
                return "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + database;
            default:
                return "jdbc:sqlite:" + new File(this.rm.getPlugin().getDataFolder(), database + ".db");
        }
    }

    /**
     * Adds a column to an existing table if it isn't there yet, so new stats can be introduced in a
     * later version without losing anyone's data.
     */
    public void createColumnIfNotExists(final String tableName, final String columnName, final String columnType) {
        try {
            final boolean exists;
            //handed back afterwards, or a pooled source would lose that connection for good
            final DatabaseConnection connection = this.connectionSource.getReadOnlyConnection(tableName);
            try {
                final DatabaseMetaData metaData = connection.getUnderlyingConnection().getMetaData();
                try (final ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
                    exists = columns.next();
                }
            } finally {
                this.connectionSource.releaseConnection(connection);
            }
            if (!exists) {
                this.playerDataDao.executeRaw("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
            }
        } catch (final SQLException e) {
            this.rm.getLogger().warning("Couldn't add column " + columnName + " to " + tableName + ": " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- loading

    @Override
    public RMPlayerStats getStats(final UUID uuid) {
        return this.cache.get(uuid);
    }

    @Override
    public void loadStats(final UUID uuid, final Consumer<RMPlayerStats> callback) {
        final RMPlayerStats loaded = this.cache.get(uuid);
        if (loaded != null) {
            callback.accept(loaded);
            return;
        }

        runAsync(() -> {
            RMPlayerStats read = null;
            try {
                read = read(uuid);
            } catch (final SQLException e) {
                this.rm.getLogger().warning("Couldn't read stats for " + uuid + ": " + e.getMessage());
            }

            final RMPlayerStats result = read;
            runSync(() -> callback.accept(result));
        });
    }

    @Override
    public void loadIntoCache(final UUID uuid, final String name) {
        //logging back in before their quit write finished: keep using that entry instead of evicting it
        this.unloaded.remove(uuid);
        if (this.cache.containsKey(uuid)) {
            return;
        }

        final RMPlayerStats stats;
        try {
            final RMPlayerStats read = read(uuid);
            //nothing on disk means we have genuinely never seen them, so start them at zero
            stats = read != null ? read : new RMPlayerStats(new RMPlayerData(uuid, name),
                    new ConcurrentHashMap<>(), ConcurrentHashMap.newKeySet());
        } catch (final SQLException e) {
            //deliberately not cached. A failed read is not an empty player, and caching a blank
            //row here would make the next flush overwrite their real totals with zeroes. Better to
            //not count this session than to destroy what they already earned.
            this.rm.getLogger().warning("Couldn't read stats for " + uuid + ", not tracking them this session: " + e.getMessage());
            return;
        }
        this.cache.put(uuid, stats);
    }

    /**
     * Reads one player's three tables. Blocking, so it must not run on the main thread.
     *
     * @return null if that player has no rows at all
     * @throws SQLException if the read failed, which callers must not confuse with a new player
     */
    private RMPlayerStats read(final UUID uuid) throws SQLException {
        final RMPlayerData data = this.playerDataDao.queryForId(uuid);
        if (data == null) {
            return null;
        }

        final Map<String, RMPlayerBlockStat> blocks = new ConcurrentHashMap<>();
        for (final RMPlayerBlockStat stat : this.blockStatsDao.queryForEq("player_uuid", uuid)) {
            blocks.put(stat.getMaterial(), stat);
        }

        final Set<String> achievements = ConcurrentHashMap.newKeySet();
        for (final RMPlayerAchievement achievement : this.achievementsDao.queryForEq("player_uuid", uuid)) {
            achievements.add(achievement.getAchievementID());
        }

        return new RMPlayerStats(data, blocks, achievements);
    }

    @Override
    public RMPlayerStats registerPlayer(final OfflinePlayer player) {
        final UUID uuid = player.getUniqueId();

        this.unloaded.remove(uuid);
        RMPlayerStats stats = this.cache.get(uuid);
        if (stats == null) {
            //the async pre login preload didn't happen or didn't finish, so pay for it here
            loadIntoCache(uuid, player.getName());
            stats = this.cache.get(uuid);
        }
        if (stats == null) {
            return null; //the read failed, so this player goes untracked for the session
        }

        stats.getData().setName(player.getName());
        stats.getData().setLastJoin(System.currentTimeMillis());
        this.dirty.add(uuid);
        return stats;
    }

    @Override
    public void unloadPlayer(final UUID uuid) {
        final boolean hadChanges = this.dirty.remove(uuid);
        final Set<String> materials = this.dirtyMaterials.remove(uuid);
        final RMPlayerStats stats = this.cache.get(uuid);
        if (stats == null) {
            return;
        }

        this.unloaded.add(uuid);
        //dropped only once the write is done, so the writer still has the rows it needs. If it fails,
        //write() marks them dirty again and the entry stays until a later flush gets it on disk
        runAsync(() -> {
            if (!hadChanges || write(uuid, stats, materials)) {
                evictIfGone(uuid);
            }
        });
    }

    /**
     * Drops a player who quit from the cache, back on the main thread and only if they haven't
     * reconnected in the meantime - a fast rejoin reuses this entry, and removing it would leave an
     * online player with nothing to count into.
     */
    private void evictIfGone(final UUID uuid) {
        if (!this.unloaded.contains(uuid)) {
            return;
        }
        runSync(() -> {
            if (this.unloaded.contains(uuid) && !this.dirty.contains(uuid) && Bukkit.getPlayer(uuid) == null) {
                this.unloaded.remove(uuid);
                this.cache.remove(uuid);
            }
        });
    }

    // ---------------------------------------------------------------- counting

    @Override
    public void addBlocksMined(final UUID uuid, final Material material, final long amount) {
        final RMPlayerStats stats = this.cache.get(uuid);
        if (stats == null) {
            //not loaded, so there is nothing safe to add to - their next login reads the real values
            return;
        }

        stats.getData().addBlocksMined(amount);
        stats.getBlockStats()
                .computeIfAbsent(material.name(), m -> new RMPlayerBlockStat(uuid, stats.getName(), m))
                .add(amount);

        this.dirtyMaterials.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(material.name());
        this.dirty.add(uuid);
    }

    @Override
    public void grantAchievement(final UUID uuid, final String achievementID) {
        final RMPlayerStats stats = this.cache.get(uuid);
        if (stats == null || !stats.getUnlockedAchievements().add(achievementID)) {
            return; //not loaded, or they already had it
        }

        //queued with the player's other changes rather than written on its own, so a failed write is
        //retried by the next flush. Losing the row would grant the achievement, rewards and all, again
        //on their next login
        this.pendingAchievements.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet())
                .add(new RMPlayerAchievement(uuid, achievementID));
        this.dirty.add(uuid);

        //the stats that earned it should hit the disk together with the unlock
        flush(uuid, true);
    }

    // ---------------------------------------------------------------- lookup

    @Override
    public void findPlayer(final String name, final Consumer<RMPlayerData> callback) {
        final Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            final RMPlayerStats stats = this.cache.get(online.getUniqueId());
            if (stats != null) {
                callback.accept(stats.getData());
                return;
            }
        }

        runAsync(() -> {
            RMPlayerData found = null;
            try {
                //a plain String is pasted into the SQL unescaped, a SelectArg is sent as a bound parameter
                found = this.playerDataDao.queryBuilder().where().eq("name", new SelectArg(name)).queryForFirst();
            } catch (final SQLException e) {
                this.rm.getLogger().warning("Couldn't look up the player named " + name + ": " + e.getMessage());
            }

            final RMPlayerData result = found;
            runSync(() -> callback.accept(result));
        });
    }

    // ---------------------------------------------------------------- writing

    @Override
    public void flush(final UUID uuid, final boolean async) {
        if (!this.dirty.remove(uuid)) {
            return;
        }
        final Set<String> materials = this.dirtyMaterials.remove(uuid);
        final RMPlayerStats stats = this.cache.get(uuid);
        if (stats == null) {
            return;
        }

        if (async) {
            runAsync(() -> write(uuid, stats, materials));
        } else {
            write(uuid, stats, materials);
        }
    }

    @Override
    public void flushAll(final boolean async) {
        if (this.dirty.isEmpty()) {
            return;
        }

        //taken before the write so blocks mined during it are picked up by the next flush instead
        //of being cleared without ever being written
        final Map<UUID, Set<String>> pending = new HashMap<>();
        for (final UUID uuid : new ArrayList<>(this.dirty)) {
            if (this.dirty.remove(uuid)) {
                pending.put(uuid, this.dirtyMaterials.remove(uuid));
            }
        }
        if (pending.isEmpty()) {
            return;
        }

        final Runnable write = () -> pending.forEach((uuid, materials) -> {
            final RMPlayerStats stats = this.cache.get(uuid);
            //a player whose write on quit failed is still cached, and can go once this one works
            if (stats != null && write(uuid, stats, materials)) {
                evictIfGone(uuid);
            }
        });

        if (async) {
            runAsync(write);
        } else {
            write.run();
        }
    }

    /**
     * @return whether the rows made it to disk. When they didn't, the player is dirty again.
     */
    private boolean write(final UUID uuid, final RMPlayerStats stats, final Set<String> materials) {
        synchronized (this.writeLock) {
            return writeLocked(uuid, stats, materials);
        }
    }

    private boolean writeLocked(final UUID uuid, final RMPlayerStats stats, final Set<String> materials) {
        try {
            if (this.closed) {
                throw new SQLException("the database connection is closed");
            }
            this.playerDataDao.createOrUpdate(stats.getData());

            if (materials != null) {
                for (final String material : materials) {
                    final RMPlayerBlockStat stat = stats.getBlockStats().get(material);
                    if (stat != null) {
                        //keeps the denormalised name in step with a rename
                        stat.setPlayerName(stats.getName());
                        this.blockStatsDao.createOrUpdate(stat);
                    }
                }
            }

            final Set<RMPlayerAchievement> achievements = this.pendingAchievements.get(uuid);
            if (achievements != null) {
                for (final RMPlayerAchievement row : new ArrayList<>(achievements)) {
                    this.achievementsDao.create(row);
                    //one at a time, so a retry after a failure part way doesn't insert the rest twice
                    achievements.remove(row);
                }
                this.pendingAchievements.computeIfPresent(uuid, (k, rows) -> rows.isEmpty() ? null : rows);
            }
            return true;
        } catch (final SQLException e) {
            this.rm.getLogger().warning("Couldn't save stats for " + uuid + ": " + e.getMessage());
            //put it back so the next flush retries instead of silently dropping the progress
            if (materials != null) {
                this.dirtyMaterials.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).addAll(materials);
            }
            this.dirty.add(uuid);
            return false;
        }
    }

    // ---------------------------------------------------------------- leaderboards

    @Override
    public List<RMPlayerData> getTopTotalBlocksMined(final int limit) {
        final List<RMPlayerData> snapshot = this.topTotal;
        return snapshot.size() > limit ? new ArrayList<>(snapshot.subList(0, limit)) : new ArrayList<>(snapshot);
    }

    @Override
    public List<RMPlayerBlockStat> getTopBlocksMined(final Material material, final int limit) {
        final List<RMPlayerBlockStat> snapshot = this.topByMaterial.get(material.name());
        if (snapshot == null) {
            //not ranked yet: ask for it and let the caller show an empty board until the next refresh
            this.requestedMaterials.add(material.name());
            return Collections.emptyList();
        }
        return snapshot.size() > limit ? new ArrayList<>(snapshot.subList(0, limit)) : new ArrayList<>(snapshot);
    }

    @Override
    public List<Material> getTrackedMaterials() {
        return this.trackedMaterials;
    }

    @Override
    public void refreshLeaderboards() {
        synchronized (this.writeLock) {
            if (!this.closed) {
                refreshLeaderboardsLocked();
            }
        }
    }

    private void refreshLeaderboardsLocked() {
        final int limit = Math.max(1, RMConfig.file().getInt("RealMines.Stats.Leaderboard-Size", 28));

        try {
            this.topTotal = this.playerDataDao.queryBuilder()
                    .orderBy("total_blocks_mined", false)
                    .limit((long) limit)
                    .where().gt("total_blocks_mined", 0)
                    .query();
        } catch (final SQLException e) {
            this.rm.getLogger().warning("Couldn't refresh the mining leaderboard: " + e.getMessage());
        }

        try {
            final List<Material> tracked = new ArrayList<>();
            final QueryBuilder<RMPlayerBlockStat, UUID> distinct = this.blockStatsDao.queryBuilder();
            distinct.distinct().selectColumns("material");
            for (final RMPlayerBlockStat row : distinct.query()) {
                final Material material = Material.matchMaterial(row.getMaterial());
                //a material that no longer exists in this Minecraft version
                if (material != null) {
                    tracked.add(material);
                }
            }
            tracked.sort(java.util.Comparator.comparing(Material::name));
            this.trackedMaterials = tracked;

            //rank the materials somebody has actually looked at, rather than every one on record
            for (final String name : this.requestedMaterials) {
                this.topByMaterial.put(name, this.blockStatsDao.queryBuilder()
                        .orderBy("amount", false)
                        .limit((long) limit)
                        .where().eq("material", new SelectArg(name)).and().gt("amount", 0)
                        .query());
            }
        } catch (final SQLException e) {
            this.rm.getLogger().warning("Couldn't refresh the per block leaderboards: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- plumbing

    private void runAsync(final Runnable runnable) {
        try {
            this.executor.execute(runnable);
        } catch (final RejectedExecutionException e) {
            //already shutting down, so there is nowhere left to hand it to
            runnable.run();
        }
    }

    private void runSync(final Runnable runnable) {
        if (this.rm.getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTask(this.rm.getPlugin(), runnable);
        } else {
            runnable.run();
        }
    }

    @Override
    public void close() {
        //let whatever was already handed off (quit writes, achievement rows, lookups) finish first
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(10, TimeUnit.SECONDS)) {
                this.rm.getLogger().warning("Gave up waiting on pending database writes.");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        synchronized (this.writeLock) {
            //taking the lock waits out a flush the timer may still be running, and writing again here
            //picks up anything that was queued or failed while it did
            flushAll(false);
            this.closed = true;
            try {
                this.connectionSource.close();
            } catch (final Exception e) {
                this.rm.getLogger().warning("Couldn't close the database connection: " + e.getMessage());
            }
        }
    }
}
