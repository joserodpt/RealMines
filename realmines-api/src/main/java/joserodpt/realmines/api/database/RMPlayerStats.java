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

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Everything known about one player's mining, gathered in one place.
 * <p>
 * For an online player this wraps the live cached rows, so it keeps reporting current numbers as
 * they keep mining. For an offline player it wraps rows read from the database, and is a snapshot.
 */
public class RMPlayerStats {

    private final RMPlayerData data;
    private final Map<String, RMPlayerBlockStat> blocks;
    private final Set<String> achievements;

    public RMPlayerStats(final RMPlayerData data, final Map<String, RMPlayerBlockStat> blocks, final Set<String> achievements) {
        this.data = data;
        this.blocks = blocks;
        this.achievements = achievements;
    }

    public RMPlayerData getData() {
        return this.data;
    }

    public UUID getUUID() {
        return this.data.getUUID();
    }

    public String getName() {
        return this.data.getName();
    }

    public long getTotalBlocksMined() {
        return this.data.getTotalBlocksMined();
    }

    public long getFirstJoin() {
        return this.data.getFirstJoin();
    }

    public long getBlocksMined(final Material material) {
        final RMPlayerBlockStat stat = this.blocks.get(material.name());
        return stat == null ? 0L : stat.getAmount();
    }

    /**
     * Every block this player has mined, keyed by material. Materials that no longer exist in this
     * Minecraft version are left out.
     */
    public Map<Material, Long> getBlocksMined() {
        final Map<Material, Long> out = new HashMap<>();
        for (final RMPlayerBlockStat stat : this.blocks.values()) {
            final Material material = Material.matchMaterial(stat.getMaterial());
            if (material != null) {
                out.put(material, stat.getAmount());
            }
        }
        return out;
    }

    /**
     * The raw per material rows. Mutated in place while the player is online.
     */
    public Map<String, RMPlayerBlockStat> getBlockStats() {
        return this.blocks;
    }

    public boolean hasAchievement(final String achievementID) {
        return this.achievements.contains(achievementID);
    }

    public Set<String> getUnlockedAchievements() {
        return this.achievements;
    }

    @Override
    public String toString() {
        return "RMPlayerStats{" + this.data + ", blocks=" + this.blocks.size()
                + ", achievements=" + this.achievements.size() + '}';
    }
}
