package josegamerpt.realmines.mines.mine;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.config.Mines;
import josegamerpt.realmines.mines.RMine;
import josegamerpt.realmines.mines.components.MineBlock;
import josegamerpt.realmines.mines.components.MineCuboid;
import josegamerpt.realmines.mines.components.MineSign;
import josegamerpt.realmines.mines.gui.MineBlockIcon;
import josegamerpt.realmines.mines.tasks.MineTimer;
import josegamerpt.realmines.utils.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.*;

public class BlockMine implements RMine {

    private String name;
    private Color color = Color.WHITE;
    private String displayName;
    private ArrayList<MineBlock> blocks;
    private ArrayList<Material> sorted = new ArrayList<>();
    private ArrayList<MineSign> signs;
    private Location teleport;
    private Material icon;
    private MineCuboid mineCuboid;
    private boolean resetByPercentage;
    private boolean resetByTime;
    private int resetByPercentageValue;
    private int resetByTimeValue;
    private MineTimer timer;
    private boolean highlight = false;
    private HashMap<MineCuboid.CuboidDirection, Material> faces;
    private Location l1;
    private Location l2;

    public BlockMine(String n, String displayname, ArrayList<MineBlock> b, ArrayList<MineSign> si, Location p1, Location p2, Material i,
                     Location t, Boolean resetByPercentag, Boolean resetByTim, int rbpv, int rbtv, String color, HashMap<MineCuboid.CuboidDirection, Material> faces) {
        this.name = ChatColor.stripColor(Text.color(n));
        this.displayName = displayname;
        this.blocks = b;
        this.signs = si;
        this.icon = i;
        this.teleport = t;
        this.resetByPercentage = resetByPercentag;
        this.resetByTime = resetByTim;
        this.resetByPercentageValue = rbpv;
        this.resetByTimeValue = rbtv;
        this.faces = faces;

        this.setColor(color);

        this.timer = new MineTimer(this);
        if (this.resetByTime) {
            this.timer.start();
        }

        this.setPOS(p1, p2);
        this.updateSigns();
    }

    @Override
    public boolean hasFaceBlock(MineCuboid.CuboidDirection up) {
        return faces.get(up) != null;
    }

    @Override
    public String toString() {
        return "Mine{" +
                "name='" + name + '\'' +
                ", color=" + color +
                ", displayName='" + displayName + '\'' +
                ", blocks=" + blocks +
                ", sorted=" + sorted +
                ", signs=" + signs +
                ", teleport=" + teleport +
                ", icon=" + icon +
                ", mineCuboid=" + mineCuboid +
                ", resetByPercentage=" + resetByPercentage +
                ", resetByTime=" + resetByTime +
                ", resetByPercentageValue=" + resetByPercentageValue +
                ", resetByTimeValue=" + resetByTimeValue +
                ", timer=" + timer +
                ", highlight=" + highlight +
                ", l1=" + l1.toString() +
                ", l2=" + l2.toString() +
                '}';
    }

