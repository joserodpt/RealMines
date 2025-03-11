package joserodpt.realmines.api.mine.types.farm;

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

import joserodpt.realmines.api.mine.components.items.farm.FarmItemGrowth;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum FarmItem {
    //crops with different item and material
    WHEAT(Material.WHEAT, Material.WHEAT, Material.FARMLAND, new FarmItemGrowth(0, 7), false, true),
    CARROT(Material.CARROT, Material.CARROTS, Material.FARMLAND, new FarmItemGrowth(0, 7), false, true),
    POTATO(Material.POTATO, Material.POTATOES, Material.FARMLAND, new FarmItemGrowth(0, 7), false, true),
    BEETROOT(Material.BEETROOT, Material.BEETROOTS, Material.FARMLAND, new FarmItemGrowth(0, 3), false, true),
    MELON_SEED(Material.MELON, Material.MELON_STEM, Material.FARMLAND, new FarmItemGrowth(0, 7), false, false),
    PUMPKIN_SEED(Material.PUMPKIN, Material.PUMPKIN_STEM, Material.FARMLAND, new FarmItemGrowth(0, 7), false, false),
    NETHER_WART(Material.NETHER_WART, Material.SOUL_SAND, new FarmItemGrowth(0, 3), false, true),

    //crops with same material
    SUGAR_CANE(Material.SUGAR_CANE, Material.GRASS_BLOCK, true, false),
    CACTUS(Material.CACTUS, Material.SAND, true, false),

    //NO ITEM
    AIR(Material.AIR);

    private final Material icon, crop, underMaterial;
    private final FarmItemGrowth fig;
    private final Boolean needsWaterNearby, canHaveNeighbours;

    FarmItem(final Material icon, final Material crop, final Material underMaterial, final FarmItemGrowth fie, final boolean needsWaterNearby, final boolean canHaveNeighbours) {
        this.icon = icon;
        this.crop = crop;
        this.underMaterial = underMaterial;
        this.needsWaterNearby = needsWaterNearby;
        this.canHaveNeighbours = canHaveNeighbours;

        this.fig = fie;
    }

    FarmItem(final Material crop, final Material underMaterial, final FarmItemGrowth fie, final boolean needsWaterNearby, final boolean canHaveNeighbours) {
        this.icon = crop;
        this.crop = crop;
        this.underMaterial = underMaterial;
        this.needsWaterNearby = needsWaterNearby;
        this.canHaveNeighbours = canHaveNeighbours;

        this.fig = fie;
    }

    FarmItem(final Material crop, final Material underMaterial, final boolean needsWaterNearby, final boolean canHaveNeighbours) {
        this.icon = crop;
        this.crop = crop;
        this.underMaterial = underMaterial;
        this.needsWaterNearby = needsWaterNearby;
        this.canHaveNeighbours = canHaveNeighbours;

        this.fig = null;
    }

    public static Material findIconForCrop(Material m) {
        return Arrays.stream(FarmItem.values())
                .filter(fi -> fi.getCrop().equals(m))
                .map(FarmItem::getIcon)
                .findFirst()
                .orElse(null);
    }

    FarmItem(final Material crop) {
        this.icon = crop;
        this.crop = crop;
        this.underMaterial = crop;

        this.needsWaterNearby = true;
        this.canHaveNeighbours = true;

        this.fig = null;
    }

    public static List<Material> getIcons() {
        return Arrays.stream(FarmItem.values()).map(FarmItem::getIcon).collect(Collectors.toList());
    }

    public static List<Material> getCrops() {
        return Arrays.stream(FarmItem.values()).map(FarmItem::getCrop).collect(Collectors.toList());
    }

    public static Material getIconFromCrop(Material cropMaterial) {
        return Arrays.stream(FarmItem.values())
                .filter(fi -> fi.getCrop().equals(cropMaterial))
                .map(FarmItem::getIcon)
                .findFirst()
                .orElse(Material.AIR);
    }

    public Material getIcon() {
        return this.icon;
    }

    public Material getCrop() {
        return this.crop;
    }

    public Material getUnderMaterial() {
        return this.underMaterial;
    }

    public FarmItemGrowth getFarmItemGrowth() {
        return fig;
    }

    public static FarmItem valueOf(Material icon) {
        return Arrays.stream(FarmItem.values())
                .filter(value -> value.getIcon().equals(icon))
                .findFirst()
                .orElse(null);
    }

    public Boolean needsWaterNearby() {
        return this.needsWaterNearby;
    }

    public Boolean canHaveNeighbours() {
        return this.canHaveNeighbours;
    }

    public boolean canBePlaced(Block block, Block under) {
        if (!this.canHaveNeighbours()) {
            if (Stream.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
                    .map(block::getRelative)
                    .map(Block::getType)
                    .noneMatch(type -> type == Material.AIR)) {
                return false;
            }
        }

        if (!this.needsWaterNearby()) {
            return true;
        }

        return Stream.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
                .map(under::getRelative)
                .map(Block::getType)
                .anyMatch(type -> type == Material.WATER);
    }
}
