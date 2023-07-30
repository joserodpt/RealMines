package josegamerpt.realmines.mine;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.manager.MineManager;
import josegamerpt.realmines.mine.component.MineBlock;
import josegamerpt.realmines.mine.component.MineCuboid;
import josegamerpt.realmines.mine.component.MineSign;
import josegamerpt.realmines.mine.gui.MineBlockIcon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class BlockMine extends RMine {
    private final ArrayList<MineBlock> blocks;
    private final ArrayList<Material> sorted = new ArrayList<>();

    public BlockMine(final String n, final String displayname, final ArrayList<MineBlock> b, final ArrayList<MineSign> si, final Location p1, final Location p2, final Material i,
                     final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final String color, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent, final MineManager mm) {
        super(n, displayname, si, i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent, mm);

        this.blocks = b;

        super.setPOS(p1, p2);
        this.updateSigns();
    }

    @Override
    public void fill() {
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            this.sortBlocks();
            if (!this.blocks.isEmpty()) {
                Bukkit.getScheduler().runTask(RealMines.getPlugin(), () -> {
                    //blocks
                    this.mineCuboid.forEach(block -> block.setType(this.getBlock()));
                    //faces
                    for (final Map.Entry<MineCuboid.CuboidDirection, Material> pair : this.faces.entrySet()) {
                        this.mineCuboid.getFace(pair.getKey()).forEach(block -> block.setType(pair.getValue()));
                    }
                });
            }
        }
    }

    @Override
    public String getType() {
        return "BLOCKS";
    }

    private void sortBlocks() {
        this.sorted.clear();

        for (final MineBlock d : this.blocks) {
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
        final Random rand = new Random();
        if (!this.sorted.isEmpty()) {
            m = this.sorted.get(rand.nextInt(this.sorted.size()));
            this.sorted.remove(m);
        } else {
            m = Material.AIR;
        }
        return m;
    }

    public ArrayList<String> getBlockList() {
        return this.blocks.stream()
                .map(mineBlock -> mineBlock.getMaterial().name() + ";" + mineBlock.getPercentage())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<MineBlockIcon> getBlockIcons() {
        return this.blocks.isEmpty() ?  new ArrayList<>(Collections.singletonList(new MineBlockIcon())) :
                this.blocks.stream()
                        .map(MineBlockIcon::new)
                        .collect(Collectors.toCollection(ArrayList::new));
    }

    public void removeBlock(final MineBlock mb) {
        this.blocks.remove(mb);
        this.saveData(Data.BLOCKS);
    }

    public void addBlock(final MineBlock mineBlock) {
        if (!this.contains(mineBlock)) {
            this.blocks.add(mineBlock);
            this.saveData(Data.BLOCKS);

            this.blocks.sort((a, b) -> {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return Double.compare(b.getPercentage(), a.getPercentage());
            });

        }
    }

    private boolean contains(final MineBlock mineBlock) {
        return this.blocks.stream()
                .anyMatch(block -> block.getMaterial() == mineBlock.getMaterial());
    }

}
