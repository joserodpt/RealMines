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

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.mine.RMine;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public abstract class MineAction {

    public enum Type {GIVE_MONEY, DROP_ITEM, GIVE_ITEM, EXECUTE_COMMAND, DUMMY}

    private String id;
    private RMine mine;
    private Double chance = 0D;
    private boolean interactable = true;

    public MineAction() {
        this.interactable = false;
    }

    //generate new action
    public MineAction(final String mineID, final Double chance) {
        this.mine = RealMinesAPI.getInstance().getMineManager().getMine(mineID);
        if (this.mine == null) {
            this.interactable = false;
            return;
        }

        this.id = getNewBreakActionCode();
        this.chance = chance;
    }

    //for existing actions
    public MineAction(final String id, final String mineID, final Double chance) {
        this.mine = RealMinesAPI.getInstance().getMineManager().getMine(mineID);
        if (this.mine == null) {
            this.interactable = false;
            return;
        }

        this.id = id;
        this.chance = chance;
    }

    public boolean isInteractable() {
        return this.interactable;
    }

    public String getID() {
        return this.id;
    }

    public Double getChance() {
        return this.chance;
    }

    public RMine getMine() {
        return this.mine;
    }

    public void setChance(Double d) {
        this.chance = d;
    }

    public abstract void execute(final Player p, final Location loc, double randomChance);

    public abstract Type getType();

    public abstract Object getValue();

    public abstract ItemStack getItem();

    public String getNewBreakActionCode() {
        final String characters = "abcdefghijklmnopqrstuvwxyz";

        return "action-" + RealMinesAPI.getRand().ints(8, 0, characters.length())
                .mapToObj(characters::charAt)
                .map(Object::toString)
                .collect(Collectors.joining()) + "-" + System.currentTimeMillis() / 1000;
    }
}
