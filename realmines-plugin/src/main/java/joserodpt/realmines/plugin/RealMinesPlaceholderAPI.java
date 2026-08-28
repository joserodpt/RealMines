package joserodpt.realmines.plugin;

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
import joserodpt.realmines.api.managers.DatabaseManagerAPI;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.utils.Countdown;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RealMinesPlaceholderAPI extends PlaceholderExpansion {

    private final RealMines plugin;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin The instance of our plugin.
     */
    public RealMinesPlaceholderAPI(final RealMines plugin) {
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    @NotNull
    public String getAuthor() {
        return this.plugin.getPlugin().getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>The identifier has to be lowercase and can't contain _ or %
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    @NotNull
    public String getIdentifier() {
        return "realmines";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     * <p>
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    @NotNull
    public String getVersion() {
        return this.plugin.getPlugin().getDescription().getVersion();
    }

    @Override
    public String onRequest(final OfflinePlayer player, final String identifier) {
        final int mineIndex = 1;

        //player scoped placeholders are matched first, so they can never be shadowed by the
        //startsWith chain of the mine scoped ones below
        if (identifier.startsWith("stats_") || identifier.startsWith("achievements_") || identifier.startsWith("top_")) {
            return onPlayerRequest(player, identifier);
        }

        if (identifier.startsWith("totalblocks")) {
            final String[] split = identifier.split("_");
            final String mine = split[mineIndex];
            final RMine m = this.plugin.getMineManager().getMine(mine);
            if (m != null) {
                return String.valueOf(m.getBlockCount());
            } else {
                return "No mine named: " + mine;
            }
        }

        if (identifier.startsWith("minedblocks")) {
            final String[] split = identifier.split("_");
            final String mine = split[mineIndex];
            final RMine m = this.plugin.getMineManager().getMine(mine);
            if (m != null) {
                return String.valueOf(m.getMinedBlocks());
            } else {
                return "No mine named: " + mine;
            }
        }

        if (identifier.startsWith("remainingblocks")) {
            final String[] split = identifier.split("_");
            final String mine = split[mineIndex];
            final RMine m = this.plugin.getMineManager().getMine(mine);
            if (m != null) {
                return String.valueOf(m.getRemainingBlocks());
            } else {
                return "No mine named: " + mine;
            }
        }

        if (identifier.startsWith("perremainingblocks")) {
            final String[] split = identifier.split("_");
            final String mine = split[mineIndex];
            final RMine m = this.plugin.getMineManager().getMine(mine);
            if (m != null) {
                return String.valueOf(m.getRemainingBlocksPer());
            } else {
                return "No mine named: " + mine;
            }
        }

        if (identifier.startsWith("perminedblocks")) {
            final String[] split = identifier.split("_");
            final String mine = split[mineIndex];
            final RMine m = this.plugin.getMineManager().getMine(mine);
            if (m != null) {
                return String.valueOf(m.getMinedBlocksPer());
            } else {
                return "No mine named: " + mine;
            }
        }

        if (identifier.startsWith("secondsleft")) {
            final String[] split = identifier.split("_");
            final String mine = split[mineIndex];
            final RMine m = this.plugin.getMineManager().getMine(mine);
            if (m != null) {
                final Integer secondsLeft = m.getCountdown();
                if (secondsLeft == null) {
                    return "-1";
                }
                return Integer.toString(secondsLeft);
            } else {
                return "No mine named: " + mine;
            }
        }

        if (identifier.startsWith("timeleft")) {
            final String[] split = identifier.split("_");
            final String mine = split[mineIndex];
            final RMine m = this.plugin.getMineManager().getMine(mine);
            if (m != null) {
                final Integer secondsLeft = m.getCountdown();
                if (secondsLeft == null) {
                    return "-1";
                }
                return Countdown.format(secondsLeft * 1000L);
            } else {
                return "No mine named: " + mine;
            }
        }

        if (identifier.startsWith("bar")) {
            final String[] split = identifier.split("_");
            final String mine = split[mineIndex];
            final RMine m = this.plugin.getMineManager().getMine(mine);
            if (m != null) {
                return m.getBar();
            } else {
                return "No mine named: " + mine;
            }
        }

        if (identifier.startsWith("percentage_bar")) {
            final String[] split = identifier.split("_");
            final String mine = split[mineIndex];
            final RMine m = this.plugin.getMineManager().getMine(mine);
            if (m != null) {
                return m.getPercentageBar();
            } else {
                return "No mine named: " + mine;
            }
        }

        return null;
    }

    /**
     * Stats, achievements and leaderboard placeholders.
     * <p>
     * Placeholders get asked for on every scoreboard tick, so none of this is allowed to touch the
     * database. Player stats come from the in memory cache, which means an offline player reports
     * nothing, and the leaderboard comes from the snapshot refreshed in the background.
     */
    private String onPlayerRequest(final OfflinePlayer player, final String identifier) {
        final DatabaseManagerAPI db = this.plugin.getDatabaseManager();
        if (db == null) {
            return "";
        }

        //%realmines_top_name_<n>% and %realmines_top_value_<n>%
        if (identifier.startsWith("top_name_") || identifier.startsWith("top_value_")) {
            final boolean wantsName = identifier.startsWith("top_name_");
            final String rest = identifier.substring(wantsName ? "top_name_".length() : "top_value_".length());

            //the position is always the last segment; anything before it is an optional material.
            //split on the last underscore rather than on all of them, because material names have
            //underscores of their own (DIAMOND_ORE)
            final int lastSeparator = rest.lastIndexOf('_');
            final int position;
            try {
                position = Integer.parseInt(rest.substring(lastSeparator + 1));
            } catch (final NumberFormatException e) {
                return null;
            }
            if (position < 1) {
                return null;
            }

            //%realmines_top_name_<MATERIAL>_<n>% ranks one block instead of the overall total
            final Material material = lastSeparator < 0 ? null : Material.matchMaterial(rest.substring(0, lastSeparator));
            if (material != null) {
                final List<RMPlayerBlockStat> top = db.getTopBlocksMined(material, position);
                if (top.size() < position) {
                    return "";
                }
                final RMPlayerBlockStat stat = top.get(position - 1);
                return wantsName ? String.valueOf(stat.getPlayerName()) : String.valueOf(stat.getAmount());
            }

            final List<RMPlayerData> top = db.getTopTotalBlocksMined(position);
            if (top.size() < position) {
                return "";
            }
            final RMPlayerData data = top.get(position - 1);
            return wantsName ? String.valueOf(data.getName()) : String.valueOf(data.getTotalBlocksMined());
        }

        //everything below is about one player, and only a loaded one can be answered without
        //blocking, so an offline player reports empty rather than stalling the main thread
        if (player == null) {
            return "";
        }
        final RMPlayerStats stats = db.getStats(player.getUniqueId());
        if (stats == null) {
            return "";
        }

        if (identifier.equals("stats_totalmined")) {
            return String.valueOf(stats.getTotalBlocksMined());
        }

        //%realmines_stats_mined_<MATERIAL>%
        if (identifier.startsWith("stats_mined_")) {
            final Material material = Material.matchMaterial(identifier.substring("stats_mined_".length()));
            if (material == null) {
                return "0";
            }
            return String.valueOf(stats.getBlocksMined(material));
        }

        if (identifier.equals("achievements_unlocked")) {
            return String.valueOf(this.plugin.getAchievementsManager().getUnlockedCount(stats));
        }

        if (identifier.equals("achievements_total")) {
            return String.valueOf(this.plugin.getAchievementsManager().getAchievements().size());
        }

        if (identifier.equals("achievements_percentage")) {
            final int total = this.plugin.getAchievementsManager().getAchievements().size();
            if (total == 0) {
                return "0";
            }
            return String.valueOf(Math.round(this.plugin.getAchievementsManager().getUnlockedCount(stats) * 100D / total));
        }

        return null;
    }
}