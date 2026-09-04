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

import joserodpt.realmines.api.config.RMPrivateMinesConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * The ledge a private mine is handed out with.
 * <p>
 * {@link PrivateMinesWorld} is empty, so a mine pasted into it is a block of stone floating in nothing.
 * This builds what makes it usable: a walkway around the mine one block under its lowest layer, so whoever
 * arrives stands level with the bottom of the mine and looks straight at it, fenced on the outside with
 * barriers as tall as the mine itself so nobody walks off into the void, and an invisible floor under the
 * pit - flush with the walkway - so an emptied mine isn't a hole either. It is all worked out from the
 * mine's own region: there is no schematic to draw or keep in step with the mine's size.
 * <p>
 * From above, for a walkway {@code width} of 3:
 * <pre>
 *   B B B B B B B B B    B - barrier fence
 *   B . . . . . . . B    . - walkway
 *   B . M M M M M . B    M - the mine itself, untouched
 *   B . M M M M M . B
 *   B . . . . . . . B
 *   B B B B B B B B B
 * </pre>
 * and in cross section:
 * <pre>
 *   B             B      the fence is as tall as the mine, so the mine is what you see over it
 *   B   M M M M   B
 *   B   M M M M   B
 *   B   M M M M   B
 *   . . # # # # . .      the walkway is one block under the mine, level with the barrier floor
 *                        # - barrier floor, so a mined out pit is still a floor
 * </pre>
 */
public final class PrivateMinePlatform {

    /**
     * How wide the walkway is on every side of the mine, in blocks.
     */
    public static final int DEFAULT_WIDTH = 3;

    /**
     * Widest walkway a template may ask for, so a mistyped width can't paint over the whole grid.
     */
    public static final int MAX_WIDTH = 16;

    /**
     * Shortest the fence is ever built, whatever the mine's height. Taller than a player can jump, so a
     * one or two block deep mine doesn't come with a fence anybody could hop over.
     */
    public static final int MIN_FENCE_HEIGHT = 3;

    /**
     * How far the platform reaches under the mine: the walkway and the barrier floor, both one block
     * under the mine's lowest layer.
     */
    public static final int REACH_BELOW = 1;

    /**
     * What the walkway is made of when the config doesn't say otherwise.
     */
    public static final Material DEFAULT_MATERIAL = Material.SMOOTH_STONE;

    /**
     * Where the walkway block is configured, in private-mines/config.yml.
     */
    public static final String MATERIAL_PATH = "Private-Mines.Platform-Material";

    private PrivateMinePlatform() {
    }

    /**
     * The block the walkway is paved with, from the config. Anything unreadable falls back to
     * {@link #DEFAULT_MATERIAL} rather than leaving a mine with no floor around it.
     */
    public static Material walkwayMaterial() {
        if (RMPrivateMinesConfig.file() == null) {
            return DEFAULT_MATERIAL;
        }

        final Material material = Material.matchMaterial(
                String.valueOf(RMPrivateMinesConfig.file().getString(MATERIAL_PATH, DEFAULT_MATERIAL.name())));
        return material == null || !material.isBlock() || material == Material.AIR ? DEFAULT_MATERIAL : material;
    }

    /**
     * How tall the fence around a mine of this height is.
     */
    public static int fenceHeight(final int mineHeight) {
        return Math.max(MIN_FENCE_HEIGHT, mineHeight);
    }

    /**
     * How far the platform reaches over the mine's top layer. The fence starts at the mine's lowest layer
     * and is as tall as the mine, so it normally ends flush with the top and only overshoots when the mine
     * is too shallow to fence at its own height.
     */
    public static int reachAbove(final int mineHeight) {
        return Math.max(0, fenceHeight(mineHeight) - mineHeight);
    }

    /**
     * How far a platform of this width reaches past the mine on every side: the walkway plus its fence.
     * What slot spacing has to leave room for.
     */
    public static int margin(final int width) {
        return width <= 0 ? 0 : width + 1;
    }

