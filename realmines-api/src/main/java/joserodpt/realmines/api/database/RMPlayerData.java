package joserodpt.realmines.api.database;

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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

/**
 * One row per player holding the stats that are not broken down by material.
 */
@DatabaseTable(tableName = "realmines_playerdata")
public class RMPlayerData {

    @DatabaseField(columnName = "uuid", canBeNull = false, id = true)
    private UUID uuid;

    @DatabaseField(columnName = "name")
    private String name;

    @DatabaseField(columnName = "total_blocks_mined")
    private long totalBlocksMined;

    @DatabaseField(columnName = "first_join")
    private long firstJoin;

    @DatabaseField(columnName = "last_join")
    private long lastJoin;

    //required by ORMLite
    public RMPlayerData() {
    }

    public RMPlayerData(final UUID uuid, final String name) {
        this.uuid = uuid;
        this.name = name;
        this.totalBlocksMined = 0L;
        this.firstJoin = System.currentTimeMillis();
        this.lastJoin = this.firstJoin;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public long getTotalBlocksMined() {
        return this.totalBlocksMined;
    }

    public void setTotalBlocksMined(final long totalBlocksMined) {
        this.totalBlocksMined = totalBlocksMined;
    }

    public void addBlocksMined(final long amount) {
        this.totalBlocksMined += amount;
    }

    public long getFirstJoin() {
        return this.firstJoin;
    }

    public long getLastJoin() {
        return this.lastJoin;
    }

    public void setLastJoin(final long lastJoin) {
        this.lastJoin = lastJoin;
    }

    @Override
    public String toString() {
        return "RMPlayerData{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", totalBlocksMined=" + totalBlocksMined +
                '}';
    }
}
