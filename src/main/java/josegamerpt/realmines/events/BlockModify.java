package josegamerpt.realmines.events;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import josegamerpt.realmines.managers.MineManager;

public class BlockModify implements Listener {

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block b = event.getBlock();
		MineManager.findBlockBreak(b);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Block b = event.getBlock();
		MineManager.findBlockBreak(b);
	}

	@EventHandler
	public void mineBlockBreak(MineBlockBreakEvent e) {
		MineManager.despertar(e.getMine());
	}
}
