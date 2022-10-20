package josegamerpt.realmines.mines.gui;

import java.util.ArrayList;
import java.util.List;

import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mines.RMine;
import josegamerpt.realmines.utils.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import josegamerpt.realmines.utils.Items;

public class MineIcon {
	
	private RMine m;
	private ItemStack i;
	private Boolean placeholder = false;

	public MineIcon(RMine min)
	{
		this.m = min;
		makeIcon();
	}

	public MineIcon() {
		this.placeholder = true;
		this.i = Items.createItemLore(Material.DEAD_BUSH, 1, Language.file().getString("GUI.Items.No-Mines-Found.Name"), Language.file().getStringList("GUI.Items.No-Mines-Found.Description"));
	}

	private void makeIcon() {
		this.i = Items.createItemLore(m.getIcon(), 1, m.getColorIcon() + " &6&l" + m.getDisplayName() + " &f- &b&l" + m.getType().name(), var(m));
	}

	private List<String> var(RMine m) {
		List<String> ret = new ArrayList<>();
		Language.file().getStringList("GUI.Items.Mine.Description").forEach(s -> ret.add(Text.color(s.replaceAll("%remainingblocks%", m.getRemainingBlocks() + "").replaceAll("%totalblocks%", m.getBlockCount() + "").replaceAll("%bar%", getBar(m)))));
		return ret;
	}

	private String getBar(RMine m) {
		return Text.getProgressBar(m.getRemainingBlocks(), m.getBlockCount(), 10, '■', ChatColor.GREEN, ChatColor.RED);
	}


	public boolean isPlaceholder() {
		return this.placeholder;
	}

	public RMine getMine() {
		return this.m;
	}

	public ItemStack getIcon() {
		return this.i;
	}
}