package josegamerpt.realmines.gui;

import java.util.Arrays;
import java.util.Collections;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import josegamerpt.realmines.classes.Mine;
import josegamerpt.realmines.managers.MineManager;
import josegamerpt.realmines.managers.PlayerManager;
import josegamerpt.realmines.utils.GUIBuilder;
import josegamerpt.realmines.utils.Itens;
import josegamerpt.realmines.utils.PlayerInput;
import josegamerpt.realmines.utils.Text;

public class GUIManager {

	public static void openMine(Mine m, Player target) {
		GUIBuilder inventory = new GUIBuilder(Text.color(m.getDisplayName()), 9, target.getUniqueId(),
				Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, ""));

		inventory.addItem(e -> {
			target.closeInventory();
			MineBlocksViewer v = new MineBlocksViewer(target, m);
			v.openInventory(target);
		}, Itens.createItemLore(Material.CHEST, 1, "&9Blocks", Collections.singletonList("&fClick here to open this category.")),
				0);

		inventory.addItem(e -> {
			target.closeInventory();
			MineResetMenu mrm = new MineResetMenu(target, m);
			mrm.openInventory(target);
		}, Itens.createItemLore(Material.ANVIL, 1,"&6Resets", Collections.singletonList("&fClick here to manage the resets.")), 1);

		inventory.addItem(e -> {
			target.closeInventory();
			MineManager.teleport(target, m, false);
		}, Itens.createItemLore(Material.ENDER_PEARL, 1, "&5Teleport",
				Collections.singletonList("&fClick here to teleport to this mine.")), 2);

		inventory.addItem(e -> {
			target.closeInventory();
			MaterialPicker s = new MaterialPicker(m, target, MaterialPicker.PickType.ICON);
			s.openInventory(target);
		}, Itens.createItemLore(m.getIcon(), 1, "&bIcon", Collections.singletonList("&fClick here to select a new icon.")), 3);

		inventory.addItem(e -> {
			target.closeInventory();
			new PlayerInput(PlayerManager.get(target), s -> {
				m.setDisplayName(s);
				GUIManager.openMine(m, target);
			}, s -> GUIManager.openMine(m, target));
		}, Itens.createItemLore(Material.PAPER, 1, "&aName", Collections.singletonList("&fClick here to change the name.")), 4);

		inventory.addItem(e -> {
			m.clear();
			Text.send(target, "&fMine has been &acleared.");
		}, Itens.createItemLore(Material.TNT, 1, "&cClear", Collections.singletonList("&fClick here to clear this mine.")), 5);

		inventory.addItem(e -> m.reset(), Itens.createItemLore(Material.DROPPER, 1, "&4Reset", Collections.singletonList("&fClick here to reset this mine.")), 6);

		inventory.addItem(e -> m.setHighlight(!m.isHighlighted()), Itens.createItemLore(Material.TORCH, 1, "&eBoundaries", Arrays.asList("&fClick to see the boundaries of the mine.")), 7);

		inventory.addItem(e -> {
			target.closeInventory();
			MineViewer m1 = new MineViewer(target);
			m1.openInventory(target);
		}, Itens.createItemLore(Material.RED_BED, 1, "&cClose", Collections.singletonList("&fClick to go back.")), 8);

		inventory.openInventory(target);
	}

}
