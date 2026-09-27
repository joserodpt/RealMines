package joserodpt.realmines.api.utils;

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

import com.google.common.base.Strings;
import joserodpt.realutils.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.List;

/**
 * RealMines' own formatting, next to the colouring and sending that RealUtils' {@link Text} does
 * for every Real* plugin.
 */
public class Format {

    public static String pluginPrefix = Text.color("&f&lReal&9&lMines");

    public static String getProgressBar(final int current, final int max, final int totalBars, final char symbol, final ChatColor completedColor, final ChatColor notCompletedColor) {
        if (max <= 0 || (current < 0 || totalBars < 0)) {
            return "&d" + symbol;
        }

        final float percent = (float) current / max;
        final int progressBars = (int) (totalBars * percent);
        final int remainingBars = totalBars - progressBars;

        if (progressBars < 0 || remainingBars < 0) {
            return "&d" + symbol;
        }

        return Strings.repeat(String.valueOf(completedColor) + symbol, progressBars)
                + Strings.repeat(String.valueOf(notCompletedColor) + symbol, remainingBars);
    }

    public static String location2Command(final Location l) {
        return l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ();
    }

    public static String formatEpoch(final long l) {
        return new java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new java.util.Date(l));
    }

    public static String formatPercentages(final double percentage) {
        // 0.12 -> 12%
        // 0.123 -> 12.3%
        // 0.1234 -> 12.34%
        return new DecimalFormat("#.##").format(percentage * 100);
    }

    /** Lists a mine item's break actions under its lore. */
    public static ItemStack addBreakActionsLore(final ItemStack i, final List<String> add) {
        final ItemMeta meta = i.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                lore.add("&6");
                lore.add("&fBreak Actions:");
                lore.addAll(add);
                meta.setLore(Text.color(lore));
                i.setItemMeta(meta);
            }
        }
        return i;
    }
}
