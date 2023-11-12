package joserodpt.realmines.mine.components.items;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.RealMines;
import joserodpt.realmines.config.Language;
import joserodpt.realmines.config.Mines;
import joserodpt.realmines.mine.components.actions.MineAction;
import joserodpt.realmines.util.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MineItem {

    public String getNewBrkActCode(final String mineName, final String material) {
        final String characters = "abcdefghijklmnopqrstuvwxyz";

        if (!Mines.file().getSection(mineName + ".Blocks." + material).getKeys().contains("Break-Actions")) {
            return RealMines.getPlugin().getRand().ints(8, 0, characters.length())
                    .mapToObj(characters::charAt)
                    .map(Object::toString)
                    .collect(Collectors.joining());
        }

        String ret;
        do {
            ret = RealMines.getPlugin().getRand().ints(8, 0, characters.length())
                    .mapToObj(characters::charAt)
                    .map(Object::toString)
                    .collect(Collectors.joining());
        } while (Mines.file().getSection(mineName + ".Blocks." + material + ".Break-Actions").getRoutesAsStrings(false).contains(ret));

        return ret;
    }

    public void toggleVanillaBlockDrop() {
        this.disabledVanillaDrop = !this.disabledVanillaDrop();
    }

    public enum Type { BLOCK, FARM, NONE }

    private Material material = null;
    private Double percentage;
    private Boolean disabledVanillaDrop;
    private List<MineAction> breakActions;

    public MineItem() {}

    public MineItem(Material material) {
        this.material = material;
        this.percentage = 0.1D;
        this.breakActions = new ArrayList<>();
    }
    public MineItem(Material material, Double percentage, boolean disabledVanillaDrop, final List<MineAction> breakActions) {
        this.material = material;
        this.percentage = percentage;
        this.breakActions = breakActions;
        this.disabledVanillaDrop = disabledVanillaDrop;
    }

    public Boolean disabledVanillaDrop() {
        return disabledVanillaDrop;
    }

    public ItemStack getItem() {
        return Items.createItemLore(Material.DEAD_BUSH, 1, Language.file().getString("GUI.Items.Mine-Block.No-Blocks.Name"), Language.file().getStringList("GUI.Items.Mine-Block.No-Blocks.Description"));
    }

    public List<MineAction> getBreakActions() {
        return this.breakActions;
    }

    public boolean isInteractable() {
        return this.material != null;
    }

    public Material getMaterial() {
        return this.material;
    }

    public double getPercentage() {
        return this.percentage;
    }
    public void setPercentage(Double d) {
        this.percentage = d;
    }
    public Type getType() {
        return Type.NONE;
    }

    @Override
    public String toString() {
        return "MineItem{" +
                "material=" + material +
                ", percentage=" + percentage +
                ", breakActions=" + breakActions +
                '}';
    }
}
