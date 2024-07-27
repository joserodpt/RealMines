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
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.config.RMMinesConfig;
import joserodpt.realmines.api.config.TranslatableLine;
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

    public MineActionGiveItem(final String id, final String mineID, final Double chance, final ItemStack i) {
        super(id, mineID, chance);
        this.i = i;
    }

    public void execute(final Player p, final Location l, final double randomChance) {
        if (randomChance < super.getChance()) {
            p.getInventory().addItem(i);
            if (RMMinesConfig.file().getBoolean(super.getMineID() + ".Settings.Discard-Break-Action-Messages"))
                TranslatableLine.MINE_BREAK_ACTION_GIVE_ITEM.send(p);
        }
    }

    @Override
    public MineAction.Type getType() {
        return Type.GIVE_ITEM;
    }

    @Override
    public String getValue() {
        return ItemStackSpringer.getItemSerializedJSON(this.i.clone());
    }

    @Override
    public ItemStack getItem() {
        return Items.createItem(Material.CHEST, 1, "&b&lGive Item &r&f- " + super.getChance() + "%", Arrays.asList("&fItem: &bx" + this.i.getAmount() + " " + Text.beautifyMaterialName(this.i.getType()), "", "&b&nLeft-Click&r&f to change the chance.", "&e&nRight-Click&r&f to change the item.", "&c&nQ (Drop)&r&f to remove this action.", "&8ID: " + getID()));
    }

    public void setItem(ItemStack itemInMainHand) {
        if (itemInMainHand == null || itemInMainHand.getType() == Material.AIR) {
            return;
        }
        this.i = itemInMainHand;
    }

    @Override
    public String toString() {
        return "MineActionItem{" +
                "i=" + i +
                ", chance=" + super.getChance() +
                '}';
    }
}
