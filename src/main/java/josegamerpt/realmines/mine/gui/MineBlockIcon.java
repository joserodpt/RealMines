package josegamerpt.realmines.mine.gui;

import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mine.components.MineBlock;
import josegamerpt.realmines.util.Items;
import josegamerpt.realmines.util.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MineBlockIcon {

    private MineBlock mb;
    private ItemStack i;
    private Boolean placeholder = false;

    public MineBlockIcon(final MineBlock mib) {
        this.mb = mib;
        this.makeIcon();
    }

    public MineBlockIcon() {
        this.placeholder = true;
        this.i = Items.createItemLore(Material.DEAD_BUSH, 1, Language.file().getString("GUI.Items.Mine-Block.No-Blocks.Name"), Language.file().getStringList("GUI.Items.Mine-Block.No-Blocks.Description"));
    }

    private void makeIcon() {
        this.i = Items.createItemLore(this.mb.getMaterial(), 1, Language.file().getString("GUI.Items.Mine-Block.Block.Name").replace("%material%", this.getMineBlock().getMaterial().name()), this.var(this.mb));
    }

    private List<String> var(final MineBlock mb) {
        final List<String> ret = new ArrayList<>();
        Language.file().getStringList("GUI.Items.Mine-Block.Block.Description").forEach(s -> ret.add(Text.color(s.replaceAll("%percentage%", String.valueOf(mb.getPercentage() * 100)))));
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