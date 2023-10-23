package joserodpt.realmines.mine.components.actions;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class MineAction {

    public enum Type { GIVE_MONEY, DROP_ITEM, GIVE_ITEM }

    private final String id;
    private final Double chance;

    //TODO: make gui for adding mine actions

    public MineAction(final String id, final Double chance) {
        this.id = id;
        this.chance = chance;
    }

    public String getID() {
        return id;
    }

    public Double getChance() {
        return chance;
    }

    public abstract void execute(final Player p, final Location loc, double randomChance);

    public abstract Type getType();

    public abstract Object getValue();
}
