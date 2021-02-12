package josegamerpt.realmines.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
		PlayerManager.unloadPlyer(PlayerManager.get(e.getPlayer()));
	}

}
