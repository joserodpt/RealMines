package joserodpt.realmines.mine.components.actions;

import joserodpt.realmines.config.Language;
import joserodpt.realmines.util.ItemStackSpringer;
import joserodpt.realmines.util.Items;
import joserodpt.realmines.util.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class MineActionGiveItem extends MineAction {

    private ItemStack i;
    public MineActionGiveItem(final String id, final Double chance, final ItemStack i) {
        super(id, chance);
        this.i = i;
    }

    public void execute(final Player p, final Location l, final double randomChance) {
        if (randomChance < super.getChance()) {
            p.getInventory().addItem(i);
            Text.send(p, Language.file().getString("Mines.Break-Actions.Give-Item"));
        }
    }

    @Override
    public MineAction.Type getType() {
        return Type.GIVE_ITEM;
    }

    @Override
    public String getValue() {
        return ItemStackSpringer.getItemSerializedJSON(this.i);
    }

    @Override
    public ItemStack getItem() {
        return Items.createItemLore(Material.CHEST, 1, "&b&lGive Item &r&f- " + super.getChance() + "%", Arrays.asList("&fItem: &bx" + this.i.getAmount() + " " + Text.beautifyMaterialName(this.i.getType()), "", "&b&nLeft-Click&r&f to change the chance.","&e&nRight-Click&r&f to change the item.", "&c&nQ (Drop)&r&f to remove this action.", "&8ID: " + getID()));
    }

    public void setItem(ItemStack itemInMainHand) {
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
