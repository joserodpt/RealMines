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
 * Records that a player has unlocked an achievement, so its reward is never handed out twice.
 */
@DatabaseTable(tableName = "realmines_player_achievements")
public class RMPlayerAchievement {

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private UUID id;

    @DatabaseField(columnName = "player_uuid", canBeNull = false, index = true)
    private UUID playerUUID;

    @DatabaseField(columnName = "achievement_id", canBeNull = false)
    private String achievementID;

    @DatabaseField(columnName = "unlocked_at")
    private long unlockedAt;

    //required by ORMLite
    public RMPlayerAchievement() {
    }

    public RMPlayerAchievement(final UUID playerUUID, final String achievementID) {
        this.playerUUID = playerUUID;
        this.achievementID = achievementID;
        this.unlockedAt = System.currentTimeMillis();
    }

    public UUID getID() {
        return this.id;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public String getAchievementID() {
        return this.achievementID;
    }

    public long getUnlockedAt() {
        return this.unlockedAt;
    }

    @Override
    public String toString() {
        return "RMPlayerAchievement{" +
                "playerUUID=" + playerUUID +
                ", achievementID='" + achievementID + '\'' +
                '}';
    }
}