    @Override
    public String getColorIcon() {
        String color = "";

        switch (this.color) {
            case PURPLE:
                color = "&d";
                break;
            case RED:
                color = "&c";
                break;
            case BLUE:
                color = "&9";
                break;
            case GRAY:
                color = "&8";
                break;
            case BROWN:
                color = "&4";
                break;
            case GREEN:
                color = "&2";
                break;
            case WHITE:
                color = "&f";
                break;
            case ORANGE:
                color = "&6";
                break;
            case YELLOW:
                color = "&e";
                break;
        }

        return color + "●";
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public void setColor(Color c) {
        this.color = c;
    }

    @Override
    public void setColor(String s) {
        switch (s.toLowerCase()) {
            case "yellow":
                setColor(Color.YELLOW);
                break;
            case "orange":
                setColor(Color.ORANGE);
                break;
            case "red":
                setColor(Color.RED);
                break;
            case "green":
                setColor(Color.GREEN);
                break;
            case "gray":
                setColor(Color.GRAY);
                break;
            case "blue":
                setColor(Color.BLUE);
                break;
            case "brown":
                setColor(Color.BROWN);
                break;
            case "purple":
                setColor(Color.PURPLE);
                break;
            case "white":
            default:
                setColor(Color.WHITE);
                break;
        }
    }

    @Override
    public Location getPOS1() {
        return this.l1;
    }

    @Override
    public MineTimer getMineTimer() {
        return this.timer;
    }

    @Override
    public Location getPOS2() {
        return this.l2;
    }

    @Override
    public void setPOS(Location p1, Location p2) {
        this.l1 = p1;
        this.l2 = p2;
        this.mineCuboid = new MineCuboid(this.getPOS1(), this.getPOS2());

        this.fill();
    }

    @Override
    public boolean hasTP() {
        return this.teleport != null;
    }

    @Override
    public ArrayList<String> getSignList() {
        ArrayList<String> l = new ArrayList<>();
        signs.forEach(mineSign -> l.add(mineSign.getBlock().getWorld().getName() + ";" + mineSign.getBlock().getX() + ";" + mineSign.getBlock().getY() + ";" + mineSign.getBlock().getZ() + ";" + mineSign.getModifier()));
        return l;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(String input) {
        this.displayName = input;
        saveData(Data.NAME);
    }

    @Override
    public MineCuboid getMineCuboid() {
        return this.mineCuboid;
    }

    @Override
    public int getBlockCount() {
        return mineCuboid.getTotalVolume();
    }

    @Override
    public int getRemainingBlocks() {
        return this.mineCuboid.getTotalBlocks();
    }

    @Override
    public int getRemainingBlocksPer() {
        return (this.mineCuboid.getTotalBlocks() * 100 / getBlockCount());
    }

    @Override
    public int getMinedBlocks() {
        return getBlockCount() - getRemainingBlocks();
    }

    @Override
    public int getMinedBlocksPer() {
        return (getMinedBlocks() * 100 / getBlockCount());
    }

    @Override
    public void fill() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            this.sortBlocks();
            if (this.blocks.size() != 0) {
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

    /*
    private MineBlock getHighestPercentageBlock()
    {
        MineBlock ret = this.blocks.get(0);
        for (int i = 0; i < this.blocks.size(); ++i) {
            if (ret.getPercentage() < this.blocks.get(i).getPercentage())
            {
                ret = this.blocks.get(i);
            }
        }
        return ret;
    }*/

    private void sortBlocks() {
        this.sorted.clear();

        for (MineBlock d : this.blocks) {
            double percentage = d.getPercentage() * getBlockCount();

            for (int i = 0; i < (int) percentage; ++i)
            {
                if (this.sorted.size() != this.getBlockCount()) {
                    this.sorted.add(d.getMaterial());
                }
            }
        }

        /*
        if (this.sorted.size() != getBlockCount())
        {
            MineBlock higher = getHighestPercentageBlock();
            for (int i = 0; i < getBlockCount() - this.sorted.size(); ++i)
            {
                this.sorted.add(higher.getMaterial());
            }
        }*/

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

    @Override
    public void register() {
        RealMines.getInstance().getMineManager().add(this);
    }

    @Override
    public void saveData(Data t) {
        RealMines.getInstance().getMineManager().saveMine(this, t);
        if (!this.resetByTime) {
            this.timer.kill();
        } else {
            this.timer.restart();
        }
    }

    @Override
    public void saveAll() {
        RealMines.getInstance().getMineManager().saveMine(this);
        if (!this.resetByTime) {
            this.timer.kill();
        } else {
            this.timer.restart();
        }
    }

    @Override
    public ArrayList<String> getBlockList() {
        ArrayList<String> l = new ArrayList<>();
        this.blocks.forEach(mineBlock -> l.add(mineBlock.getMaterial().name() + ";" + mineBlock.getPercentage()));
        return l;
    }

    @Override
    public ArrayList<MineBlockIcon> getBlocks() {
        ArrayList<MineBlockIcon> l = new ArrayList<>();
        blocks.forEach(mineBlock -> l.add(new MineBlockIcon(mineBlock)));
        if (l.size() == 0) {
            l.add(new MineBlockIcon());
        }
        return l;
    }

    @Override
    public void reset() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            kickPlayers(Language.file().getString("Mines.Reset.Starting").replace("%mine%", this.getDisplayName()));
            this.fill();
            this.updateSigns();
            if (!isSilent()) {
                Bukkit.broadcastMessage(Text.color(RealMines.getInstance().getPrefix() + Language.file().getString("Mines.Reset.Announcement").replace("%mine%", getDisplayName())));
            }
        }
    }

    @Override
    public void setSilent(boolean b) {
        Mines.file().set(this.name + ".Settings.Reset.Silent", b);
    }

    @Override
    public boolean isSilent() {
        return Mines.file().getBoolean(this.name + ".Settings.Reset.Silent");
    }

    @Override
    public void addSign(Block block, String modif) {
        this.signs.add(new MineSign(block, modif));
        this.saveData(Data.SIGNS);
    }

    @Override
    public void updateSigns() {
        Bukkit.getScheduler().runTask(RealMines.getInstance(), () -> {
            for (MineSign ms : this.signs) {
                if (ms.getBlock().getType().name().contains("SIGN")) {
                    Sign sign = (Sign) ms.getBlock().getState();
                    String modif = ms.getModifier();

                    switch (modif.toLowerCase()) {
                        case "pm":
                            sign.setLine(1, getMinedBlocksPer() + "%");
                            sign.setLine(2, "mined on");
                            break;
                        case "bm":
                            sign.setLine(1, "" + getMinedBlocks());
                            sign.setLine(2, "mined blocks on");
                            break;
                        case "br":
                            sign.setLine(1, "" + getRemainingBlocks());
                            sign.setLine(2, "blocks on");
                            break;
                        case "pl":
                            sign.setLine(1, getRemainingBlocksPer() + "%");
                            sign.setLine(2, "left on");
                            break;
                    }

                    sign.setLine(0, RealMines.getInstance().getPrefix());
                    sign.setLine(3, "" + Text.color(getDisplayName()));
                    sign.update();
                }
            }
        });
    }

    @Override
    public void removeBlock(MineBlock mb) {
        blocks.remove(mb);
        saveData(Data.BLOCKS);
    }

    @Override
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

    @Override
    public void clear() {
        this.mineCuboid.forEach(block -> block.setType(Material.AIR));
    }

    @Override
    public void kickPlayers(String s) {
        this.getPlayersInMine().forEach(player -> RealMines.getInstance().getMineManager().teleport(player, this, false));
        broadcastMessage(s, true);
    }

    @Override
    public void broadcastMessage(String s, Boolean prefix) {
        if (prefix) {
            s = RealMines.getInstance().getPrefix() + s;
        }
        String finalS = s;
        getPlayersInMine().forEach(player -> player.sendMessage(Text.color(finalS)));
    }

    @Override
    public ArrayList<Player> getPlayersInMine() {
        ArrayList<Player> ps = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (mineCuboid.contains(p.getLocation())) {
                ps.add(p);
            }
        }
        return ps;
    }

    @Override
    public void removeDependencies() {
        this.signs.forEach(ms -> ms.getBlock().getLocation().getWorld().getBlockAt(ms.getBlock().getLocation()).setType(Material.AIR));
    }

    @Override
    public List<Location> getCube() {
        List<Location> result = new ArrayList<>();
        World world = this.mineCuboid.getPOS1().getWorld();
        double minX = Math.min(this.mineCuboid.getPOS1().getX(), this.mineCuboid.getPOS2().getX());
        double minY = Math.min(this.mineCuboid.getPOS1().getY(), this.mineCuboid.getPOS2().getY());
        double minZ = Math.min(this.mineCuboid.getPOS1().getZ(), this.mineCuboid.getPOS2().getZ());
        double maxX = Math.max(this.mineCuboid.getPOS1().getX() + 1, this.mineCuboid.getPOS2().getX() + 1);
        double maxY = Math.max(this.mineCuboid.getPOS1().getY() + 1, this.mineCuboid.getPOS2().getY() + 1);
        double maxZ = Math.max(this.mineCuboid.getPOS1().getZ() + 1, this.mineCuboid.getPOS2().getZ() + 1);
        double dist = 0.5D;
        for (double x = minX; x <= maxX; x += dist) {
            for (double y = minY; y <= maxY; y += dist) {
                for (double z = minZ; z <= maxZ; z += dist) {
                    int components = 0;
                    if (x == minX || x == maxX) components++;
                    if (y == minY || y == maxY) components++;
                    if (z == minZ || z == maxZ) components++;
                    if (components >= 2) {
                        result.add(new Location(world, x, y, z));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void highlight() {
        if (this.highlight) {
            this.getCube().forEach(location -> location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(), 0, 0.001, 1, 0, 1, new Particle.DustOptions(convertColor(this.color), 1)));
        }
    }

    private org.bukkit.Color convertColor(Color color) {
        switch (color) {
            case RED:
                return org.bukkit.Color.RED;
            case YELLOW:
                return org.bukkit.Color.YELLOW;
            case GRAY:
                return org.bukkit.Color.GRAY;
            case GREEN:
                return org.bukkit.Color.GREEN;
            case BLUE:
                return org.bukkit.Color.fromRGB(51, 153, 255);
            case WHITE:
                return org.bukkit.Color.WHITE;
            case BROWN:
                return org.bukkit.Color.fromRGB(153, 102, 51);
            case ORANGE:
                return org.bukkit.Color.ORANGE;
            case PURPLE:
                return org.bukkit.Color.PURPLE;
        }
        return org.bukkit.Color.fromRGB(0, 153, 204);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isResetBy(Reset e) {
        switch (e) {
            case PERCENTAGE:
                return this.resetByPercentage;
            case TIME:
                return this.resetByTime;
        }
        return false;
    }

    @Override
    public int getResetValue(Reset e) {
        switch (e) {
            case PERCENTAGE:
                return this.resetByPercentageValue;
            case TIME:
                return this.resetByTimeValue;
        }
        return -1;
    }

    @Override
    public void setResetStatus(Reset e, boolean b) {
        switch (e) {
            case PERCENTAGE:
                this.resetByPercentage = b;
            case TIME:
                this.resetByTime = b;
        }
    }

    @Override
    public void setResetValue(Reset e, int d) {
        switch (e) {
            case PERCENTAGE:
                this.resetByPercentageValue = d;
            case TIME:
                this.resetByTimeValue = d;
        }
    }

    @Override
    public Material getIcon() {
        return this.icon;
    }

    @Override
    public void setIcon(Material a) {
        this.icon = a;
    }

    @Override
    public boolean isHighlighted() {
        return this.highlight;
    }

    @Override
    public void setHighlight(boolean b) {
        this.highlight = b;
    }

    @Override
    public Location getTeleport() {
        return this.teleport;
    }

    @Override
    public void setTeleport(Location location) {
        this.teleport = location;
    }

    @Override
    public ArrayList<MineSign> getSigns() {
        return this.signs;
    }

    @Override
    public MineTimer getTimer() {
        return this.timer;
    }

    @Override
    public Material getFaceBlock(MineCuboid.CuboidDirection up) {
        return this.faces.get(up);
    }

    @Override
    public void setFaceBlock(MineCuboid.CuboidDirection cd, Material a) {
        this.faces.put(cd, a);
        this.saveData(Data.FACES);
    }

    @Override
    public void removeFaceblock(MineCuboid.CuboidDirection d) {
        this.faces.remove(d);
        this.saveData(Data.FACES);
    }

    @Override
    public HashMap<MineCuboid.CuboidDirection, Material> getFaces() {
        return this.faces;
    }

    @Override
    public Type getType() {
        return Type.BLOCKS;
    }

    @Override
    public Location getSchematicPlace() {
        return new Location(this.mineCuboid.getWorld(), 0, 0, 0);
    }

    @Override
    public String getSchematicFilename() {
        return "ThisIsABlockMine";
    }

}
