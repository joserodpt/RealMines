package josegamerpt.realmines.mine.gui;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mine.component.MineBlock;
import josegamerpt.realmines.util.Items;
import josegamerpt.realmines.util.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

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
        this.i = Items.createItemLore(this.mb.getMaterial(), 1, Language.file().getString("GUI.Items.Mine-Block.Block.Name").replace("%material%", this.getMineBlock().getMaterial().name()), this.makeDescription(this.mb));
    }

    public List<String> makeDescription(MineBlock mb) {
        return Language.file().getStringList("GUI.Items.Mine-Block.Block.Description")
                .stream()
                .map(s -> Text.color(s.replaceAll("%percentage%", String.valueOf(mb.getPercentage() * 100))))
                .collect(Collectors.toList());
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