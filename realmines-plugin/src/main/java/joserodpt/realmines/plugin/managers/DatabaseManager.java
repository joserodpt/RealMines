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
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.db.DatabaseTypeUtils;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.logger.NullLogBackend;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    public DatabaseManager(final RealMines rm) throws SQLException {
        this.rm = rm;

        //ORMLite logs a wall of text on startup otherwise
        LoggerFactory.setLogBackendFactory(new NullLogBackend.NullLogBackendFactory());

        final String url = getDatabaseURL();
        this.connectionSource = new JdbcConnectionSource(url,
                RMSQLConfig.file().getString("username"),
                RMSQLConfig.file().getString("password"),
                DatabaseTypeUtils.createDatabaseType(url));

        TableUtils.createTableIfNotExists(this.connectionSource, RMPlayerData.class);
        TableUtils.createTableIfNotExists(this.connectionSource, RMPlayerBlockStat.class);
        TableUtils.createTableIfNotExists(this.connectionSource, RMPlayerAchievement.class);

        this.playerDataDao = DaoManager.createDao(this.connectionSource, RMPlayerData.class);
        this.blockStatsDao = DaoManager.createDao(this.connectionSource, RMPlayerBlockStat.class);
        this.achievementsDao = DaoManager.createDao(this.connectionSource, RMPlayerAchievement.class);

        //for anyone who ran a build from before the leaderboards stopped needing a player lookup per row
        createColumnIfNotExists(BLOCK_STATS_TABLE, "player_name", "VARCHAR");
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
            final DatabaseMetaData metaData = this.connectionSource.getReadOnlyConnection(tableName)
                    .getUnderlyingConnection().getMetaData();
            if (!metaData.getColumns(null, null, tableName, columnName).next()) {
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

        //dropped only once the write is done, so the writer still has the rows it needs
        runAsync(() -> {
            if (hadChanges) {
                write(uuid, stats, materials);
            }
            //back on the main thread to drop it, and only if they haven't reconnected in the
            //meantime - a fast rejoin reuses this entry, and removing it would leave an online
            //player with nothing to count into
            runSync(() -> {
                if (Bukkit.getPlayer(uuid) == null) {
                    this.cache.remove(uuid);
                }
            });
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

        final RMPlayerAchievement row = new RMPlayerAchievement(uuid, achievementID);
        runAsync(() -> {
            try {
                this.achievementsDao.create(row);
            } catch (final SQLException e) {
                this.rm.getLogger().warning("Couldn't save achievement " + achievementID + " for " + uuid + ": " + e.getMessage());
            }
        });

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
                found = this.playerDataDao.queryBuilder().where().eq("name", name).queryForFirst();
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
            if (stats != null) {
                write(uuid, stats, materials);
            }
        });

        if (async) {
            runAsync(write);
        } else {
            write.run();
        }
    }

    private void write(final UUID uuid, final RMPlayerStats stats, final Set<String> materials) {
        try {
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
        } catch (final SQLException e) {
            this.rm.getLogger().warning("Couldn't save stats for " + uuid + ": " + e.getMessage());
            //put it back so the next flush retries instead of silently dropping the progress
            if (materials != null) {
                this.dirtyMaterials.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).addAll(materials);
            }
            this.dirty.add(uuid);
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
                        .where().eq("material", name).and().gt("amount", 0)
                        .query());
            }
        } catch (final SQLException e) {
            this.rm.getLogger().warning("Couldn't refresh the per block leaderboards: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- plumbing

    private void runAsync(final Runnable runnable) {
        //async tasks are refused once the server starts shutting down
        if (this.rm.getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(this.rm.getPlugin(), runnable);
        } else {
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
        try {
            this.connectionSource.close();
        } catch (final Exception e) {
            this.rm.getLogger().warning("Couldn't close the database connection: " + e.getMessage());
        }
    }
}
