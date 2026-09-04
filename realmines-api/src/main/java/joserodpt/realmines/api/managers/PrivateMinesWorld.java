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

import joserodpt.realmines.api.RealMinesAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * The single world every private mine is built in.
 * <p>
 * RealMines owns this world: it is created the first time it is needed and loaded again on every boot,
 * so a server never has to set one up by hand and a claim can't fail because somebody forgot to. It is
 * generated empty on purpose - the only blocks in it are the mines and their shells.
 */
public final class PrivateMinesWorld {

    /**
     * Fixed by design. Private mines are placed on a grid that RealMines works out itself, so letting the
     * world be configured per template only ever created ways for a claim to land somewhere unexpected.
     */
    public static final String NAME = "realminespm";

    private static final ChunkGenerator GENERATOR = new VoidGenerator();

    /**
     * Set once a creation attempt has failed, so a GUI that validates templates on every open doesn't
     * repeat the same error forever. Creation itself is still retried.
     */
    private static boolean reportedFailure;

    private PrivateMinesWorld() {
    }

    /**
     * The private mines world, creating or loading it if it isn't up yet.
     * <p>
     * Must be called from the main thread; returns null only when the server refused to make the world,
     * which is logged.
     */
    public static World get() {
        final World loaded = Bukkit.getWorld(NAME);
        return loaded != null ? loaded : create();
    }

    /**
     * The private mines world if it is already loaded, without trying to create it. For callers that only
     * want to look, such as status output.
     */
    public static World peek() {
        return Bukkit.getWorld(NAME);
    }

    /**
     * The generator RealMines uses for its world: no terrain at all.
     */
    public static ChunkGenerator getGenerator() {
        return GENERATOR;
    }

    private static synchronized World create() {
        //another caller may have created it while this one waited for the lock
        final World existing = Bukkit.getWorld(NAME);
        if (existing != null) {
            return existing;
        }

        if (!Bukkit.isPrimaryThread()) {
            logger().severe("The private mines world can only be created on the main thread.");
            return null;
        }

        //a world folder that is already there is being loaded, not set up, and must keep whatever
        //gamerules and spawn the server changed since
        final boolean firstTime = !new File(Bukkit.getWorldContainer(), NAME).isDirectory();

        World world = null;
        try {
            world = new WorldCreator(NAME)
                    .environment(World.Environment.NORMAL)
                    .generateStructures(false)
                    .generator(GENERATOR)
                    .createWorld();
        } catch (final Throwable t) {
            if (!reportedFailure) {
                logger().severe("Couldn't create the private mines world '" + NAME + "': " + t.getMessage());
                reportedFailure = true;
            }
            return null;
        }

        if (world == null) {
            if (!reportedFailure) {
                logger().severe("The server refused to create the private mines world '" + NAME + "'. "
                        + "Private mines can't be claimed until it exists.");
                reportedFailure = true;
            }
            return null;
        }

        if (firstTime) {
            applyDefaults(world);
            logger().info("Created the private mines world '" + NAME + "'.");
        } else {
            logger().info("Loaded the private mines world '" + NAME + "'.");
        }

        reportedFailure = false;
        return world;
    }

    /**
     * Sane starting point for a world that only holds mines: nothing spawns in it, nothing burns and
     * nothing gets blown up. Only applied when the world is first created, so an admin's later changes
     * survive a restart.
     */
    private static void applyDefaults(final World world) {
        try {
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
            world.setGameRule(GameRule.DO_FIRE_TICK, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            //deliberately not randomTickSpeed: farm mines need crops to grow
        } catch (final Throwable ignored) {
            //an old or unusual server missing one of these gamerules isn't worth failing the world over
        }
        world.setStorm(false);
        world.setThundering(false);
        world.setAutoSave(true);
    }

    private static Logger logger() {
        return RealMinesAPI.getInstance() == null ? Bukkit.getLogger() : RealMinesAPI.getInstance().getLogger();
    }

    /**
     * Generates nothing. The modern hooks are all turned off, and {@code generateChunkData} keeps servers
     * from before they existed empty too.
     */
    private static final class VoidGenerator extends ChunkGenerator {

        @Override
        @SuppressWarnings("deprecation")
        public ChunkData generateChunkData(final World world, final Random random, final int x, final int z,
                                           final BiomeGrid biome) {
            //left completely empty: the mines and their shells are the only blocks here
            return createChunkData(world);
        }

        @Override
        public boolean shouldGenerateNoise() {
            return false;
        }

        @Override
        public boolean shouldGenerateSurface() {
            return false;
        }

        @Override
        public boolean shouldGenerateBedrock() {
            return false;
        }

        @Override
        public boolean shouldGenerateCaves() {
            return false;
        }

        @Override
        public boolean shouldGenerateDecorations() {
            return false;
        }

        @Override
        public boolean shouldGenerateMobs() {
            return false;
        }

        @Override
        public boolean shouldGenerateStructures() {
            return false;
        }

        @Override
        public List<BlockPopulator> getDefaultPopulators(final World world) {
            return Collections.emptyList();
        }

        /**
         * Given explicitly so the server doesn't search an empty world for somewhere safe to stand.
         */
        @Override
        public Location getFixedSpawnLocation(final World world, final Random random) {
            return new Location(world, 0.5D, 64D, 0.5D);
        }
    }
}
