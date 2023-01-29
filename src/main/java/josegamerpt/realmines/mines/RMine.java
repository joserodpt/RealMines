package josegamerpt.realmines.mines;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mines.components.MineCuboid;
import josegamerpt.realmines.mines.components.MineSign;
import josegamerpt.realmines.mines.tasks.MineTimer;
import josegamerpt.realmines.utils.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class RMine {

    protected String name;
    protected Color color = Color.WHITE;
    protected String displayName;
    protected ArrayList<MineSign> signs;
    protected Location teleport;
    protected Material icon;
    protected MineCuboid mineCuboid;
    protected boolean resetByPercentage;
    protected boolean resetByTime;
    protected int resetByPercentageValue;
    protected int resetByTimeValue;
    protected MineTimer timer;
    protected boolean highlight = false;
    protected Location l1;
    protected Location l2;
    protected boolean silent;
    protected HashMap<MineCuboid.CuboidDirection, Material> faces;

    public RMine(String n, String displayname, ArrayList<MineSign> si, Material i,
                 Location t, Boolean resetByPercentag, Boolean resetByTim, int rbpv, int rbtv, String color, HashMap<MineCuboid.CuboidDirection, Material> faces, boolean silent) {
        this.name = ChatColor.stripColor(Text.color(n));
        this.displayName = displayname;
        this.signs = si;
        this.icon = i;
        this.teleport = t;
        this.resetByPercentage = resetByPercentag;
        this.resetByTime = resetByTim;
        this.resetByPercentageValue = rbpv;
        this.resetByTimeValue = rbtv;
        this.silent = silent;
        this.faces = faces;

        this.setColor(color);

        this.timer = new MineTimer(this);
        if (this.resetByTime) {
            this.timer.start();
        }
    }
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

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color c) {
        this.color = c;
    }

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

    public boolean hasFaceBlock(MineCuboid.CuboidDirection up) {
        return faces.get(up) != null;
    }

    public Location getPOS1() {
        return this.l1;
    }

    public MineTimer getMineTimer() {
        return this.timer;
    }

    public Location getPOS2() {
        return this.l2;
    }

    public void setPOS(Location p1, Location p2) {
        this.l1 = p1;
        this.l2 = p2;
        this.mineCuboid = new MineCuboid(this.getPOS1(), this.getPOS2());

        this.fill();
    }

    public boolean hasTP() {
        return this.teleport != null;
    }

    public ArrayList<String> getSignList() {
        ArrayList<String> l = new ArrayList<>();
        signs.forEach(mineSign -> l.add(mineSign.getBlock().getWorld().getName() + ";" + mineSign.getBlock().getX() + ";" + mineSign.getBlock().getY() + ";" + mineSign.getBlock().getZ() + ";" + mineSign.getModifier()));
        return l;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String input) {
        this.displayName = input;
        saveData(Data.NAME);
    }

    public MineCuboid getMineCuboid() {
        return this.mineCuboid;
    }

    public int getBlockCount() {
        return mineCuboid.getTotalVolume();
    }

    public int getRemainingBlocks() {
        return this.mineCuboid.getTotalBlocks();
    }

    public int getRemainingBlocksPer() {
        return (this.mineCuboid.getTotalBlocks() * 100 / getBlockCount());
    }

    public int getMinedBlocks() {
        return getBlockCount() - getRemainingBlocks();
    }

    public int getMinedBlocksPer() {
        return (getMinedBlocks() * 100 / getBlockCount());
    }

    public abstract void fill();

    public void register() {
        RealMines.getInstance().getMineManager().add(this);
    }

    public void saveData(Data t) {
        RealMines.getInstance().getMineManager().saveMine(this, t);
        if (!this.resetByTime) {
            this.timer.kill();
        } else {
            this.timer.restart();
        }
    }

    public void saveAll() {
        RealMines.getInstance().getMineManager().saveMine(this);
        if (!this.resetByTime) {
            this.timer.kill();
        } else {
            this.timer.restart();
        }
    }

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

    public void addSign(Block block, String modif) {
        this.signs.add(new MineSign(block, modif));
        this.saveData(Data.SIGNS);
    }

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

    public void clear() {
        this.mineCuboid.forEach(block -> block.setType(Material.AIR));
    }

    public boolean isSilent() {
        return silent;
    }

    public void kickPlayers(String s) {
        this.getPlayersInMine().forEach(player -> RealMines.getInstance().getMineManager().teleport(player, this, false));
        broadcastMessage(s, true);
    }

    public void broadcastMessage(String s, Boolean prefix) {
        if (prefix) {
            s = RealMines.getInstance().getPrefix() + s;
        }
        String finalS = s;
        getPlayersInMine().forEach(player -> player.sendMessage(Text.color(finalS)));
    }

    public ArrayList<Player> getPlayersInMine() {
        ArrayList<Player> ps = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (mineCuboid.contains(p.getLocation())) {
                ps.add(p);
            }
        }
        return ps;
    }

    public void removeDependencies() {
        this.signs.forEach(ms -> ms.getBlock().getLocation().getWorld().getBlockAt(ms.getBlock().getLocation()).setType(Material.AIR));
    }

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

    public String getName() {
        return this.name;
    }

    public boolean isResetBy(Reset e) {
        switch (e) {
            case PERCENTAGE:
                return this.resetByPercentage;
            case TIME:
                return this.resetByTime;
            case SILENT:
                return this.silent;
        }
        return false;
    }

    public int getResetValue(Reset e) {
        switch (e) {
            case PERCENTAGE:
                return this.resetByPercentageValue;
            case TIME:
                return this.resetByTimeValue;
        }
        return -1;
    }

    public void setResetStatus(Reset e, boolean b) {
        switch (e) {
            case PERCENTAGE:
                this.resetByPercentage = b;
            case TIME:
                this.resetByTime = b;
            case SILENT:
                this.silent = b;
        }
    }

    public void setResetValue(Reset e, int d) {
        switch (e) {
            case PERCENTAGE:
                this.resetByPercentageValue = d;
            case TIME:
                this.resetByTimeValue = d;
        }
    }

    public Material getIcon() {
        return this.icon;
    }

    public void setIcon(Material a) {
        this.icon = a;
    }

    public boolean isHighlighted() {
        return this.highlight;
    }

    public void setHighlight(boolean b) {
        this.highlight = b;
    }

    public Location getTeleport() {
        return this.teleport;
    }

    public void setTeleport(Location location) {
        this.teleport = location;
    }

    public ArrayList<MineSign> getSigns() {
        return this.signs;
    }

    public MineTimer getTimer() {
        return this.timer;
    }

    public Material getFaceBlock(MineCuboid.CuboidDirection up) {
        return this.faces.get(up);
    }

    public void setFaceBlock(MineCuboid.CuboidDirection cd, Material a) {
        this.faces.put(cd, a);
        this.saveData(Data.FACES);
    }

    public void removeFaceblock(MineCuboid.CuboidDirection d) {
        this.faces.remove(d);
        this.saveData(Data.FACES);
    }

    public HashMap<MineCuboid.CuboidDirection, Material> getFaces() {
        return this.faces;
    }

    public abstract String getType();

    public enum Reset {PERCENTAGE, TIME, SILENT}

    public enum Data {BLOCKS, ICON, TELEPORT, SIGNS, PLACE, OPTIONS, NAME, FACES, COLOR, MINE_TYPE}

    public enum Color {YELLOW, ORANGE, RED, GREEN, WHITE, GRAY, BLUE, PURPLE, BROWN}
}
