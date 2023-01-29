package josegamerpt.realmines.mines;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Config;
import josegamerpt.realmines.mines.components.MineBlock;
import josegamerpt.realmines.mines.components.MineCuboid;
import josegamerpt.realmines.mines.components.MineSign;
import josegamerpt.realmines.mines.gui.MineBlockIcon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BlockMine extends RMine {
    private ArrayList<MineBlock> blocks;
    private ArrayList<Material> sorted = new ArrayList<>();

    public BlockMine(String n, String displayname, ArrayList<MineBlock> b, ArrayList<MineSign> si, Location p1, Location p2, Material i,
                     Location t, Boolean resetByPercentag, Boolean resetByTim, int rbpv, int rbtv, String color, HashMap<MineCuboid.CuboidDirection, Material> faces, boolean silent) {
        super(n, displayname, si, i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent);

        this.blocks = b;

        super.setPOS(p1, p2);
        this.updateSigns();
    }

    @Override
    public void fill() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            this.sortBlocks();
            if (this.blocks.size() != 0) {
                if (Config.file().getBoolean("RealMines.dev-options.async-resets")) {
                    RealMines.getInstance().getExecutor().execute(() -> {
                        //blocks
                        this.mineCuboid.forEach(block -> block.setType(getBlock()));
                        //faces
                        for (Map.Entry<MineCuboid.CuboidDirection, Material> pair : this.faces.entrySet()) {
                            this.mineCuboid.getFace(pair.getKey()).forEach(block -> block.setType(pair.getValue()));
                        }
                    });
                } else {
                    Bukkit.getScheduler().runTask(RealMines.getInstance(), () -> {
                        //blocks
                        this.mineCuboid.forEach(block -> block.setType(getBlock()));
                        //faces
                        for (Map.Entry<MineCuboid.CuboidDirection, Material> pair : this.faces.entrySet()) {
                            this.mineCuboid.getFace(pair.getKey()).forEach(block -> block.setType(pair.getValue()));
                        }
                    });
                }
            }
        }
    }

    @Override
    public String getType() {
        return "BLOCKS";
    }

    private void sortBlocks() {
        this.sorted.clear();

        for (MineBlock d : this.blocks) {
            double percentage = d.getPercentage() * getBlockCount();

            for (int i = 0; i <= (int) percentage; ++i)
            {
                if (this.sorted.size() != this.getBlockCount()) {
                    this.sorted.add(d.getMaterial());
                }
            }
        }
    }

    private Material getBlock() {
        Material m;
        Random rand = new Random();
        if (this.sorted.size() > 0) {
            m = this.sorted.get(rand.nextInt(sorted.size()));
            this.sorted.remove(m);
        } else {
            m = Material.AIR;
        }
        return m;
    }

    public ArrayList<String> getBlockList() {
        ArrayList<String> l = new ArrayList<>();
        this.blocks.forEach(mineBlock -> l.add(mineBlock.getMaterial().name() + ";" + mineBlock.getPercentage()));
        return l;
    }

    public ArrayList<MineBlockIcon> getBlocks() {
        ArrayList<MineBlockIcon> l = new ArrayList<>();
        blocks.forEach(mineBlock -> l.add(new MineBlockIcon(mineBlock)));
        if (l.size() == 0) {
            l.add(new MineBlockIcon());
        }
        return l;
    }

    public void removeBlock(MineBlock mb) {
        blocks.remove(mb);
        saveData(Data.BLOCKS);
    }

    public void addBlock(MineBlock mineBlock) {
        if (!this.contains(mineBlock)) {
            this.blocks.add(mineBlock);
            saveData(Data.BLOCKS);

            this.blocks.sort((a, b) -> {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return Double.compare(b.getPercentage(), a.getPercentage());
            });

        }
    }

    private boolean contains(MineBlock mineBlock) {
        for (MineBlock block : this.blocks) {
            if (block.getMaterial() == mineBlock.getMaterial()) {
                return true;
            }
        }
        return false;
    }
}
