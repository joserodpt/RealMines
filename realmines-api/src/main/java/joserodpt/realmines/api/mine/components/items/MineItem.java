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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.RMMinesConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.components.actions.MineAction;
import joserodpt.realmines.api.utils.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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

    private boolean isSchematicBlock = false;

    public MineItem() {
    }

    public MineItem(Material material) {
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

    public MineItem(Material material, Double percentage, boolean disabledVanillaDrop, boolean disabledBlockMining, final List<MineAction> breakActions, boolean isSchematicBlock) {
        this.material = material;
        this.percentage = percentage;
        this.breakActions = breakActions;
        this.disabledVanillaDrop = disabledVanillaDrop;
        this.disabledBlockMining = disabledBlockMining;

        this.isSchematicBlock = isSchematicBlock;
    }

    public String getNewBreakActionCode(final String mineName, final String material) {
        final String characters = "abcdefghijklmnopqrstuvwxyz";

        if (!RMMinesConfig.file().getSection(mineName + ".Blocks." + material).getKeys().contains("Break-Actions")) {
            return RealMinesAPI.getRand().ints(8, 0, characters.length())
                    .mapToObj(characters::charAt)
                    .map(Object::toString)
                    .collect(Collectors.joining());
        }

        String ret;
        do {
            ret = RealMinesAPI.getRand().ints(8, 0, characters.length())
                    .mapToObj(characters::charAt)
                    .map(Object::toString)
                    .collect(Collectors.joining());
        } while (RMMinesConfig.file().getSection(mineName + ".Blocks." + material + ".Break-Actions").getRoutesAsStrings(false).contains(ret));

        return ret;
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

    public boolean isSchematicBlock() {
        return isSchematicBlock;
    }

    public ItemStack getItem() {
        return Items.createItem(Material.DEAD_BUSH, 1, TranslatableLine.GUI_NO_BLOCKS_NAME.get(), RMLanguageConfig.file().getStringList("GUI.Items.No-Blocks.Description"));
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
