package josegamerpt.realmines.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import josegamerpt.realmines.managers.MineManager;

public class BlockModify implements Listener {

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		MineManager.findBlockUpdate(e.getBlock());
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		MineManager.findBlockUpdate(e.getBlock());
	}

	@EventHandler
	public void mineBlockBreak(MineBlockBreakEvent e) {
		MineManager.despertar(e.getMine());
	}
}
