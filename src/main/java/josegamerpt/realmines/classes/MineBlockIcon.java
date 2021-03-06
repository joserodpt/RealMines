package josegamerpt.realmines.classes;

import java.util.ArrayList;
import java.util.List;

import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.utils.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import josegamerpt.realmines.utils.Itens;

public class MineBlockIcon {
	
	private MineBlock mb;
	private ItemStack i;
	private Boolean placeholder = false;

	public MineBlockIcon(MineBlock mib)
	{
		this.mb = mib;
		makeIcon();
	}

	public MineBlockIcon() {
		placeholder = true;
		i = Itens.createItemLore(Material.DEAD_BUSH, 1, Language.file().getString("GUI.Items.Mine-Block.No-Blocks.Name"), Language.file().getStringList("GUI.Items.Mine-Block.No-Blocks.Description"));
	}

	private void makeIcon() {
		i = Itens.createItemLore(mb.getMaterial(), 1, Language.file().getString("GUI.Items.Mine-Block.Block.Name").replace("%material%", getMineBlock().getMaterial().name()), var(mb));
	}

	private List<String> var(MineBlock mb) {
		List<String> ret = new ArrayList<>();
		Language.file().getStringList("GUI.Items.Mine-Block.Block.Description").forEach(s -> ret.add(Text.color(s.replaceAll("%percentage%", mb.getPercentage() + ""))));
		return ret;
	}

	public MineBlock getMineBlock() {
		return this.mb;
	}

	public boolean isPlaceholder() {
		return this.placeholder;
	}

	public ItemStack getItemStack() {
		return this.i;
	}
}