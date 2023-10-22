package joserodpt.realmines.mine.components.actions;

import joserodpt.realmines.config.Config;
import joserodpt.realmines.config.Language;
import joserodpt.realmines.util.ItemStackSpringer;
import joserodpt.realmines.util.Text;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class MineActionItem extends MineAction {

    public enum Type { MONEY, DROP_ITEM, GIVE_ITEM }
    private ItemStack i;
    private boolean drop;

    public MineActionItem(final Double chance, final ItemStack i, final boolean drop) {
        super(chance);
        this.i = i;
        this.drop = drop;
    }

    public void execute(final Player p, final Location l, final double randomChance) {
        if (randomChance < super.getChance()) {
            if (this.drop) {
                Objects.requireNonNull(l.getWorld()).dropItemNaturally(l, this.i);
                Text.send(p, Language.file().getString("Mines.Break-Actions.Drop-Item"));
            } else {
                p.getInventory().addItem(i);
                Text.send(p, Language.file().getString("Mines.Break-Actions.Give-Item"));
            }
        }
    }

    @Override
    public MineAction.Type getType() {
        return MineAction.Type.ITEM;
    }

    @Override
    public String toString() {
        return "MineActionItem{" +
                "i=" + i +
                ", drop=" + drop +
                ", chance=" + super.getChance() +
                '}';
    }
}
