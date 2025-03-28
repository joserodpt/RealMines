package joserodpt.realmines.api.mine.components.items.farm;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.components.actions.MineAction;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.mine.types.farm.FarmItem;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MineFarmItem extends MineItem {

    private final FarmItem fi;
    private int age = 0;

    public MineFarmItem(final FarmItem c, final Double percentage, final Boolean disabledVanillaDrop, final Boolean disabledBlockMining, final int age, final List<MineAction> breakActions) {
        super(c.getIcon(), percentage, disabledVanillaDrop, disabledBlockMining, breakActions);
        this.fi = c;
        this.age = age;
    }

    public MineFarmItem(final FarmItem c, final Double percentage, final int age) {
        super(c.getIcon(), percentage, false, false, new ArrayList<>());
        this.fi = c;
        this.age = age;
    }

    public MineFarmItem(final FarmItem fi, final Double percentage) {
        super(fi.getIcon(), percentage, false, false, new ArrayList<>());
        this.fi = fi;
    }

    public MineFarmItem(final FarmItem fi) {
        super(fi.getIcon(), 0.1D, false, false, new ArrayList<>());
        this.fi = fi;
    }

    public MineFarmItem(final Material m) {
        this(FarmItem.valueOf(m));
    }

    public MineFarmItem() {
        super();
        this.fi = FarmItem.AIR;
    }

    @Override
    public ItemStack getItem() {
        return Items.createItem(super.getMaterial(), 1, TranslatableLine.GUI_FARM_ITEM_NAME.setV1(TranslatableLine.ReplacableVar.MATERIAL.eq(Text.beautifyMaterialName(this.fi.getIcon()))).setV2(TranslatableLine.ReplacableVar.AGE.eq(String.valueOf(this.getAge()))).get() + (super.areVanillaDropsDisabled() ? " &c&lNo-DROP" : "") + (super.isBlockMiningDisabled() ? " &c&lUnbreakable" : ""), RMLanguageConfig.file().getStringList("GUI.Items.Farm-Item.Description")
                .stream()
                .map(s -> Text.color(s.replaceAll("%percentage%", Text.formatPercentages(super.getPercentage()))))
                .collect(Collectors.toList()));
    }

    public int getAge() {
        return this.age;
    }

    public FarmItem getFarmItem() {
        return fi;
    }

    @Override
    public Type getType() {
        return Type.FARM;
    }

    public void addAge(int i) {
        if (this.getFarmItem().getFarmItemGrowth() != null) {
            this.age = Math.min(this.getFarmItem().getFarmItemGrowth().getMax(), Math.max(this.getFarmItem().getFarmItemGrowth().getMin(), age + i));
        }
    }
}