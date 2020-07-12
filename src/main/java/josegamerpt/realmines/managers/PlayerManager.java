package josegamerpt.realmines.managers;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import josegamerpt.realmines.classes.MinePlayer;

public class PlayerManager {

	public static ArrayList<MinePlayer> players = new ArrayList<MinePlayer>();

	public static void loadPlayer(Player player) {
		MinePlayer mp = new MinePlayer(player);
		mp.save();
	}

	public static MinePlayer get(Player player) {
		for (MinePlayer p : players) {
			if (p.player.equals(player))
			{
				return p;
			}
		}
		return null;
	}
}
