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
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

/**
 * How many blocks of one material a player has mined. One row per (player, material) pair.
 * <p>
 * The material is stored as its name instead of the enum so that a material that disappears
 * between Minecraft versions leaves a dead row behind instead of failing the whole table load.
 */
@DatabaseTable(tableName = "realmines_player_block_stats")
public class RMPlayerBlockStat {

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private UUID id;

    @DatabaseField(columnName = "player_uuid", canBeNull = false, index = true)
    private UUID playerUUID;

    @DatabaseField(columnName = "material", canBeNull = false, index = true)
    private String material;

    /**
     * Denormalised so a leaderboard can show names straight from one query, instead of looking up
     * a player row for every entry it ranks.
     */
    @DatabaseField(columnName = "player_name")
    private String playerName;

    @DatabaseField(columnName = "amount")
    private long amount;

    //required by ORMLite
    public RMPlayerBlockStat() {
    }

    public RMPlayerBlockStat(final UUID playerUUID, final String playerName, final String material) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.material = material;
        this.amount = 0L;
    }

    public UUID getID() {
        return this.id;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(final String playerName) {
        this.playerName = playerName;
    }

    public String getMaterial() {
        return this.material;
    }

    public long getAmount() {
        return this.amount;
    }

    public void setAmount(final long amount) {
        this.amount = amount;
    }

    public void add(final long amount) {
        this.amount += amount;
    }

    @Override
    public String toString() {
        return "RMPlayerBlockStat{" +
                "playerUUID=" + playerUUID +
                ", material='" + material + '\'' +
                ", amount=" + amount +
                '}';
    }
}
