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
import joserodpt.realmines.config.Config;
import joserodpt.realmines.gui.BlockPickerGUI;
import joserodpt.realmines.manager.MineManager;
import joserodpt.realmines.mine.RMine;
import joserodpt.realmines.mine.components.MineColor;
import joserodpt.realmines.mine.components.MineCuboid;
import joserodpt.realmines.mine.components.MineSign;
import joserodpt.realmines.mine.components.items.MineFarmItem;
import joserodpt.realmines.mine.components.items.MineItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class FarmMine extends RMine {

    private final ArrayList<MineItem> farmItems;
    private final ArrayList<MineItem> sorted = new ArrayList<>();

    public FarmMine(final String n, final String displayname, final ArrayList<MineItem> b, final ArrayList<MineSign> si, final Location p1, final Location p2, final Material i,
                    final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final MineColor color, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent, final MineManager mm) {
        super(n, displayname, si, i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent, mm);

        this.farmItems = b;
        super.setPOS(p1, p2);
        this.updateSigns();

    }

    @Override
    public void fill() {
        this.sortCrops();
        if (!this.farmItems.isEmpty()) {
            Bukkit.getScheduler().runTask(RealMines.getPlugin(), () -> {
                for (Block block : this.mineCuboid) {
                    MineFarmItem fi = this.getFarmBlock();
                    if (fi.getMaterial() != Material.AIR) {
                        final Block under = block.getRelative(BlockFace.DOWN);

                        if (fi.getFarmItem().canBePlaced(block, under)) {
                            if (under.getType() != Material.WATER) {
                                if (Config.file().getBoolean("RealMines.placeFarmLandBelowCrop")) {
                                    Material underMat = fi.getFarmItem().getUnderMaterial();
                                    if (under.getType() != underMat) {
                                        under.setType(underMat);
                                    }
                                }

                                Material mat = fi.getFarmItem().getMaterial();
                                if (block.getType() != mat) {
                                    block.setType(mat);
                                }

                                BlockData data = block.getBlockData();
                                if (data instanceof Ageable) {
                                    Ageable ag = (Ageable) data;
                                    ag.setAge(fi.getAge());
                                    block.setBlockData(ag);
                                }
                            }
                        }
                    }
                }

                //faces
                for (final Map.Entry<MineCuboid.CuboidDirection, Material> pair : this.faces.entrySet()) {
                    this.mineCuboid.getFace(pair.getKey()).forEach(block -> block.setType(pair.getValue()));
                }
            });
        }
    }

    @Override
    public RMine.Type getType() {
        return Type.FARM;
    }

    private void sortCrops() {
        this.sorted.clear();

        for (final MineItem d : this.farmItems) {
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

    public ArrayList<String> getFarmItems() {
        return this.farmItems.stream()
                .map(Object::toString)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void removeMineFarmItem(final MineItem mb) {
        this.farmItems.remove(mb);
        this.saveData(Data.BLOCKS);
    }

    public void addFarmItem(final MineFarmItem mineFarmItem) {
        if (!this.contains(mineFarmItem)) {
            this.farmItems.add(mineFarmItem);
            this.saveData(Data.BLOCKS);

            this.farmItems.sort((a, b) -> {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return Double.compare(b.getPercentage(), a.getPercentage());
            });
        }
    }

    private boolean contains(final MineFarmItem fi) {
        return this.farmItems.stream()
                .anyMatch(item -> ((MineFarmItem) item).getFarmItem() == fi.getFarmItem());
    }

    public ArrayList<MineItem> getBlockIcons() {
        return this.farmItems.isEmpty() ? new ArrayList<>(Collections.singletonList(new MineItem())) :
                this.farmItems;
    }

    @Override
    public BlockPickerGUI.PickType getBlockPickType() {
        return BlockPickerGUI.PickType.FARM_ITEM;
    }
}
