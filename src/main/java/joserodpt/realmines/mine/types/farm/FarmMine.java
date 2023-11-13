package joserodpt.realmines.mine.types.farm;

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
import joserodpt.realmines.gui.BlockPickerGUI;
import joserodpt.realmines.manager.MineManager;
import joserodpt.realmines.mine.RMine;
import joserodpt.realmines.mine.components.MineColor;
import joserodpt.realmines.mine.components.MineCuboid;
import joserodpt.realmines.mine.components.MineSign;
import joserodpt.realmines.mine.components.items.MineItem;
import joserodpt.realmines.mine.components.items.farm.MineFarmItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FarmMine extends RMine {

    private final List<MineItem> sorted = new ArrayList<>();
    private List<Block> mineGroundBlocks = new ArrayList<>();

    public FarmMine(final World w, final String n, final String displayname, final Map<Material, MineItem> b, final List<MineSign> si, final Location p1, final Location p2, final Material i,
                    final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final MineColor color, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent, final boolean breakingPermissionOn, final MineManager mm) {
        super(w, n, displayname, si, b, i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent, breakingPermissionOn, mm);

        this.setPOS(p1, p2);
        this.fill();
        this.updateSigns();
    }

    public void setPOS(Location p1, Location p2) {
        super.setPOS(p1, p2);
        this.checkFarmBlocks();
    }

    private void checkFarmBlocks() {
        //check farm blocks
        if (!this.oneBlockHeight()) {
            this.mineGroundBlocks.clear();
            List<Block> underBlocks = new ArrayList<>(this.getMineCuboid().getFace(MineCuboid.CuboidDirection.Down).getBlocks());
            while (!underBlocks.isEmpty()) {
                Block block = underBlocks.get(0);
                Material upMat = block.getRelative(BlockFace.UP).getType();
                if (block.getType() != Material.WATER && (upMat == Material.AIR || upMat == Material.GRASS || upMat == Material.TALL_GRASS || FarmItem.getCrops().contains(upMat))) {
                    this.mineGroundBlocks.add(block);
                } else {
                    if (block.getType() != Material.WATER) {
                        underBlocks.add(block.getRelative(BlockFace.UP));
                    }
                }
                underBlocks.remove(block);
            }
        }
    }

    @Override
    public void clearContents() {
        if (this.oneBlockHeight()) {
            this.getMineCuboid().clear();
        } else {
            this.mineGroundBlocks.forEach(block -> block.getRelative(BlockFace.UP).setType(Material.AIR));
        }
    }

    @Override
    public void fill() {
        this.sortCrops();

        if (!super.getMineItems().isEmpty()) {
            Bukkit.getScheduler().runTask(RealMines.getPlugin(), () -> {
                if (this.oneBlockHeight()) {
                    for (Block target : this.getMineCuboid()) {
                        MineFarmItem fi = this.getFarmBlock();
                        Block under = target.getRelative(BlockFace.DOWN);
                        placeFarmItems(target, under, fi);
                    }
                } else {
                    for (Block under : this.mineGroundBlocks) {
                        Block target = under.getRelative(BlockFace.UP);
                        MineFarmItem fi = this.getFarmBlock();
                        placeFarmItems(target, under, fi);
                    }
                }

                // Set faces
                for (Map.Entry<MineCuboid.CuboidDirection, Material> pair : this.faces.entrySet()) {
                    this.getMineCuboid().getFace(pair.getKey()).forEach(block -> block.setType(pair.getValue()));
                }
            });
        }

        //faces
        for (final Map.Entry<MineCuboid.CuboidDirection, Material> pair : this.faces.entrySet()) {
            this.getMineCuboid().getFace(pair.getKey()).forEach(block -> block.setType(pair.getValue()));
        }
    }

    private void placeFarmItems(Block target, Block under, MineFarmItem fi) {
        if (fi.getMaterial() != Material.AIR) {
            if (fi.getFarmItem().canBePlaced(target, under)) {
                if (under.getType() != Material.WATER) {
                    boolean placeFarmLandBelowCrop = fi.getFarmItem().canBePlaced(target, under);

                    Material underMat = fi.getFarmItem().getUnderMaterial();
                    if (placeFarmLandBelowCrop && under.getType() != underMat) {
                        under.setType(underMat);
                    }

                    Material mat = fi.getFarmItem().getCrop();
                    if (target.getType() != mat) {
                        target.setType(mat);
                    }

                    BlockData data = target.getBlockData();
                    if (data instanceof Ageable) {
                        Ageable ag = (Ageable) data;
                        ag.setAge(fi.getAge());
                        target.setBlockData(ag);
                    }
                }
            }
        }
    }

    private boolean oneBlockHeight() {
        return getPOS1().getY() == getPOS2().getY();
    }

    @Override
    public RMine.Type getType() {
        return Type.FARM;
    }

    private void sortCrops() {
        this.sorted.clear();

        for (final MineItem d : super.getMineItems().values()) {
            final double percentage = d.getPercentage() * this.getBlockCount();

            for (int i = 0; i <= (int) percentage; ++i) {
                if (this.sorted.size() != this.getBlockCount()) {
                    this.sorted.add(d);
                }
            }
        }
    }

    private MineFarmItem getFarmBlock() {
        if (!sorted.isEmpty()) {
            int randomIndex = new Random().nextInt(sorted.size());
            return (MineFarmItem) sorted.remove(randomIndex);
        } else {
            return new MineFarmItem();
        }
    }

    public void removeMineFarmItem(final MineItem mb) {
        super.getMineItems().remove(mb);
        this.saveData(Data.BLOCKS);
    }

    public void addFarmItem(final MineFarmItem mineFarmItem) {
        if (!this.contains(mineFarmItem)) {
            super.getMineItems().put(mineFarmItem.getMaterial(), mineFarmItem);
            this.saveData(Data.BLOCKS);
        }
    }

    private boolean contains(final MineFarmItem fi) {
        return super.getMineItems().values().stream()
                .anyMatch(item -> ((MineFarmItem) item).getFarmItem() == fi.getFarmItem());
    }

    @Override
    public BlockPickerGUI.PickType getBlockPickType() {
        return BlockPickerGUI.PickType.FARM_ITEM;
    }
}
