package josegamerpt.realmines.classes;

import josegamerpt.realmines.utils.CubeVisualizer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.managers.PlayerManager;
import josegamerpt.realmines.utils.Text;

public class MinePlayer {
	private Player player;
	private CubeVisualizer cb = new CubeVisualizer(this);
	
	public MinePlayer(Player p)
	{
		this.player = p;
	}

	public void sendMessage(String string) {
		player.sendMessage(RealMines.getPrefix() + Text.color(string));
	}

	public Player getPlayer() {
		return this.player;
	}

	public CubeVisualizer getCube()
	{
		return this.cb;
	}
}
