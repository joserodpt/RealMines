package josegamerpt.realmines.events;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.utils.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import josegamerpt.realmines.managers.PlayerManager;

public class PlayerEvents implements Listener {
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		PlayerManager.loadPlayer(e.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		PlayerManager.players.remove(PlayerManager.get(e.getPlayer()));
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e){
		if (e.getItemDrop().getItemStack().equals(RealMines.SelectionTool)) {
			PlayerManager.get(e.getPlayer()).clearSelection();
			Text.send(e.getPlayer(), "&fSelection &9cleared.");
			e.setCancelled(true);
		}
	}
}
