package josegamerpt.realmines.classes;

import java.util.Arrays;

import josegamerpt.realmines.utils.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import josegamerpt.realmines.utils.Itens;

public class MineBlockIcon {
	
	public MineBlock mb;
	public ItemStack i;
	public Boolean placeholder = false;

	public MineBlockIcon(MineBlock mib)
	{
		this.mb = mib;
		makeIcon();
	}

	public MineBlockIcon() {
		placeholder = true;
		i = Itens.createItemLore(Material.DEAD_BUSH, 1, "&9This mine has no blocks.", Arrays.asList("&fAdd a new block."));
	}

	private void makeIcon() {
		i = Itens.createItemLore(mb.material, 1, Text.color("&9" + mb.material.name()), Arrays.asList("&fPercentage: &b" + mb.getPercentage() + "%", "&fClick to edit the percentage.", "&f(Q) to remove this block."));
	}
}