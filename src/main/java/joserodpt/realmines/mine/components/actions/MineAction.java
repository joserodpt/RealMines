package joserodpt.realmines.mine.components.actions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class MineAction {

    public enum Type { GIVE_MONEY, DROP_ITEM, GIVE_ITEM, EXECUTE_COMMAND, DUMMY }

    private String id, mineID;
    private Double chance = 0D;
    private boolean interactable = true;

    public MineAction() {
        this.interactable = false;
    }

    public MineAction(final String id, final String mineID, final Double chance) {
        this.id = id;
        this.mineID = mineID;
        this.chance = chance;
    }

    public boolean isInteractable() {
        return interactable;
    }

    public String getID() {
        return id;
    }

    public Double getChance() {
        return chance;
    }

    public String getMineID() {
        return mineID;
    }

    public void setChance(Double d) {
        this.chance = d;
    }

    public abstract void execute(final Player p, final Location loc, double randomChance);

    public abstract Type getType();

    public abstract Object getValue();

    public abstract ItemStack getItem();
}
