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
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import com.google.common.base.Strings;
import joserodpt.realmines.api.config.RMConfig;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Text {

    public static String pluginPrefix = color("&f&lReal&9&lMines");

    public static String color(final String string) {
        if (string == null) {
            return "";
        }
        return string.isEmpty() ? "" : ChatColor.translateAlternateColorCodes('&', string);
    }

    public static void sendList(final CommandSender p, final List<String> list) {
        list.forEach(s -> p.sendMessage(color(s)));
    }

    public static List<String> color(final List<?> list) {
        final List<String> color = new ArrayList<>();
        list.forEach(o -> color.add(Text.color((String) o)));
        return color;
    }

    public static void send(final CommandSender p, final String string) {
        p.sendMessage(getPrefix() + Text.color("&f" + string));
    }

    public static String formatNumber(double number) {
        String[] suffixes = {"", "k", "M", "T"};
        int index = 0;
        while (number >= 1_000 && index < suffixes.length - 1) {
            number /= 1_000;
            ++index;
        }
        return new DecimalFormat("#.#").format(number) + suffixes[index];
    }

    public static String beautifyMaterialName(Material m) {
        if (m == null) {
            return "Unknown";
        }

        String[] parts = m.name().split("_");
        StringBuilder formattedString = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                formattedString.append(part.substring(0, 1).toUpperCase());
                if (part.length() > 1) {
                    formattedString.append(part.substring(1).toLowerCase());
                }
            }
            formattedString.append(" ");
        }
        return formattedString.toString().trim();
    }

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

    public static String location2Command(Location l) {
        return l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ();
    }

    public static String getPrefix() {
        return color(RMConfig.file().getString("RealMines.Prefix"));
    }

    public static String formatEpoch(long l) {
        return new java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new java.util.Date(l));
    }

    public static String formatPercentages(double percentage) {
        // 0.12 -> 12%
        // 0.123 -> 12.3%
        // 0.1234 -> 12.34%
        return new DecimalFormat("#.##").format(percentage * 100);
    }
}
