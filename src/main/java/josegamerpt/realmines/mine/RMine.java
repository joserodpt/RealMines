package josegamerpt.realmines.mine;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mine.component.MineCuboid;
import josegamerpt.realmines.mine.component.MineSign;
import josegamerpt.realmines.mine.task.MineTimer;
import josegamerpt.realmines.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
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

    protected int minedBlocks;

    public RMine(final String n, final String displayname, final ArrayList<MineSign> si, final Material i,
                 final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final String color, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent) {
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

    public void setColor(final Color c) {
        this.color = c;
    }

    public void setColor(final String s) {
        switch (s.toLowerCase()) {
            case "yellow":
                this.setColor(Color.YELLOW);
                break;
            case "orange":
                this.setColor(Color.ORANGE);
                break;
            case "red":
                this.setColor(Color.RED);
                break;
            case "green":
                this.setColor(Color.GREEN);
                break;
            case "gray":
                this.setColor(Color.GRAY);
                break;
            case "blue":
                this.setColor(Color.BLUE);
                break;
            case "brown":
                this.setColor(Color.BROWN);
                break;
            case "purple":
                this.setColor(Color.PURPLE);
                break;
            case "white":
            default:
                this.setColor(Color.WHITE);
                break;
        }
    }

    public boolean hasFaceBlock(final MineCuboid.CuboidDirection up) {
        return this.faces.get(up) != null;
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

    public void setPOS(final Location p1, final Location p2) {
        this.l1 = p1;
        this.l2 = p2;
        this.mineCuboid = new MineCuboid(this.getPOS1(), this.getPOS2());

        this.fill();
    }

    public boolean hasTP() {
        return this.teleport != null;
    }

    public ArrayList<String> getSignList() {
        final ArrayList<String> l = new ArrayList<>();
        this.signs.forEach(mineSign -> l.add(mineSign.getBlock().getWorld().getName() + ";" + mineSign.getBlock().getX() + ";" + mineSign.getBlock().getY() + ";" + mineSign.getBlock().getZ() + ";" + mineSign.getModifier()));
        return l;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(final String input) {
        this.displayName = input;
        this.saveData(Data.NAME);
    }

    public MineCuboid getMineCuboid() {
        return this.mineCuboid;
    }

    //block counts
    public int getBlockCount() {
        return this.mineCuboid.getTotalBlocks();
    }

    public int getMinedBlocks() {
        return this.minedBlocks;
    }

    public int getRemainingBlocks() {
        return this.mineCuboid.getTotalBlocks() - this.getMinedBlocks();
    }
    //block counts

    //block percentages
    public int getRemainingBlocksPer() {
        return (this.getRemainingBlocks() * 100 / this.getBlockCount());
    }

    public int getMinedBlocksPer() {
        return (this.getMinedBlocks() * 100 / this.getBlockCount());
    }
    //block percentages

    public abstract void fill();

    public void register() {
        RealMines.getInstance().getMineManager().add(this);
    }

    public void saveData(final Data t) {
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
            this.kickPlayers(Language.file().getString("Mines.Reset.Starting").replace("%mine%", this.getDisplayName()));
            this.fill();

            //reset mined blocks
            this.minedBlocks = 0;

            this.updateSigns();
            if (!this.isSilent()) {
                Bukkit.broadcastMessage(Text.color(RealMines.getInstance().getPrefix() + Language.file().getString("Mines.Reset.Announcement").replace("%mine%", this.getDisplayName())));
            }
        }
    }

    public void addSign(final Block block, final String modif) {
        this.signs.add(new MineSign(block, modif));
        this.saveData(Data.SIGNS);
    }

    public void updateSigns() {
        Bukkit.getScheduler().runTask(RealMines.getInstance(), () -> {
            for (final MineSign ms : this.signs) {
                if (ms.getBlock().getType().name().contains("SIGN")) {
                    final Sign sign = (Sign) ms.getBlock().getState();
                    final String modif = ms.getModifier();

                    switch (modif.toLowerCase()) {
                        case "pm":
                            sign.setLine(1, this.getMinedBlocksPer() + "%");
                            sign.setLine(2, "mined on");
                            break;
                        case "bm":
                            sign.setLine(1, String.valueOf(this.getMinedBlocks()));
                            sign.setLine(2, "mined blocks on");
                            break;
                        case "br":
                            sign.setLine(1, String.valueOf(this.getRemainingBlocks()));
                            sign.setLine(2, "blocks on");
                            break;
                        case "pl":
                            sign.setLine(1, this.getRemainingBlocksPer() + "%");
                            sign.setLine(2, "left on");
                            break;
                    }

                    sign.setLine(0, RealMines.getInstance().getPrefix());
                    sign.setLine(3, Text.color(this.getDisplayName()));
                    sign.update();
                }
            }
        });
    }

    public void clear() {
        this.mineCuboid.forEach(block -> block.setType(Material.AIR));
    }

    public boolean isSilent() {
        return this.silent;
    }

    public void kickPlayers(final String s) {
        this.getPlayersInMine().forEach(player -> RealMines.getInstance().getMineManager().teleport(player, this, this.isSilent()));
        if (!this.isSilent()) {
            this.broadcastMessage(s, true);
        }
    }

    public void broadcastMessage(String s, final Boolean prefix) {
        if (prefix) {
            s = RealMines.getInstance().getPrefix() + s;
        }
        final String finalS = s;
        this.getPlayersInMine().forEach(player -> player.sendMessage(Text.color(finalS)));
    }

    public ArrayList<Player> getPlayersInMine() {
        final ArrayList<Player> ps = new ArrayList<>();
        for (final Player p : Bukkit.getOnlinePlayers()) {
            if (this.mineCuboid.contains(p.getLocation())) {
                ps.add(p);
            }
        }
        return ps;
    }

    public void removeDependencies() {
        this.signs.forEach(ms -> ms.getBlock().getLocation().getWorld().getBlockAt(ms.getBlock().getLocation()).setType(Material.AIR));
    }

    public List<Location> getCube() {
        final List<Location> result = new ArrayList<>();
        final World world = this.mineCuboid.getPOS1().getWorld();
        final double minX = Math.min(this.mineCuboid.getPOS1().getX(), this.mineCuboid.getPOS2().getX());
        final double minY = Math.min(this.mineCuboid.getPOS1().getY(), this.mineCuboid.getPOS2().getY());
        final double minZ = Math.min(this.mineCuboid.getPOS1().getZ(), this.mineCuboid.getPOS2().getZ());
        final double maxX = Math.max(this.mineCuboid.getPOS1().getX() + 1, this.mineCuboid.getPOS2().getX() + 1);
        final double maxY = Math.max(this.mineCuboid.getPOS1().getY() + 1, this.mineCuboid.getPOS2().getY() + 1);
        final double maxZ = Math.max(this.mineCuboid.getPOS1().getZ() + 1, this.mineCuboid.getPOS2().getZ() + 1);
        final double dist = 0.5D;
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
            this.getCube().forEach(location -> location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(), 0, 0.001, 1, 0, 1, new Particle.DustOptions(this.convertColor(this.color), 1)));
        }
    }

    private org.bukkit.Color convertColor(final Color color) {
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

    public boolean isResetBy(final Reset e) {
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

    public int getResetValue(final Reset e) {
        switch (e) {
            case PERCENTAGE:
                return this.resetByPercentageValue;
            case TIME:
                return this.resetByTimeValue;
        }
        return -1;
    }

    public void setResetStatus(final Reset e, final boolean b) {
        switch (e) {
            case PERCENTAGE:
                this.resetByPercentage = b;
            case TIME:
                this.resetByTime = b;
            case SILENT:
                this.silent = b;
        }
    }

    public void setResetValue(final Reset e, final int d) {
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

    public void setIcon(final Material a) {
        this.icon = a;
    }

    public boolean isHighlighted() {
        return this.highlight;
    }

    public void setHighlight(final boolean b) {
        this.highlight = b;
    }

    public Location getTeleport() {
        return this.teleport;
    }

    public void setTeleport(final Location location) {
        this.teleport = location;
    }

    public ArrayList<MineSign> getSigns() {
        return this.signs;
    }

    public MineTimer getTimer() {
        return this.timer;
    }

    public Material getFaceBlock(final MineCuboid.CuboidDirection up) {
        return this.faces.get(up);
    }

    public void setFaceBlock(final MineCuboid.CuboidDirection cd, final Material a) {
        this.faces.put(cd, a);
        this.saveData(Data.FACES);
    }

    public void removeFaceblock(final MineCuboid.CuboidDirection d) {
        this.faces.remove(d);
        this.saveData(Data.FACES);
    }

    public HashMap<MineCuboid.CuboidDirection, Material> getFaces() {
        return this.faces;
    }

    public abstract String getType();

    public void processBlockBreakEvent(final boolean broken) {
        //add or remove to mined blocks
        this.minedBlocks = this.minedBlocks + (broken ? 1 : -1);

        //if mine reset percentage is lower, reset it
        if (this.isResetBy(RMine.Reset.PERCENTAGE) & ((double) this.getRemainingBlocksPer() < this.getResetValue(RMine.Reset.PERCENTAGE))) {
            this.kickPlayers(Language.file().getString("Mines.Reset.Percentage"));
            Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.getInstance(), this::reset, 10);
        }

        //update min e signs
        this.updateSigns();
    }

    public enum Reset {PERCENTAGE, TIME, SILENT}

    public enum Data {BLOCKS, ICON, TELEPORT, SIGNS, PLACE, OPTIONS, NAME, FACES, COLOR, MINE_TYPE}

    public enum Color {YELLOW, ORANGE, RED, GREEN, WHITE, GRAY, BLUE, PURPLE, BROWN}
}
