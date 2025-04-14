package joserodpt.realmines.api.mine.components.actions;

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

import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.components.RMineSettings;
import joserodpt.realmines.api.utils.ItemStackSpringer;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class MineActionGiveItem extends MineAction {

    private ItemStack i;

    //for existing
    public MineActionGiveItem(final String id, final String mineID, final Double chance, final ItemStack i) {
        super(id, mineID, chance);
        this.i = i;
    }

    //generate new
    public MineActionGiveItem(final String mineID, final Double chance, final ItemStack i) {
        super(mineID, chance);
        this.i = i;
    }

    public void execute(final Player p, final Location l) {
        if (super.getMine() == null) {
            return;
        }

        if (!super.getMine().getSettingBool(RMineSettings.DISCARD_BREAK_ACTION_MESSAGES)) {
            TranslatableLine.MINE_BREAK_ACTION_GIVE_ITEM.send(p);
        }

        ItemStack item = getClonedItem();
        if (hasSpace(p, item)) {
            p.getInventory().addItem(item);
        } else {
            p.getWorld().dropItemNaturally(l, item);
            Text.send(p, "&cYour inventory is full. &fThe item was dropped!");
        }
    }

    private boolean hasSpace(Player p, ItemStack item) {
        for (int i = 0; i < p.getInventory().getContents().length; ++i) {
            if (i == 36 || i == 37 || i == 38 || i == 39) { // skip armor slots
                continue;
            }

            ItemStack slot = p.getInventory().getItem(i);
            if (slot == null || slot.getType() == Material.AIR) {
                return true;
            } else if (slot.isSimilar(item) && (slot.getAmount() + item.getAmount() <= slot.getMaxStackSize())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public MineActionType getType() {
        return MineActionType.GIVE_ITEM;
    }

    @Override
    public String getValueString() {
        ItemStack tmp = this.getClonedItem();
        return "x" + tmp.getAmount() + " " + Text.beautifyMaterialName(tmp.getType());
    }

    @Override
    public String getValue() {
        return ItemStackSpringer.getItemSerializedJSON(this.getClonedItem().clone());
    }

    @Override
    public ItemStack getIcon() {
        ItemStack tmp = this.getClonedItem();
        return Items.createItem(Material.CHEST, 1, getType().getDisplayName() + " &r&f- " + Text.formatPercentages(super.getChance() / 100) + "%", Arrays.asList("&fItem: &bx" + tmp.getAmount() + " " + Text.beautifyMaterialName(tmp.getType()), "", "&b&nLeft-Click&r&f to change the chance.", "&e&nRight-Click&r&f to change the item.", "&c&nQ (Drop)&r&f to remove this action.", "&8ID: " + getID()));
    }

    public void setItem(ItemStack itemInMainHand) {
        if (itemInMainHand == null || itemInMainHand.getType() == Material.AIR) {
            return;
        }
        this.i = itemInMainHand.clone();
    }

    @Override
    public String toString() {
        return "MineActionItem{" +
                "i=" + getClonedItem() +
                ", chance=" + super.getChance() +
                '}';
    }

    private ItemStack getClonedItem() {
        return i.clone();
    }
}
