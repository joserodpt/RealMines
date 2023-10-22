package joserodpt.realmines.mine.components.actions;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class MineAction {

    public enum Type { MONEY, ITEM }

    private Double chance;

    public MineAction(final Double chance) {
        this.chance = chance;
    }

    public Double getChance() {
        return chance;
    }

    public abstract void execute(final Player p, final Location loc, double randomChance);

    public abstract Type getType();
}
