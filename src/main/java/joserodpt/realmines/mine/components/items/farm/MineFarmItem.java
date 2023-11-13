package joserodpt.realmines.mine.components.items.farm;

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
import joserodpt.realmines.mine.components.items.MineItem;
import joserodpt.realmines.mine.types.farm.FarmItem;
import joserodpt.realmines.util.Items;
import joserodpt.realmines.util.Text;
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
        super(fi.getIcon(), 0.1D, false, new ArrayList<>(), false);
        this.fi = fi;
    }

    public MineFarmItem(final FarmItem fi, final Double percentage) {
        super(fi.getIcon(), percentage, false, new ArrayList<>(), false);
        this.fi = fi;
    }

    public MineFarmItem(final FarmItem c, final Double percentage, final int age) {
        super(c.getIcon(), percentage, false, new ArrayList<>(), false);
        this.fi = c;
        this.age = age;
    }

    public MineFarmItem(final FarmItem c, final Double percentage, final Boolean disab, final int age, final List<MineAction> breakActions) {
        super(c.getIcon(), percentage, disab, breakActions, false);
        this.fi = c;
        this.age = age;
    }

    @Override
    public ItemStack getItem() {
        return Items.createItemLore(super.getMaterial(), 1, Language.file().getString("GUI.Items.Farm-Item.Name").replace("%material%", Text.beautifyMaterialName(this.fi.getIcon())).replace("%age%", String.valueOf(this.getAge())), Language.file().getStringList("GUI.Items.Farm-Item.Description")
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