package josegamerpt.realmines.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import josegamerpt.realmines.RealMines;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Text {

	public static String color(final String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static String rainbow(final String original) {
		final char[] chars = { 'c', '6', 'e', 'a', 'b', '3', 'd' };
		int index = 0;
		String returnValue = "";
		char[] charArray;
		for (int length = (charArray = original.toCharArray()).length, i = 0; i < length; ++i) {
			final char c = charArray[i];
			returnValue = String.valueOf(returnValue) + "&" + chars[index] + c;
			if (++index == chars.length) {
				index = 0;
			}
		}
		return ChatColor.translateAlternateColorCodes('&', returnValue);
	}

	public static void sendList(Player p, ArrayList<String> list) {
		for (String s : list) {
			p.sendMessage(color(s));
		}
	}
	
	public static void sendList(Player p, ArrayList<String> list, Object var) {
		for (String s : list) {
			p.sendMessage(color(s).replace("%CAGES%", var + ""));
		}
	}
	
	public static void sendList(Player p, List<String> list) {
		for (String s : list) {
			p.sendMessage(color(s));
		}
	}


	public static ArrayList<String> color(List<?> list) {
		ArrayList<String> color = new ArrayList<String>();
		for (Object s : list) {
			color.add(Text.color((String) s));
		}
		return color;
	}

	public static CharSequence makeSpace() {
		return Text.color(randSp() + randSp() + randSp());
	}
	
	private static String randSp() 
    { 
        Random rand = new Random(); 
        List<String> sp = Arrays.asList("&6", "&7", "&8", "&9", "&5", "&f", "&e", "&a", "&b");
        return sp.get(rand.nextInt(sp.size())); 
    }

	public static void send(Player p, String string) {
		p.sendMessage(Text.color(RealMines.getPrefix() + string));
	} 
	
	public static void sendActionBar(Player p, String s)
	{
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(color(s)));
	}
}
