package josegamerpt.realmines.classes;

import josegamerpt.realmines.utils.CubeVisualizer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.managers.PlayerManager;
import josegamerpt.realmines.utils.Text;

public class MinePlayer {
	
	public Location pos1;
	public Location pos2;
	public Player player;
	public CubeVisualizer cb = new CubeVisualizer(this);
	
	public MinePlayer(Player p)
	{
		this.player = p;
	}
	
	public void save()
	{
		PlayerManager.players.add(this);
	}
	
	public void clearSelection()
	{
		pos1 = null;
		pos2 = null;
	}

	public void sendMessage(String string) {
		player.sendMessage(RealMines.getPrefix() + Text.color(string));
	}
}
