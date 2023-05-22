package josegamerpt.realmines.util;

import com.google.common.base.Strings;
import josegamerpt.realmines.RealMines;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Text {

    public static String color(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static void sendList(final CommandSender p, final List<String> list) {
        list.forEach(s -> p.sendMessage(color(s)));
    }

    public static ArrayList<String> color(final List<?> list) {
        final ArrayList<String> color = new ArrayList<>();
        list.forEach(o -> color.add(Text.color((String) o)));
        return color;
    }

    public static void send(final Player p, final String string) {
        p.sendMessage(Text.color(RealMines.getInstance().getPrefix() + string));
    }

    public static void send(final CommandSender p, final String string) {
        p.sendMessage(Text.color(RealMines.getInstance().getPrefix() + string));
    }

    public static String getProgressBar(final int current, final int max, final int totalBars, final char symbol, final ChatColor completedColor,
										final ChatColor notCompletedColor) {
        final float percent = (float) current / max;
        final int progressBars = (int) (totalBars * percent);

        return Strings.repeat(String.valueOf(completedColor) + symbol, progressBars)
                + Strings.repeat(String.valueOf(notCompletedColor) + symbol, totalBars - progressBars);
    }
}
