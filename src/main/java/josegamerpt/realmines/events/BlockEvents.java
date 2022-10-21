package josegamerpt.realmines.events;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mines.RMine;
import josegamerpt.realmines.utils.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import org.bukkit.event.block.SignChangeEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockEvents implements Listener {

	private final RealMines rm;
	private final String noSetting = Language.file().getString("Signs.Setting-Not-Found");
	private final String noMine = Language.file().getString("Signs.Mine-Not-Found");

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

			RMine m = RealMines.getInstance().getMineManager().get(name);

			if (m != null) {
				String modif = event.getLine(2);
				if (RealMines.getInstance().getMineManager().signset.contains(modif)) {
					m.addSign(event.getBlock(), modif);
					m.updateSigns();
				} else {
					String[] line = noSetting.split("\\|");
					event.setLine(1, Text.color(line[0]));
					event.setLine(2, Text.color(line[1]));
					event.setLine(3, Text.color(line[2]));
				}
			} else {
				String[] line = noMine.split("\\|");
				event.setLine(1, Text.color(line[0]));
				event.setLine(2, Text.color(line[1]));
				event.setLine(3, Text.color(line[2]));
			}
		}
	}
}
