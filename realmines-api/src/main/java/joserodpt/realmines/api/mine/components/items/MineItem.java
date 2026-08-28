package joserodpt.realmines.api.mine.components.items;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2026
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.components.actions.MineAction;
import joserodpt.realmines.api.mine.components.items.farm.MineFarmItem;
import joserodpt.realmines.api.mine.types.farm.FarmItem;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MineItem {

    public enum Type {SCHEMATIC_BLOCK, BLOCK, FARM, NONE}

    private Material material = null;
    private Double percentage;
    private Boolean disabledVanillaDrop = false;
    private Boolean disabledBlockMining = false;
    private List<MineAction> breakActions;
    //depth range, from 0 to 1, relative to the mine's depth origin face
    private double depthMin = 0.0D, depthMax = 1.0D;

    public MineItem() {
    }

    public MineItem(Material material) {
        if (this instanceof MineBlockItem && !material.isBlock()) {
            throw new IllegalArgumentException("Material " + material + " is not a block.");
        }
        if (this instanceof MineFarmItem && FarmItem.valueOf(material) == null) {
            throw new IllegalArgumentException("Material " + material + " is not a farm item.");
        }
        this.material = material;
        this.percentage = 0.1D;
        this.breakActions = new ArrayList<>();
    }

    public MineItem(Material material, Double percentage) {
        //schematic block
        this.material = material;
        this.percentage = percentage;
        this.breakActions = new ArrayList<>();
    }

    public MineItem(Material material, Double percentage, boolean disabledVanillaDrop, boolean disabledBlockMining, final List<MineAction> breakActions) {
        this.material = material;
        this.percentage = percentage;
        this.breakActions = breakActions;
        this.disabledVanillaDrop = disabledVanillaDrop;
        this.disabledBlockMining = disabledBlockMining;
    }

    public double getDepthMin() {
        return this.depthMin;
    }

    public double getDepthMax() {
        return this.depthMax;
    }

    public void setDepthRange(double min, double max) {
        min = Math.max(0D, Math.min(1D, min));
        max = Math.max(0D, Math.min(1D, max));

        this.depthMin = Math.min(min, max);
        this.depthMax = Math.max(min, max);
    }

    public boolean hasDepthRange() {
        return this.depthMin > 0D || this.depthMax < 1D;
    }

    public boolean isAllowedAtDepth(final double ratio) {
        return ratio >= this.depthMin && ratio <= this.depthMax;
    }

    public void toggleVanillaBlockDrop() {
        this.disabledVanillaDrop = !this.areVanillaDropsDisabled();
    }

    public Boolean areVanillaDropsDisabled() {
        return disabledVanillaDrop;
    }

    public void toggleBlockMining() {
        this.disabledBlockMining = !this.disabledBlockMining;
    }

    public Boolean isBlockMiningDisabled() {
        return this.disabledBlockMining;
    }

    public ItemStack getItem() {
        return Items.createItem(Material.DEAD_BUSH, 1, TranslatableLine.GUI_NO_BLOCKS_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.No-Blocks.Description"));
    }

    public List<MineAction> getBreakActions() {
        return this.breakActions;
    }

    protected @NotNull List<String> getBreakActionsTextList() {
        return this.getBreakActions().stream().map(action -> "&7- " + action.getType().getShortName() + "&r&f: " + action.getValueString() + " &f(&e" + Text.formatPercentages(action.getChance() / 100) + "%&f)").collect(Collectors.toList());
    }

    public boolean hasBreakActions() {
        return !this.breakActions.isEmpty();
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
