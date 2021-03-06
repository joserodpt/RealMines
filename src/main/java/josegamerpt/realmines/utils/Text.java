package josegamerpt.realmines.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import josegamerpt.realmines.RealMines;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Text {

	public static String color(final String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static void sendList(CommandSender p, List<String> list) {
		list.forEach(s -> p.sendMessage(color(s)));
	}

	public static ArrayList<String> color(List<?> list) {
		ArrayList<String> color = new ArrayList<>();
		list.forEach(o -> color.add(Text.color((String) o)));
		return color;
	}

	public static void send(Player p, String string) {
		p.sendMessage(Text.color(RealMines.getPrefix() + string));
	}

	public static void send(CommandSender p, String string) {
		p.sendMessage(Text.color(RealMines.getPrefix() + string));
	}

	public static String getProgressBar(int current, int max, int totalBars, char symbol, ChatColor completedColor,
								 ChatColor notCompletedColor) {
		float percent = (float) current / max;
		int progressBars = (int) (totalBars * percent);

		return Strings.repeat("" + completedColor + symbol, progressBars)
				+ Strings.repeat("" + notCompletedColor + symbol, totalBars - progressBars);
	}
}
