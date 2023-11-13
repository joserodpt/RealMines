package joserodpt.realmines.util;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import com.google.common.base.Strings;
import joserodpt.realmines.config.Config;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    public static void send(final Player p, final String string) {
        p.sendMessage(Text.color(Config.file().getString("RealMines.Prefix") + string));
    }

    public static void send(final CommandSender p, final String string) {
        p.sendMessage(Text.color(Config.file().getString("RealMines.Prefix") + "&f" + string));
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
        return WordUtils.capitalizeFully(m.name().replace("_", " "));
    }

    public static String getProgressBar(final int current, final int max, final int totalBars, final char symbol, final ChatColor completedColor,
										final ChatColor notCompletedColor) {
        final float percent = (float) current / max;
        final int progressBars = (int) (totalBars * percent);

        return Strings.repeat(String.valueOf(completedColor) + symbol, progressBars)
                + Strings.repeat(String.valueOf(notCompletedColor) + symbol, totalBars - progressBars);
    }
}
