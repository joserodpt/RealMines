package joserodpt.realmines.mine.types;

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
import joserodpt.realmines.mine.components.items.MineBlockItem;
import joserodpt.realmines.mine.components.items.MineItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockMine extends RMine {
    private final Map<Material, MineItem> blocks;
    private final List<Material> sorted = new ArrayList<>();

    public BlockMine(final World w, final String n, final String displayname, final Map<Material, MineItem> b, final List<MineSign> si, final Location p1, final Location p2, final Material i,
                     final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final MineColor color, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent, final MineManager mm) {

        super(w, n, displayname, si, i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent, mm);

        this.blocks = b;

        super.setPOS(p1, p2);
        this.fill();
        this.updateSigns();
    }

    @Override
    public void fill() {
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            this.sortBlocks();
            if (!this.blocks.isEmpty()) {
                Bukkit.getScheduler().runTask(RealMines.getPlugin(), () -> {
                    //blocks
                    for (Block block : this.mineCuboid) {
                        Material set = this.getBlock();
                        if (block.getType() != set) {
                            block.setType(set);
                        }
                    }

                    //faces
                    for (final Map.Entry<MineCuboid.CuboidDirection, Material> pair : this.faces.entrySet()) {
                        this.mineCuboid.getFace(pair.getKey()).forEach(block -> block.setType(pair.getValue()));
                    }
                });
            }
        }
    }

    @Override
    public Map<Material, MineItem> getMineItems() {
        return this.blocks;
    }

    @Override
    public RMine.Type getType() {
        return Type.BLOCKS;
    }

    private void sortBlocks() {
        this.sorted.clear();

        for (final MineItem d : this.blocks.values()) {
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
            m = this.sorted.get(RealMines.getPlugin().getRand().nextInt(this.sorted.size()));
            this.sorted.remove(m);
        } else {
            m = Material.AIR;
        }
        return m;
    }

    public List<MineItem> getBlockIcons() {
        return this.blocks.isEmpty() ? new ArrayList<>(Collections.singletonList(new MineItem())) :
                new ArrayList<>(this.blocks.values());
    }

    public void removeMineBlockItem(final MineItem mb) {
        this.blocks.remove(mb.getMaterial());
        this.saveData(Data.BLOCKS);
    }

    public void addItem(final MineBlockItem mineBlock) {
        if (!this.contains(mineBlock)) {
            this.blocks.put(mineBlock.getMaterial(), mineBlock);
            this.saveData(Data.BLOCKS);
        }
    }

    private boolean contains(final MineBlockItem mineBlock) {
        return this.blocks.containsKey(mineBlock.getMaterial());
    }

    @Override
    public BlockPickerGUI.PickType getBlockPickType() {
        return BlockPickerGUI.PickType.BLOCK;
    }

    @Override
    public void clearContents() {
        this.getMineCuboid().forEach(block -> block.setType(Material.AIR));
    }
}
