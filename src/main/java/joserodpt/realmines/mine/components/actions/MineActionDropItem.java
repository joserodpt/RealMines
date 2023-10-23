package joserodpt.realmines.mine.components.actions;

import joserodpt.realmines.config.Language;
import joserodpt.realmines.util.ItemStackSpringer;
import joserodpt.realmines.util.Text;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class MineActionDropItem extends MineAction {

    private ItemStack i;
    public MineActionDropItem(final String id, final Double chance, final ItemStack i) {
        super(id, chance);
        this.i = i;
    }

    public void execute(final Player p, final Location l, final double randomChance) {
        if (randomChance < super.getChance()) {
            Objects.requireNonNull(l.getWorld()).dropItemNaturally(l, this.i);
            Text.send(p, Language.file().getString("Mines.Break-Actions.Drop-Item"));
        }
    }

    @Override
    public MineAction.Type getType() {
        return Type.DROP_ITEM;
    }

    @Override
    public String getValue() {
        return ItemStackSpringer.getItemSerializedJSON(this.i);
    }

    @Override
    public String toString() {
        return "MineActionItem{" +
                "i=" + i +
                ", chance=" + super.getChance() +
                '}';
    }
}
