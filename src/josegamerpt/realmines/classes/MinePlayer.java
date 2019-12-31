package josegamerpt.realmines.classes;

import org.bukkit.entity.Player;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.managers.PlayerManager;
import josegamerpt.realmines.utils.Text;

public class MinePlayer {
	
	public SelectionBlock pos1;
	public SelectionBlock pos2;
	public Player player;
	
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
		player.sendMessage(RealMines.getPrefix() + Text.addColor(string));
	}
}
