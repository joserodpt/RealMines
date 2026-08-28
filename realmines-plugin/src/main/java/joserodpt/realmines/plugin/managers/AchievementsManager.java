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

import joserodpt.realmines.api.achievements.RMAchievement;
import joserodpt.realmines.api.achievements.RMAchievementType;
import joserodpt.realmines.api.config.RMAchievementsConfig;
import joserodpt.realmines.api.database.RMPlayerStats;
import joserodpt.realmines.api.managers.AchievementsManagerAPI;
import joserodpt.realmines.api.managers.DatabaseManagerAPI;
import joserodpt.realmines.api.mine.components.actions.MineAction;
import joserodpt.realmines.plugin.RealMines;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AchievementsManager extends AchievementsManagerAPI {

    private static final String ROOT = "Achievements";

    private final RealMines rm;

    private final Map<String, RMAchievement> achievements = new LinkedHashMap<>();

    //so a mined block only ever evaluates the achievements it could possibly have advanced
    private final List<RMAchievement> totalBlockAchievements = new ArrayList<>();
    private final Map<Material, List<RMAchievement>> materialAchievements = new HashMap<>();

    public AchievementsManager(final RealMines rm) {
        this.rm = rm;
    }

    @Override
    public void loadAchievements() {
        this.achievements.clear();
        this.totalBlockAchievements.clear();
        this.materialAchievements.clear();

        if (RMAchievementsConfig.file() == null || !RMAchievementsConfig.file().isSection(ROOT)) {
            return;
        }

        for (final String id : RMAchievementsConfig.file().getSection(ROOT).getRoutesAsStrings(false)) {
            final String route = ROOT + "." + id;
            try {
                final RMAchievement achievement = load(id, route);
                if (achievement == null) {
                    continue;
                }

                this.achievements.put(id, achievement);
                if (achievement.getType() == RMAchievementType.MATERIAL) {
                    this.materialAchievements.computeIfAbsent(achievement.getMaterial(), m -> new ArrayList<>()).add(achievement);
                } else {
                    this.totalBlockAchievements.add(achievement);
                }
            } catch (final Exception e) {
                this.rm.getLogger().warning("Achievement " + id + " is invalid and was skipped: " + e.getMessage());
            }
        }
    }

    private RMAchievement load(final String id, final String route) {
        final RMAchievementType type = RMAchievementType.valueOf(
                RMAchievementsConfig.file().getString(route + ".Type", "TOTAL_BLOCKS").toUpperCase());

        Material material = null;
        if (type == RMAchievementType.MATERIAL) {
            final String materialName = RMAchievementsConfig.file().getString(route + ".Material");
            material = materialName == null ? null : Material.matchMaterial(materialName);
            if (material == null) {
                this.rm.getLogger().warning("Achievement " + id + " has an unknown material (" + materialName + ") and was skipped.");
                return null;
            }
        }

        Material icon = Material.matchMaterial(RMAchievementsConfig.file().getString(route + ".Icon", "PAPER"));
        if (icon == null) {
            icon = material == null ? Material.PAPER : material;
        }

        return new RMAchievement(id,
                RMAchievementsConfig.file().getString(route + ".Display-Name", id),
                RMAchievementsConfig.file().getStringList(route + ".Description"),
                icon,
                type,
                material,
                RMAchievementsConfig.file().getLong(route + ".Goal", 1L),
                RMAchievementsConfig.file().getBoolean(route + ".Announce", false),
                loadRewards(id, route + ".Rewards"));
    }

    private List<MineAction> loadRewards(final String achievementID, final String route) {
        if (!RMAchievementsConfig.file().isSection(route)) {
            return Collections.emptyList();
        }

        final List<MineAction> rewards = new ArrayList<>();
        for (final String rewardID : RMAchievementsConfig.file().getSection(route).getRoutesAsStrings(false)) {
            final String rewardRoute = route + "." + rewardID;
            try {
                final MineAction.MineActionType type = MineAction.MineActionType.valueOf(
                        RMAchievementsConfig.file().getString(rewardRoute + ".type"));
                //null mine: these actions belong to an achievement, not to a mine
                final MineAction reward = MineAction.deserialize(rewardID, null, type,
                        RMAchievementsConfig.file().getDouble(rewardRoute + ".chance", 100D),
                        RMAchievementsConfig.file().get(rewardRoute + ".value"));
                if (reward != null) {
                    rewards.add(reward);
                }
            } catch (final Exception e) {
                this.rm.getLogger().warning("Reward " + rewardID + " of achievement " + achievementID
                        + " is invalid and was skipped: " + e.getMessage());
            }
        }
        return rewards;
    }

    @Override
    public RMAchievement getAchievement(final String id) {
        return this.achievements.get(id);
    }

    @Override
    public Collection<RMAchievement> getAchievements() {
        return this.achievements.values();
    }

    @Override
    public int getUnlockedCount(final RMPlayerStats stats) {
        if (stats == null) {
            return 0;
        }
        int count = 0;
        for (final RMAchievement achievement : this.achievements.values()) {
            if (stats.hasAchievement(achievement.getID())) {
                ++count;
            }
        }
        return count;
    }

    @Override
    public void checkAchievements(final Player player, final Material mined) {
        grantCompleted(player, this.totalBlockAchievements);
        grantCompleted(player, this.materialAchievements.get(mined));
    }

    @Override
    public void checkAllAchievements(final Player player) {
        grantCompleted(player, this.achievements.values());
    }

    private void grantCompleted(final Player player, final Collection<RMAchievement> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        final DatabaseManagerAPI db = this.rm.getDatabaseManager();
        if (db == null) {
            return;
        }
        //only an online, loaded player can earn one, so the live cache is the right source here
        final RMPlayerStats stats = db.getStats(player.getUniqueId());
        if (stats == null) {
            return;
        }

        for (final RMAchievement achievement : candidates) {
            //">= goal" rather than "== goal", so an achievement is still handed out when a player
            //jumps straight past its goal
            if (achievement.isCompletedButNotUnlocked(stats)) {
                achievement.grant(player);
            }
        }
    }
}
