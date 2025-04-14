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
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.utils.Countdown;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

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
                if (m.getMineTimer().getCountdown() == null) {
                    return "-1";
                }
                return Integer.toString(m.getMineTimer().getCountdown().getSecondsLeft());
            } else {
                return "No mine named: " + mine;
            }
        }

        if (identifier.startsWith("timeleft")) {
            final String[] split = identifier.split("_");
            final String mine = split[mineIndex];
            final RMine m = this.plugin.getMineManager().getMine(mine);
            if (m != null) {
                if (m.getMineTimer().getCountdown() == null) {
                    return "-1";
                }
                return Countdown.format(m.getMineTimer().getCountdown().getSecondsLeft() * 1000L);
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
}