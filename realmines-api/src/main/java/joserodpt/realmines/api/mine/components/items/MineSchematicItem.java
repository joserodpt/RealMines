package joserodpt.realmines.api.mine.components.items;

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

import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.components.actions.MineAction;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class MineSchematicItem extends MineItem {

    public MineSchematicItem(final Material m) {
        super(m, 1D);
    }

    public MineSchematicItem(final Material m, final Boolean disabledVanillaDrop, final Boolean disabledBlockMining, final List<MineAction> actionsList) {
        super(m, 1D, disabledVanillaDrop, disabledBlockMining, actionsList, true);
    }

    @Override
    public ItemStack getItem() {
        return Items.createItem(super.getMaterial(), 1, TranslatableLine.GUI_SCHEMATIC_BLOCK_NAME.setV1(TranslatableLine.ReplacableVar.MATERIAL.eq(Text.beautifyMaterialName(super.getMaterial()))).get() + (super.areVanillaDropsDisabled() ? " &c&lNo-DROP" : "") + (super.isBlockMiningDisabled() ? " &c&lUnbreakable" : ""), RMLanguageConfig.file().getStringList("GUI.Items.Mine-Block.Schematic-Block.Description")
                .stream()
                .map(Text::color)
                .collect(Collectors.toList()));
    }

    @Override
    public Type getType() {
        return Type.SCHEMATIC_BLOCK;
    }
}