package josegamerpt.realmines.managers;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import josegamerpt.realmines.classes.MinePlayer;

public class PlayerManager {

	static ArrayList<MinePlayer> players = new ArrayList<>();

	public static void loadPlayer(Player player) {
		players.add(new MinePlayer(player));
	}

	public static MinePlayer get(Player player) {
		for (MinePlayer p : players) {
			return p.getPlayer().equals(player) ? p : null;
		}
		return null;
	}

	public static ArrayList<MinePlayer> getPlayers() {
		return players;
	}

    public static void unloadPlyer(MinePlayer minePlayer) {
		players.remove(minePlayer);
    }
}
