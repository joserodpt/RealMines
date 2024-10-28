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
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.managers.MineManagerAPI;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.MineColor;
import joserodpt.realmines.api.mine.components.MineCuboid;
import joserodpt.realmines.api.mine.components.MineSign;
import joserodpt.realmines.api.mine.components.items.MineBlockItem;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.utils.WorldEditUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockMine extends RMine {
    private final List<Material> sorted = new ArrayList<>();

    public BlockMine(final World w, final String n, final String displayname, final Map<Material, MineItem> b, final List<MineSign> si, final Location p1, final Location p2, final Material i,
                     final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final MineColor color, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent, final boolean breakingPermissionOn, final MineManagerAPI mm) {

        super(w, n, displayname, si, b, i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent, breakingPermissionOn, mm);

        super.setPOS(p1, p2);
        this.fillContent();
        this.updateSigns();
    }

    @Override
    public void fillContent() {
        if (!super.getMineItems().isEmpty()) {
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

        for (final MineItem d : super.getMineItems().values()) {
            final double percentage = d.getPercentage() * this.getBlockCount();

            for (int i = 0; i <= (int) percentage; ++i) {
                if (this.sorted.size() != this.getBlockCount()) {
                    this.sorted.add(d.getMaterial());
                }
            }
        }
    }

    private Material getBlock() {
        final Material m;
        if (!this.sorted.isEmpty()) {
            m = this.sorted.get(RealMinesAPI.getRand().nextInt(this.sorted.size()));
            this.sorted.remove(m);
        } else {
            m = Material.AIR;
        }
        return m;
    }

    public void removeMineBlockItem(final MineItem mb) {
        super.getMineItems().remove(mb.getMaterial());
        this.saveData(Data.BLOCKS);
    }

    public void addItem(final MineBlockItem mineBlock) {
        if (!this.contains(mineBlock)) {
            super.getMineItems().put(mineBlock.getMaterial(), mineBlock);
            this.saveData(Data.BLOCKS);
        }
    }

    private boolean contains(final MineBlockItem mineBlock) {
        return super.getMineItems().containsKey(mineBlock.getMaterial());
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
