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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MineBlockItem extends MineItem {

    public MineBlockItem(final Material m) {
        super(m);
    }

    public MineBlockItem(final Material m, final Double percentage) {
        super(m, percentage, false, false, new ArrayList<>(), false);
    }

    public MineBlockItem(final Material m, final Double percentage, final Boolean disabledVanillaDrop, final Boolean disabledBlockMining, final List<MineAction> breakActions) {
        super(m, percentage, disabledVanillaDrop, disabledBlockMining, breakActions, false);
    }

    @Override
    public ItemStack getItem() {
        return Items.createItemLore(super.getMaterial(), 1, TranslatableLine.GUI_MINE_BLOCK_NAME.setV1(TranslatableLine.ReplacableVar.MATERIAL.eq(Text.beautifyMaterialName(super.getMaterial()))) + (super.areVanillaDropsDisabled() ? " &c&lNo-DROP" : "") + (super.isBlockMiningDisabled() ? " &c&lUnbreakable" : ""), RMLanguageConfig.file().getStringList("GUI.Items.Mine-Block.Block.Description")
                .stream()
                .map(s -> Text.color(s.replaceAll("%percentage%", String.valueOf(super.getPercentage() * 100))))
                .collect(Collectors.toList()));
    }

    @Override
    public Type getType() {
        return Type.BLOCK;
    }
}