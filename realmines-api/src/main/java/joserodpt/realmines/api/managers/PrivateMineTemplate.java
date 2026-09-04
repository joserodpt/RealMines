package joserodpt.realmines.api.managers;

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

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.PrivateMineData;
import joserodpt.realmines.api.utils.WorldEditUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A frozen snapshot of a mine plus the rules for handing copies of it out to players.
 * <p>
 * The file is an ordinary mine file with one extra {@code template:} section, so claiming a private mine
 * is just a matter of copying the file, moving its coordinates onto a free grid slot and handing the
 * result to the usual mine constructors.
 */
public class PrivateMineTemplate {

    public static final String ROOT = "template";

    /**
     * Hard ceiling on {@code trusted-limit}, set by how many heads the manage GUI can display.
     */
    public static final int MAX_TRUSTED = 7;

    private final String id;
    private final File file;
    private final YamlConfiguration snapshot;

    private final String displayName;
    private final Material icon;
    private final List<String> description;
    private final String sourceMine;
    private final String permission;
    private final double cost, renewCost;
    private final PrivateMineData.Lifecycle lifecycle;
    private final long duration;
    private final int trustedLimit;
    private final Placement placement;

    public PrivateMineTemplate(final String id, final File file, final YamlConfiguration snapshot) {
        this.id = id;
        this.file = file;
        this.snapshot = snapshot;

        final ConfigurationSection s = snapshot.getConfigurationSection(ROOT);
        final ConfigurationSection t = s == null ? snapshot.createSection(ROOT) : s;

        this.displayName = t.getString("display-name", "&b%player%'s Mine");
        this.icon = parseMaterial(t.getString("icon"), Material.DIAMOND_ORE);
        this.description = t.getStringList("description");
        this.sourceMine = t.getString("source-mine", "");
        this.permission = t.getString("permission", "");
        this.cost = t.getDouble("cost", 0D);
        this.renewCost = t.getDouble("renew-cost", 0D);
        this.lifecycle = PrivateMineData.Lifecycle.fromString(t.getString("lifecycle"), PrivateMineData.Lifecycle.PERSISTENT);
        this.duration = t.getLong("duration", 3600L);
        //capped at what the manage GUI has room for: trusted players it can't show are ones the owner
        //could never remove again
        this.trustedLimit = Math.max(0, Math.min(MAX_TRUSTED, t.getInt("trusted-limit", 5)));
        this.placement = new Placement(t.getConfigurationSection("placement"));
    }

    private static Material parseMaterial(final String name, final Material def) {
        if (name == null || name.isEmpty()) {
            return def;
        }
        final Material m = Material.matchMaterial(name);
        return m == null ? def : m;
    }

    public String getID() {
        return this.id;
    }

    public File getFile() {
        return this.file;
    }

    /**
     * A fresh copy of the snapshot, safe to mutate. Read from disk each time so that two claims never
     * share a config object.
     */
    public YamlConfiguration copySnapshot() {
        return YamlConfiguration.loadConfiguration(this.file);
    }

    public String getDisplayNameFor(final String playerName) {
        return this.displayName.replace("%player%", playerName);
    }

    public String getRawDisplayName() {
        return this.displayName;
    }

    public Material getIcon() {
        return this.icon;
    }

    public List<String> getDescription() {
        return this.description == null ? Collections.emptyList() : this.description;
    }

    public String getSourceMine() {
        return this.sourceMine;
    }

    public String getPermission() {
        return this.permission;
    }

    public boolean hasPermission() {
        return this.permission != null && !this.permission.isEmpty();
    }

    public double getCost() {
        return this.cost;
    }

    public double getRenewCost() {
        return this.renewCost;
    }

    public PrivateMineData.Lifecycle getLifecycle() {
        return this.lifecycle;
    }

    public long getDuration() {
        return this.duration;
    }

    public int getTrustedLimit() {
        return this.trustedLimit;
    }

    public Placement getPlacement() {
        return this.placement;
    }

    public RMine.Type getMineType() {
        try {
            return RMine.Type.valueOf(this.snapshot.getString("type", "BLOCKS"));
        } catch (final IllegalArgumentException e) {
            return RMine.Type.BLOCKS;
        }
    }

