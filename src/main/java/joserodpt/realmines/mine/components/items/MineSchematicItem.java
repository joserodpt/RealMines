package joserodpt.realmines.mine.components.items;

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

import joserodpt.realmines.config.Language;
import joserodpt.realmines.mine.components.actions.MineAction;
import joserodpt.realmines.utils.Items;
import joserodpt.realmines.utils.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class MineSchematicItem extends MineItem {

    public MineSchematicItem(final Material m) {
        super(m, 1D);
    }

    public MineSchematicItem(Material m, Boolean disabledVanillaDrop, List<MineAction> actionsList) {
        super(m, 1D, disabledVanillaDrop, actionsList, true);
    }

    @Override
    public ItemStack getItem() {
        return Items.createItemLore(super.getMaterial(), 1, Language.file().getString("GUI.Items.Mine-Block.Schematic-Block.Name").replace("%material%", Text.beautifyMaterialName(super.getMaterial())) + (super.disabledVanillaDrop() ? " &c&lNo-DROP" : ""), Language.file().getStringList("GUI.Items.Mine-Block.Schematic-Block.Description")
                .stream()
                .map(Text::color)
                .collect(Collectors.toList()));
    }

    @Override
    public Type getType() {
        return Type.SCHEMATIC_BLOCK;
    }
}