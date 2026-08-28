package joserodpt.realmines.api.mine.types;

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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.MineCuboid;
import joserodpt.realmines.api.mine.components.RMBlockSet;
import joserodpt.realmines.api.mine.components.RMFailedToLoadException;
import joserodpt.realmines.api.mine.components.items.MineBlockItem;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.utils.WorldEditUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BlockMine extends RMine {
    private final List<Material> sorted = new ArrayList<>();

    //new mine
    public BlockMine(String name, World w, Location pos1, Location pos2) throws RMFailedToLoadException {
        super(name, w, pos1, pos2);

        this.fillContent();
        this.updateSigns();
    }

    //converting from old config to new config
    public BlockMine(String name, Section mineConfigSection) throws RMFailedToLoadException {
        super(name, mineConfigSection);

        this.fillContent();
        this.updateSigns();
    }

    //after converting from old config to new config
    public BlockMine(String name, YamlConfiguration config) throws RMFailedToLoadException {
        super(name, config);

        if (!RMConfig.file().getBoolean("RealMines.disableMineResetOnServerStart", false)) {
            this.fillContent();
        }
        this.updateSigns();
    }

    @Override
    public void fillContent() {
        if (!super.getMineItems().isEmpty()) {
            if (this.hasDepthRanges()) {
                this.fillContentByDepth();
                super.fillFaces();
                return;
            }

            if (RMConfig.file().getBoolean("RealMines.useWorldEditForBlockPlacement")) {
                try {
                    //blocks
                    RandomPattern randomPattern = new RandomPattern();

                    super.getMineItems().values().stream().filter(mineItem -> mineItem.getPercentage() > 0)
                            .forEach(mineBlock -> randomPattern.add(BukkitAdapter.adapt(mineBlock.getMaterial().createBlockData()).toBaseBlock(), mineBlock.getPercentage()));

                    BlockVector3 point1 = BlockVector3.at(this.getMineCuboid().getPOS1().getX(), this.getMineCuboid().getPOS1().getY(), this.getMineCuboid().getPOS1().getZ());
                    BlockVector3 point2 = BlockVector3.at(this.getMineCuboid().getPOS2().getX(), this.getMineCuboid().getPOS2().getY(), this.getMineCuboid().getPOS2().getZ());
                    WorldEditUtils.setBlocks(new CuboidRegion(BukkitAdapter.adapt(this.getWorld()), point1, point2), randomPattern);
                } catch (Exception e) {
                    Bukkit.getLogger().severe("Error while setting blocks for mine: " + this.getName());
                    Bukkit.getLogger().warning("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                this.sortBlocks();
                if (!super.getMineItems().isEmpty()) {
                    Bukkit.getScheduler().runTask(RealMinesAPI.getInstance().getPlugin(), () -> {
                        //blocks
                        for (Block block : this.getMineCuboid()) {
                            Material set = this.getBlock();
                            if (block.getType() != set) {
                                block.setType(set);
                            }
                        }
                    });
                }
            }
        }
        super.fillFaces();
    }


    @Override
    public RMine.Type getType() {
        return Type.BLOCKS;
    }

    private void sortBlocks() {
        this.sorted.clear();
        this.sorted.addAll(buildBag(super.getMineItems().values(), this.getBlockCount(), 1D));
    }

    private Material getBlock() {
        return drawBlock(this.sorted);
    }

    /**
     * Builds the pool of materials a region is filled from: each item takes up a
     * slice of the pool proportional to its percentage, scaled by the given factor.
     */
    private static List<Material> buildBag(final Collection<MineItem> items, final int blockCount, final double scale) {
        final List<Material> bag = new ArrayList<>();

        for (final MineItem d : items) {
            final double percentage = d.getPercentage() * scale * blockCount;

            for (int i = 0; i <= (int) percentage; ++i) {
                if (bag.size() != blockCount) {
                    bag.add(d.getMaterial());
                }
            }
        }

        return bag;
    }

    /**
     * Takes a random material out of the given pool. An exhausted pool means the
     * region isn't fully covered by the mine's percentages, so it's filled with air.
     */
    private static Material drawBlock(final List<Material> bag) {
        final Material m;
        if (!bag.isEmpty()) {
            m = bag.get(RealMinesAPI.getRand().nextInt(bag.size()));
            bag.remove(m);
        } else {
            m = Material.AIR;
        }
        return m;
    }

    private boolean hasDepthRanges() {
        return super.getMineItems().values().stream().anyMatch(MineItem::hasDepthRange);
    }

    /**
     * Fills the mine one layer at a time, from the mine's depth origin face towards
     * the opposite one, so that each material only spawns inside its depth range.
     * Materials that aren't allowed at a given depth have their share redistributed
     * over the ones that are, keeping the layer as filled as it would be otherwise.
     */
    private void fillContentByDepth() {
        final MineCuboid cuboid = this.getMineCuboid();
        final MineCuboid.CuboidDirection direction = this.getDepthDirection();
        final int layerCount = cuboid.getSize(direction);

        final Collection<MineItem> mineItems = super.getMineItems().values();
        final double totalPercentage = mineItems.stream().mapToDouble(MineItem::getPercentage).sum();

        if (RMConfig.file().getBoolean("RealMines.useWorldEditForBlockPlacement")) {
            try {
                for (int i = 0; i < layerCount; ++i) {
                    final List<MineItem> eligible = getItemsAtDepth(mineItems, i, layerCount);

                    //WorldEdit normalizes the weights of a random pattern by itself
                    final RandomPattern randomPattern = new RandomPattern();
                    if (eligible.isEmpty()) {
                        //no material may spawn at this depth, so the layer is left empty
                        randomPattern.add(BukkitAdapter.adapt(Material.AIR.createBlockData()).toBaseBlock(), 1D);
                    } else {
                        eligible.forEach(mineItem -> randomPattern.add(BukkitAdapter.adapt(mineItem.getMaterial().createBlockData()).toBaseBlock(), mineItem.getPercentage()));
                    }

                    final MineCuboid layer = cuboid.getLayer(direction, i);
                    final BlockVector3 point1 = BlockVector3.at(layer.getMin().getX(), layer.getMin().getY(), layer.getMin().getZ());
                    final BlockVector3 point2 = BlockVector3.at(layer.getMax().getX(), layer.getMax().getY(), layer.getMax().getZ());
                    WorldEditUtils.setBlocks(new CuboidRegion(BukkitAdapter.adapt(this.getWorld()), point1, point2), randomPattern);
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error while setting blocks for mine: " + this.getName());
                Bukkit.getLogger().warning("Error: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }

        Bukkit.getScheduler().runTask(RealMinesAPI.getInstance().getPlugin(), () -> {
            for (int i = 0; i < layerCount; ++i) {
                final List<MineItem> eligible = getItemsAtDepth(mineItems, i, layerCount);
                final double eligiblePercentage = eligible.stream().mapToDouble(MineItem::getPercentage).sum();
                //what the materials of this layer can't fill is given to the ones that can
                final double scale = eligiblePercentage <= 0 ? 0D : totalPercentage / eligiblePercentage;

                final MineCuboid layer = cuboid.getLayer(direction, i);
                final List<Material> bag = buildBag(eligible, layer.getTotalBlocks(), scale);

                for (final Block block : layer) {
                    final Material set = drawBlock(bag);
                    if (block.getType() != set) {
                        block.setType(set);
                    }
                }
            }
        });
    }

    private static List<MineItem> getItemsAtDepth(final Collection<MineItem> items, final int layer, final int layerCount) {
        //the middle of a layer is what decides which materials it can hold
        final double depth = (layer + 0.5D) / layerCount;
        return items.stream()
                .filter(mineItem -> mineItem.getPercentage() > 0 && mineItem.isAllowedAtDepth(depth))
                .collect(Collectors.toList());
    }

    public void removeMineBlockItem(final String blockSetKey, final MineItem mb) {
        super.getBlockSet(blockSetKey).remove(mb);
        this.saveData(MineData.BLOCKS);
    }

    public void addItem(final String blockSetKey, final MineBlockItem mineBlock) {
        RMBlockSet blockSet = this.getBlockSet(blockSetKey);
        if (blockSet != null && !blockSet.contains(mineBlock)) {
            blockSet.add(mineBlock);
            this.saveData(MineData.BLOCKS);
        }
    }

    @Override
    public void clearContents() {
        if (RMConfig.file().getBoolean("RealMines.useWorldEditForBlockPlacement")) {
            BlockVector3 point1 = BlockVector3.at(this.getMineCuboid().getPOS1().getX(), this.getMineCuboid().getPOS1().getY(), this.getMineCuboid().getPOS1().getZ());
            BlockVector3 point2 = BlockVector3.at(this.getMineCuboid().getPOS2().getX(), this.getMineCuboid().getPOS2().getY(), this.getMineCuboid().getPOS2().getZ());

            WorldEditUtils.setBlocks(new CuboidRegion(BukkitAdapter.adapt(this.getWorld()), point1, point2),
                    BukkitAdapter.adapt(Material.AIR.createBlockData()));
        } else {
            this.getMineCuboid().clear();
        }
    }
}
