package josegamerpt.realmines.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.classes.Mine;
import josegamerpt.realmines.classes.MinePlayer;
import josegamerpt.realmines.classes.SelectionBlock;
import josegamerpt.realmines.managers.MineManager;
import josegamerpt.realmines.managers.PlayerManager;

public class BlockInteractions implements Listener {

	@EventHandler
	public void onUseEvent(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK
				&& player.getInventory().getItemInMainHand().equals(RealMines.SelectionTool)) {
			MinePlayer p = PlayerManager.searchPlayer(player);

			if (p.pos2 != null) {
				p.pos2.revert();
			}
			p.pos2 = new SelectionBlock(e.getClickedBlock().getLocation(), e.getClickedBlock().getType());
			p.sendMessage("&9POS2 &fselected (&bX: " + e.getClickedBlock().getX() + " Y: " + e.getClickedBlock().getY()
					+ " Z: " + e.getClickedBlock().getZ() + "&f)");
			return;

		}
		if (e.getAction() == Action.LEFT_CLICK_BLOCK
				&& player.getInventory().getItemInMainHand().equals(RealMines.SelectionTool)) {

			e.setCancelled(true);

			MinePlayer p = PlayerManager.searchPlayer(player);

			if (p.pos1 != null) {
				p.pos1.revert();
			}
			p.pos1 = new SelectionBlock(e.getClickedBlock().getLocation(), e.getClickedBlock().getType());
			p.sendMessage("&9POS1 &fselected (&bX: " + e.getClickedBlock().getX() + " Y: " + e.getClickedBlock().getY()
					+ " Z: " + e.getClickedBlock().getZ() + "&f)");

		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (event.getLine(0).contains("[realmines]") || event.getLine(0).contains("[RealMines]")) {
			event.setLine(0, "§7[§9Real§bMines§7]");
			String name = event.getLine(1);
			if (MineManager.exists(name) == true) {
				String modif = event.getLine(2);
				if (MineManager.signset.contains(modif)) {
					Mine m = MineManager.getMine(name);
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