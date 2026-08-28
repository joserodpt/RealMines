package joserodpt.realmines.api.achievements;

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

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.database.RMPlayerStats;
import joserodpt.realmines.api.event.PlayerAchievementUnlockEvent;
import joserodpt.realmines.api.managers.DatabaseManagerAPI;
import joserodpt.realmines.api.mine.components.actions.MineAction;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * One goal a player can work towards by mining, and whatever they get for reaching it.
 */
public class RMAchievement {

    private final String id, displayName;
    private final List<String> description;
    private final Material icon;
    private final RMAchievementType type;
    private final Material material;
    private final long goal;
    private final boolean announce;
    private final List<MineAction> rewards;

    public RMAchievement(final String id, final String displayName, final List<String> description, final Material icon,
                         final RMAchievementType type, final Material material, final long goal, final boolean announce,
                         final List<MineAction> rewards) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.type = type;
        this.material = material;
        this.goal = Math.max(1L, goal);
        this.announce = announce;
        this.rewards = rewards;
    }

    public String getID() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Material getIcon() {
        return this.icon;
    }

    public RMAchievementType getType() {
        return this.type;
    }

    /**
     * The material being counted, or null when this achievement counts every block.
     */
    public Material getMaterial() {
        return this.material;
    }

    public long getGoal() {
        return this.goal;
    }

    public boolean shouldAnnounce() {
        return this.announce;
    }

    public List<MineAction> getRewards() {
        return this.rewards;
    }

    /**
     * How many blocks this player has mined towards the goal, uncapped.
     */
    public long getRawProgress(final RMPlayerStats stats) {
        if (stats == null) {
            return 0L;
        }
        return this.type == RMAchievementType.MATERIAL
                ? stats.getBlocksMined(this.material)
                : stats.getTotalBlocksMined();
    }

    /**
     * The same as {@link #getRawProgress(RMPlayerStats)} but never above the goal, for display.
     */
    public long getProgress(final RMPlayerStats stats) {
        return Math.min(getRawProgress(stats), this.goal);
    }

    public boolean isUnlocked(final RMPlayerStats stats) {
        return stats != null && stats.hasAchievement(this.id);
    }

    /**
     * Whether this player has reached the goal but hasn't been given the achievement yet.
     */
    public boolean isCompletedButNotUnlocked(final RMPlayerStats stats) {
        return !isUnlocked(stats) && getRawProgress(stats) >= this.goal;
    }

    /**
     * Hands this achievement to a player: fires the event, runs the rewards, tells them about it and
     * records it so it can never be given twice.
     */
    public void grant(final Player player) {
        final DatabaseManagerAPI db = RealMinesAPI.getInstance().getDatabaseManager();
        if (db == null) {
            return;
        }
        final RMPlayerStats stats = db.getStats(player.getUniqueId());
        if (stats == null || stats.hasAchievement(this.id)) {
            return;
        }

        final PlayerAchievementUnlockEvent event = new PlayerAchievementUnlockEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        //recorded before the rewards run, so a reward that throws can't hand the achievement out twice
        db.grantAchievement(player.getUniqueId(), this.id);

        Text.send(player, TranslatableLine.ACHIEVEMENT_UNLOCKED
                .setV1(TranslatableLine.ReplacableVar.NAME.eq(this.displayName)).get());

        for (final MineAction reward : this.rewards) {
            try {
                reward.execute(player, player.getLocation());
            } catch (final Exception e) {
                RealMinesAPI.getInstance().getLogger().warning("Reward " + reward.getID() + " of achievement "
                        + this.id + " failed: " + e.getMessage());
            }
        }

        if (this.announce) {
            Bukkit.broadcastMessage(TranslatableLine.ACHIEVEMENT_BROADCAST
                    .setV1(TranslatableLine.ReplacableVar.NAME.eq(this.displayName))
                    .setV2(TranslatableLine.ReplacableVar.OBJECT.eq(player.getName())).get());
        }
    }

    /**
     * The description lines with their placeholders filled in.
     */
    public List<String> getDescription() {
        final List<String> out = new ArrayList<>();
        for (final String line : this.description) {
            out.add(Text.color(line
                    .replace("%goal%", Text.formatNumber(this.goal))
                    .replace("%material%", this.material == null ? "" : Text.beautifyMaterialName(this.material))));
        }
        return out;
    }

    /**
     * The GUI icon as this player sees it: glowing with the rewards listed once unlocked, otherwise
     * plain with a progress bar.
     */
    public ItemStack getItem(final RMPlayerStats stats) {
        final boolean unlocked = isUnlocked(stats);

        final List<String> lore = new ArrayList<>(getDescription());
        lore.add("");

        if (unlocked) {
            lore.addAll(Text.color(RMLanguageConfig.file().getStringList("GUI.Items.Achievement-Unlocked.Description")));
        } else {
            final long progress = getProgress(stats);
            //the bar takes ints, and a goal can be configured beyond what one holds
            final int barGoal = (int) Math.min(this.goal, Integer.MAX_VALUE);
            final int barProgress = (int) Math.min(progress, barGoal);
            final String bar = Text.getProgressBar(barProgress, barGoal, 20, '|', ChatColor.GREEN, ChatColor.GRAY);
            for (final String line : RMLanguageConfig.file().getStringList("GUI.Items.Achievement-Locked.Description")) {
                lore.add(Text.color(line
                        .replace("%bar%", bar)
                        .replace("%value%", Text.formatNumber(progress) + " / " + Text.formatNumber(this.goal))));
            }
        }

        if (!this.rewards.isEmpty()) {
            lore.add("");
            lore.add(Text.color(RMLanguageConfig.file().getString("GUI.Items.Achievement-Rewards.Name")));
            for (final MineAction reward : this.rewards) {
                lore.add(Text.color("&8- " + reward.getType().getShortName() + "&r&f: " + reward.getValueString()));
            }
        }

        return unlocked
                ? Items.createItemLoreEnchanted(this.icon, 1, this.displayName, lore)
                : Items.createItem(this.icon, 1, this.displayName, lore);
    }

    @Override
    public String toString() {
        return "RMAchievement{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", material=" + material +
                ", goal=" + goal +
                '}';
    }
}
