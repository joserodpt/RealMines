package josegamerpt.realmines.classes;

import java.util.Arrays;
import java.util.Collections;

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
		i = Itens.createItemLore(Material.DEAD_BUSH, 1, "&9No Mines Found", Collections.singletonList("&fCreate a new mine with /rm crate <name>."));
	}

	private void makeIcon() {
		i = Itens.createItemLore(m.getIcon(), 1, "&9" + m.getDisplayName(), Arrays.asList("&b" + m.getRemainingBlocks() + "&f/&b" + m.getBlockCount() + "&f blocks", "&fClick to edit this mine."));
	}
}