package josegamerpt.realmines.classes;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import josegamerpt.realmines.utils.Itens;

public class MineIcon {
	
	public Mine m;
	public ItemStack i;
	public Boolean placeholder = false;

	public MineIcon(Mine min)
	{
		this.m = min;
		makeIcon();
	}

	public MineIcon() {
		placeholder = true;
		i = Itens.createItemLore(Material.DEAD_BUSH, 1, "&9No Mines Found", Arrays.asList("&fCreate a new mine with /rm crate <name>."));
	}

	private void makeIcon() {
		i = Itens.createItemLore(m.icon, 1, "&9" + m.name, Arrays.asList("§b" + m.getRemainingBlocks() + "&f/&b" + m.getBlockCount() + "&f blocks", "&fClick to edit this mine."));
	}
}