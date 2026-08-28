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

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.utils.ItemStackSpringer;
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

    /**
     * Whether this action belongs to a mine. Achievement rewards reuse these actions without one,
     * so they are built with a null mine and must not be skipped for not resolving to a mine.
     */
    public boolean isMineBound() {
        return this.mineID != null;
    }

    public RMine getMine() {
        return this.mineID == null ? null : RealMinesAPI.getInstance().getMineManager().getMine(mineID);
    }

    /**
     * Whether this action should be skipped: it points at a mine that no longer exists.
     */
    protected boolean isOrphaned() {
        return isMineBound() && getMine() == null;
    }

    /**
     * Builds an action from the values stored in a config file. Shared by mine break actions and
     * achievement rewards, which use the same on-disk shape.
     *
     * @param mineID the owning mine's name, or null for an action that isn't tied to a mine
     * @return the action, or null if the type has nothing to build
     * @throws RuntimeException if the value can't be read as the type says it should be
     */
    public static MineAction deserialize(final String id, final String mineID, final MineActionType type, final Double chance, final Object value) {
        if (value == null) {
            return null;
        }
        switch (type) {
            case EXECUTE_COMMAND:
                return new MineActionCommand(id, mineID, chance, String.valueOf(value));
            case GIVE_MONEY:
                return new MineActionMoney(id, mineID, chance, value instanceof Number
                        ? ((Number) value).doubleValue()
                        : Double.parseDouble(String.valueOf(value)));
            case DROP_ITEM:
                return new MineActionDropItem(id, mineID, chance, ItemStackSpringer.getItemDeSerializedJSON(String.valueOf(value)).clone());
            case GIVE_ITEM:
                return new MineActionGiveItem(id, mineID, chance, ItemStackSpringer.getItemDeSerializedJSON(String.valueOf(value)));
            default:
                return null;
        }
    }

    public void setChance(Double d) {
        this.chance = d;
    }

    public abstract void execute(final Player p, final Location loc);

    public abstract MineActionType getType();

    public abstract String getValueString();

    public abstract Object getValue();

    public abstract ItemStack getIcon();

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
