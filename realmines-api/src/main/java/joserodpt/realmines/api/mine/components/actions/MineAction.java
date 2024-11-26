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

    public enum MineActionType {
        GIVE_MONEY("&a&lGive Money", "&aMoney"),
        DROP_ITEM("&e&lDrop Item", "&eDrop"),
        GIVE_ITEM("&b&lGive Item", "&bGive"),
        EXECUTE_COMMAND("&c&lExecute Command", "&cCommand"),
        DUMMY("&d&lDummy", "&dDummy");

        private final String displayName, shortName;

        MineActionType(String displayName, String shortName) {
            this.displayName = displayName;
            this.shortName = shortName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getShortName() {
            return shortName;
        }
    }

    private String id, mineID;
    private Double chance = 0D;
    private boolean interactable = true;

    public MineAction() {
        this.interactable = false;
    }

    //generate new action
    public MineAction(final String mineID, final Double chance) {
        this.id = getNewBreakActionCode();
        this.mineID = mineID;
        this.chance = chance;
    }

    //for existing actions
    public MineAction(final String id, final String mineID, final Double chance) {
        this.id = id;
        this.mineID = mineID;
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
        return RealMinesAPI.getInstance().getMineManager().getMine(mineID);
    }

    public void setChance(Double d) {
        this.chance = d;
    }

    public abstract void execute(final Player p, final Location loc, double randomChance);

    public abstract MineActionType getType();

    public abstract String getValueString();

    public abstract Object getValue();

    public abstract ItemStack getItem();

    public String getNewBreakActionCode() {
        final String characters = "abcdefghijklmnopqrstuvwxyz";

        return "action-" + RealMinesAPI.getRand().ints(8, 0, characters.length())
                .mapToObj(characters::charAt)
                .map(Object::toString)
                .collect(Collectors.joining()) + "-" + System.currentTimeMillis() / 1000;
    }

    @Override
    public String toString() {
        return "MineAction{" +
                "id='" + id + '\'' +
                ", chance=" + chance +
                ", type=" + getType().name() +
                ", mine=" + mineID +
                '}';
    }
}
