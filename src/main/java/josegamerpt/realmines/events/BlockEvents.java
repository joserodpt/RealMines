package josegamerpt.realmines.events;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.mines.Mine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import org.bukkit.event.block.SignChangeEvent;

public class BlockEvents implements Listener {

	private final RealMines rm;

	public BlockEvents(RealMines rm)
	{
		this.rm = rm;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		RealMines.getInstance().getMineManager().findBlockUpdate(e.getBlock());
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		RealMines.getInstance().getMineManager().findBlockUpdate(e.getBlock());
	}

	@EventHandler
	public void mineBlockBreak(MineBlockBreakEvent e) {
		RealMines.getInstance().getMineManager().resetPercentage(e.getMine());
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (event.getLine(0).contains("[realmines]") || event.getLine(0).contains("[RealMines]")) {
			event.setLine(0, this.rm.getPrefix());
			String name = event.getLine(1);

			Mine m = RealMines.getInstance().getMineManager().get(name);

			if (m != null) {
				String modif = event.getLine(2);
				if (RealMines.getInstance().getMineManager().signset.contains(modif)) {
					m.addSign(event.getBlock(), modif);
					m.updateSigns();
				} else {
					event.setLine(1, "§4Setting not");
					event.setLine(2, "§4found");
				}
			} else {
				event.setLine(1, "§4Mine not");
				event.setLine(2, "§4found");
			}
		}
	}
}