    /**
     * Builds the platform around a mine.
     *
     * @param region the mine's own region, as {minX, minY, minZ, maxX, maxY, maxZ}
     * @param width  walkway width in blocks; 0 or less builds nothing
     */
    public static void build(final World world, final int[] region, final int width) {
        paint(world, region, width, walkwayMaterial(), Material.BARRIER, Material.BARRIER);
    }

    /**
     * Takes the platform away again, so the slot is handed to the next owner as empty as it started.
     * Only ever called with the width the platform was built at, which is why each mine remembers it.
     */
    public static void remove(final World world, final int[] region, final int width) {
        paint(world, region, width, Material.AIR, Material.AIR, Material.AIR);
    }

    /**
     * Where the owner arrives: on the walkway at the corner off the mine's lowest corner, looking
     * diagonally across it. A corner is the one spot on the ring that is never a dead end, whichever way
     * the player runs.
     *
     * @return the spot, or null when this mine has no platform to stand on
     */
    public static Location entrance(final World world, final int[] region, final int width) {
        if (world == null || region == null || width <= 0) {
            return null;
        }

        //the middle of the walkway rather than its lip, so the player doesn't land against the fence
        final int inset = Math.max(1, (width + 1) / 2);
        return new Location(world, region[0] - inset, region[1], region[2] - inset, -45F, 0F);
    }

    private static void paint(final World world, final int[] region, final int width,
                              final Material floor, final Material fence, final Material pitFloor) {
        if (world == null || region == null || region.length < 6 || width <= 0) {
            return;
        }

        final int minX = region[0], minY = region[1], minZ = region[2];
        final int maxX = region[3], maxY = region[4], maxZ = region[5];

        //the walkway is one block under the mine, so whoever walks it stands level with the mine's lowest
        //layer. Painted as four strips instead of one square: the mine's own blocks are never written over
        final int walkway = minY - REACH_BELOW;
        fill(world, minX - width, walkway, minZ - width, maxX + width, walkway, minZ - 1, floor);
        fill(world, minX - width, walkway, maxZ + 1, maxX + width, walkway, maxZ + width, floor);
        fill(world, minX - width, walkway, minZ, minX - 1, walkway, maxZ, floor);
        fill(world, maxX + 1, walkway, minZ, maxX + width, walkway, maxZ, floor);

        //the fence, one block out from the walkway's lip and as tall as the mine, so there is nothing to
        //see over it but the mine. The two X sides run the full length to close the corners, and the Z
        //sides fill in between them
        final int out = width + 1;
        final int fenceBottom = walkway + 1;
        final int fenceTop = fenceBottom + fenceHeight(maxY - minY + 1) - 1;
        fill(world, minX - out, fenceBottom, minZ - out, minX - out, fenceTop, maxZ + out, fence);
        fill(world, maxX + out, fenceBottom, minZ - out, maxX + out, fenceTop, maxZ + out, fence);
        fill(world, minX - width, fenceBottom, minZ - out, maxX + width, fenceTop, minZ - out, fence);
        fill(world, minX - width, fenceBottom, maxZ + out, maxX + width, fenceTop, maxZ + out, fence);

        //and the pit's own floor, in line with the walkway, so digging the last block out doesn't open it
        fill(world, minX, walkway, minZ, maxX, walkway, maxZ, pitFloor);
    }

    /**
     * Sets every block of one cuboid, skipping whatever is already right and never triggering physics -
     * the platform is placed on nothing and would otherwise fall or update its way through the world.
     */
    private static void fill(final World world, final int x1, final int y1, final int z1,
                             final int x2, final int y2, final int z2, final Material material) {
        final int lowY = Math.max(Math.min(y1, y2), world.getMinHeight());
        final int highY = Math.min(Math.max(y1, y2), world.getMaxHeight() - 1);
        if (lowY > highY) {
            //the mine is against the world's ceiling or floor, so this part of the platform has nowhere
            //to go. validate() warns the admin about it when the template is loaded
            return;
        }

        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                for (int y = lowY; y <= highY; y++) {
                    final Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != material) {
                        block.setType(material, false);
                    }
                }
            }
        }
    }
}
