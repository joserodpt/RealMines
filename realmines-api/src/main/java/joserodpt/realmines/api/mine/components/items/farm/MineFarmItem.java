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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.mine.components.actions.MineAction;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.mine.types.farm.FarmItem;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Text;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MineFarmItem extends MineItem {

    private final FarmItem fi;
    private int age = 0;

    public MineFarmItem() {
        super();
        this.fi = FarmItem.AIR;
    }

    public MineFarmItem(final FarmItem fi) {
        super(fi.getIcon(), 0.1D, false, false, new ArrayList<>(), false);
        this.fi = fi;
    }

    public MineFarmItem(final FarmItem fi, final Double percentage) {
        super(fi.getIcon(), percentage, false, false, new ArrayList<>(), false);
        this.fi = fi;
    }

    public MineFarmItem(final FarmItem c, final Double percentage, final int age) {
        super(c.getIcon(), percentage, false, false, new ArrayList<>(), false);
        this.fi = c;
        this.age = age;
    }

    public MineFarmItem(final FarmItem c, final Double percentage, final Boolean disabledVanillaDrop, final Boolean disabledBlockMining, final int age, final List<MineAction> breakActions) {
        super(c.getIcon(), percentage, disabledVanillaDrop, disabledBlockMining, breakActions, false);
        this.fi = c;
        this.age = age;
    }

    @Override
    public ItemStack getItem() {
        return Items.createItemLore(super.getMaterial(), 1, RMLanguageConfig.file().getString("GUI.Items.Farm-Item.Name").replace("%material%", Text.beautifyMaterialName(this.fi.getIcon())).replace("%age%", String.valueOf(this.getAge())) + (super.areVanillaDropsDisabled() ? " &c&lNo-DROP" : "") + (super.isBlockMiningDisabled() ? " &c&lUnbreakable" : ""), RMLanguageConfig.file().getStringList("GUI.Items.Farm-Item.Description")
                .stream()
                .map(s -> Text.color(s.replaceAll("%percentage%", String.valueOf(super.getPercentage() * 100))))
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