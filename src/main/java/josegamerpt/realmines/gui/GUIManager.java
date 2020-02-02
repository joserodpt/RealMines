package josegamerpt.realmines.gui;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import josegamerpt.realmines.classes.Enum.PickType;
import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.classes.Mine;
import josegamerpt.realmines.managers.MineManager;
import josegamerpt.realmines.managers.PlayerManager;
import josegamerpt.realmines.utils.GUIBuilder;
import josegamerpt.realmines.utils.GUIBuilder.ClickRunnable;
import josegamerpt.realmines.utils.Itens;
import josegamerpt.realmines.utils.PlayerInput;
import josegamerpt.realmines.utils.Text;
import josegamerpt.realmines.utils.PlayerInput.InputRunnable;

public class GUIManager {

	public static void openMine(Mine m, Player target) {
		GUIBuilder inventory = new GUIBuilder("§9" + m.name, 9, target.getUniqueId(),
				Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, ""));

		inventory.addItem(new ClickRunnable() {
							  public void run(InventoryClickEvent e) {
								  target.closeInventory();
								  MineBlocksViewer v = new MineBlocksViewer(target, m);
								  v.openInventory(target);
							  }
						  }, Itens.createItemLore(Material.CHEST, 1, "§bBlocks", Arrays.asList("&fClick here to open this category.")),
				0);

		inventory.addItem(new ClickRunnable() {
			public void run(InventoryClickEvent e) {
				target.closeInventory();
				MineResetMenu mrm = new MineResetMenu(target, m);
				mrm.openInventory(target);
			}
		}, Itens.createItemLore(Material.ANVIL, 1, "§6Resets", Arrays.asList("&fClick here to manage the resets.")), 1);

		inventory.addItem(new ClickRunnable() {
			public void run(InventoryClickEvent e) {
				target.closeInventory();
				MineManager.teleport(target, m, false);
			}
		}, Itens.createItemLore(Material.ENDER_PEARL, 1, "§1Teleport",
				Arrays.asList("&fClick here to teleport to this mine.")), 2);

		inventory.addItem(new ClickRunnable() {
			public void run(InventoryClickEvent e) {
				target.closeInventory();
				MaterialPicker s = new MaterialPicker(m, target, PickType.ICON);
				s.openInventory(target);
			}
		}, Itens.createItemLore(m.icon, 1, "§bIcon", Arrays.asList("&fClick here to select a new icon.")), 3);

		inventory.addItem(new ClickRunnable() {
			public void run(InventoryClickEvent e) {
				target.closeInventory();
				new PlayerInput(PlayerManager.searchPlayer(target), new InputRunnable() {
					@Override
					public void run(String s) {
						m.setName(s);
						GUIManager.openMine(m, target);
					}
				}, new InputRunnable() {
					@Override
					public void run(String s) {
						GUIManager.openMine(m, target);
					}
				});
			}
		}, Itens.createItemLore(Material.PAPER, 1, "§bName", Arrays.asList("&fClick here to change the name.")), 4);

		inventory.addItem(new ClickRunnable() {
			public void run(InventoryClickEvent e) {
				m.clear();
				target.sendMessage(RealMines.getPrefix() + Text.addColor("&fMine has been &acleared."));
			}
		}, Itens.createItemLore(Material.TNT, 1, "§6Clear", Arrays.asList("&fClick here to clear this mine.")), 5);

		// close

		inventory.addItem(new ClickRunnable() {
			public void run(InventoryClickEvent e) {
				target.closeInventory();
				MineViewer m = new MineViewer(target);
				m.openInventory(target);
			}
		}, Itens.createItemLore(Material.LECTERN, 1, "§cClose", Arrays.asList("&fClick to go back.")), 8);

		inventory.openInventory(target);
	}

}
