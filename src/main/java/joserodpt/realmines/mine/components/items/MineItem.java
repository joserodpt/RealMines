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

import joserodpt.realmines.config.Language;
import joserodpt.realmines.mine.components.actions.MineAction;
import joserodpt.realmines.util.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MineItem {

    public enum Type { BLOCK, FARM, NONE }

    private Material material = null;
    private Double percentage;
    private List<MineAction> breakActions;

    public MineItem() {}

    public MineItem(Material material) {
        this.material = material;
        this.percentage = 0.1D;
        this.breakActions = new ArrayList<>();
    }
    public MineItem(Material material, Double percentage, final List<MineAction> breakActions) {
        this.material = material;
        this.percentage = percentage;
        this.breakActions = breakActions;
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