    public int getResetTime() {
        return this.snapshot.getInt("reset.time.value", 120);
    }

    /**
     * The snapshot's two corners, as raw block coordinates. Read straight from config rather than through
     * a {@link joserodpt.realmines.api.mine.components.MineCuboid}, because that resolves the world and
     * throws when the mine was snapshotted in a world that is no longer loaded.
     */
    public int[] getSnapshotBounds() {
        final int[] p1 = parsePos(this.snapshot.getString("pos1"));
        final int[] p2 = parsePos(this.snapshot.getString("pos2"));
        if (p1 == null || p2 == null) {
            return null;
        }
        return new int[]{
                Math.min(p1[0], p2[0]), Math.min(p1[1], p2[1]), Math.min(p1[2], p2[2]),
                Math.max(p1[0], p2[0]), Math.max(p1[1], p2[1]), Math.max(p1[2], p2[2])};
    }

    public static int[] parsePos(final String s) {
        if (s == null) {
            return null;
        }
        final String[] parts = s.split(";");
        if (parts.length < 3) {
            return null;
        }
        try {
            return new int[]{
                    (int) Math.floor(Double.parseDouble(parts[0])),
                    (int) Math.floor(Double.parseDouble(parts[1])),
                    (int) Math.floor(Double.parseDouble(parts[2]))};
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    /**
     * Problems that would make claiming from this template fail or silently produce overlapping mines.
     * Reported once at load so the admin sees them in the console instead of when a player complains.
     */
    public List<String> validate() {
        final List<String> problems = new ArrayList<>();

        if (this.getMineType() == RMine.Type.SCHEMATIC) {
            problems.add("schematic mines can't be used as private mine templates");
        }

        final int[] bounds = this.getSnapshotBounds();
        if (bounds == null) {
            problems.add("the snapshot has no usable pos1/pos2");
        }

        if (this.placement.getPerRow() <= 0) {
            problems.add("placement.per-row has to be at least 1");
        }

        final World world = this.placement.getWorld();
        if (world == null) {
            problems.add("the private mines world '" + PrivateMinesWorld.NAME
                    + "' couldn't be created - see the console");
        } else if (bounds != null) {
            //instances are moved so their lowest corner sits on the placement origin, and the platform
            //takes the layer under the mine plus whatever its fence stands above the top
            final int lowY = this.placement.getOriginY()
                    - (this.placement.hasPlatform() ? PrivateMinePlatform.REACH_BELOW : 0);
            final int highY = this.placement.getOriginY() + (bounds[4] - bounds[1])
                    + (this.placement.hasPlatform() ? PrivateMinePlatform.reachAbove(bounds[4] - bounds[1] + 1) : 0);
            if (lowY < world.getMinHeight() || highY >= world.getMaxHeight()) {
                problems.add("placed at origin Y " + this.placement.getOriginY() + " the mine and its platform"
                        + " would span Y " + lowY + " to " + highY + ", which doesn't fit in world '"
                        + world.getName() + "' (" + world.getMinHeight() + " to " + (world.getMaxHeight() - 1) + ")");
            }
        }

        if (bounds != null) {
            int sizeX = bounds[3] - bounds[0] + 1;
            int sizeZ = bounds[5] - bounds[2] + 1;

            //the shell is pasted at the same origin, so if it is wider than the mine it is the shell that
            //decides how far apart slots have to be - otherwise it paints over the next owner's mine
            if (this.placement.hasShellSchematic()) {
                final Clipboard shell = WorldEditUtils.loadSchematic(this.placement.getShellSchematic());
                if (shell == null) {
                    problems.add("shell schematic '" + this.placement.getShellSchematic() + "' couldn't be read");
                } else {
                    final BlockVector3 size = shell.getRegion().getMaximumPoint()
                            .subtract(shell.getRegion().getMinimumPoint()).add(1, 1, 1);
                    sizeX = Math.max(sizeX, size.getBlockX());
                    sizeZ = Math.max(sizeZ, size.getBlockZ());
                }
            }

            //the platform is built around every copy, so neighbouring slots have to clear it as well
            final int margin = this.placement.getPlatformMargin() * 2;
            if (this.placement.getSpacingX() <= sizeX + margin) {
                problems.add("placement.spacing-x (" + this.placement.getSpacingX() + ") has to be bigger than the mine's"
                        + " X size plus its platform (" + (sizeX + margin) + ")");
            }
            if (this.placement.getSpacingZ() <= sizeZ + margin) {
                problems.add("placement.spacing-z (" + this.placement.getSpacingZ() + ") has to be bigger than the mine's"
                        + " Z size plus its platform (" + (sizeZ + margin) + ")");
            }
        }

        return problems;
    }

    /**
     * Where the grid slot numbered {@code slot} starts. Slots fill a row left to right and then wrap.
     */
    public Location getSlotOrigin(final int slot) {
        final World world = this.placement.getWorld();
        if (world == null) {
            return null;
        }
        final int perRow = Math.max(1, this.placement.getPerRow());
        return new Location(world,
                this.placement.getOriginX() + (long) (slot % perRow) * this.placement.getSpacingX(),
                this.placement.getOriginY(),
                this.placement.getOriginZ() + (long) (slot / perRow) * this.placement.getSpacingZ());
    }

    /**
     * Where the private mine instances of this template are put in the world.
     */
    public static class Placement {

        private final int originX, originY, originZ;
        private final int spacingX, spacingZ, perRow;
        private final String shellSchematic;
        private final int platformWidth;

        Placement(final ConfigurationSection section) {
            if (section == null) {
                this.originX = 0;
                this.originY = 64;
                this.originZ = 0;
                this.spacingX = 200;
                this.spacingZ = 200;
                this.perRow = 20;
                this.shellSchematic = "";
                this.platformWidth = PrivateMinePlatform.DEFAULT_WIDTH;
                return;
            }

            final int[] origin = parsePos(section.getString("origin", "0;64;0"));
            this.originX = origin == null ? 0 : origin[0];
            this.originY = origin == null ? 64 : origin[1];
            this.originZ = origin == null ? 0 : origin[2];
            this.spacingX = section.getInt("spacing-x", 200);
            this.spacingZ = section.getInt("spacing-z", 200);
            this.perRow = section.getInt("per-row", 20);
            this.shellSchematic = section.getString("shell-schematic", "");

            //what the walkway is made of is a server wide setting; a template only says how wide it is,
            //and a width of 0 is how it asks for no platform at all
            this.platformWidth = Math.max(0, Math.min(PrivateMinePlatform.MAX_WIDTH,
                    section.getInt("platform-width", PrivateMinePlatform.DEFAULT_WIDTH)));
        }

        /**
         * Always {@link PrivateMinesWorld#NAME}: private mines are not placed in worlds the server owns.
         */
        public String getWorldName() {
            return PrivateMinesWorld.NAME;
        }

        /**
         * The private mines world, created on the spot if this is the first time it is needed.
         */
        public World getWorld() {
            return PrivateMinesWorld.get();
        }

        public int getOriginX() {
            return this.originX;
        }

        public int getOriginY() {
            return this.originY;
        }

        public int getOriginZ() {
            return this.originZ;
        }

        public int getSpacingX() {
            return this.spacingX;
        }

        public int getSpacingZ() {
            return this.spacingZ;
        }

        public int getPerRow() {
            return this.perRow;
        }

        public String getShellSchematic() {
            return this.shellSchematic;
        }

        public boolean hasShellSchematic() {
            return this.shellSchematic != null && !this.shellSchematic.isEmpty();
        }

        public int getPlatformWidth() {
            return this.platformWidth;
        }

        public boolean hasPlatform() {
            return this.platformWidth > 0;
        }

        /**
         * How far past the mine the platform reaches on every side, which is what the grid has to space
         * copies out by on top of the mine's own size.
         */
        public int getPlatformMargin() {
            return this.hasPlatform() ? PrivateMinePlatform.margin(this.platformWidth) : 0;
        }
    }
}
