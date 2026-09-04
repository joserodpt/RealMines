package joserodpt.realmines.api.utils;

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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class WorldEditUtils {

    public static void setBlocks(Region region, Pattern pattern) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                .world(region.getWorld())
                .build()) {

            editSession.setReorderMode(EditSession.ReorderMode.FAST);
            editSession.setBlocks(region, pattern);
        } catch (MaxChangedBlocksException exception) {
            Bukkit.getLogger().warning("Error while setting blocks for RealMines: " + exception.getMessage());
        }
    }

    // blockvector3 to location function
    public static Location toLocation(BlockVector3 vector, World world) {
        return new Location(world, vector.getX(), vector.getY(), vector.getZ());
    }

    public static Location toLocation(com.sk89q.worldedit.math.Vector3 vector, World world) {
        return new Location(world, vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Reads a schematic from the plugin's schematics folder, or null if it can't be read.
     */
    public static Clipboard loadSchematic(final String name) {
        final File folder = new File(RealMinesAPI.getInstance().getMineManager().getSchematicFolder(), "schematics");
        final File file = new File(folder, name);

        final ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            RealMinesAPI.getInstance().getPlugin().getLogger().severe("Failed to load schematic named " + name + ": unknown format.");
            return null;
        }

        try (final ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()))) {
            return reader.read();
        } catch (final IOException e) {
            RealMinesAPI.getInstance().getPlugin().getLogger().severe("Failed to load schematic named " + name);
            RealMinesAPI.getInstance().getPlugin().getLogger().severe(e.getMessage());
            return null;
        }
    }

    /**
     * Pastes a clipboard at a location and returns the two corners of what was pasted, or null if the
     * paste failed. Callers that need to know where the paste landed - schematic mines, and the shell
     * decoration around a private mine slot - use the returned corners.
     */
    public static Location[] pasteSchematic(final Clipboard clipboard, final Location loc) {
        if (clipboard == null || loc == null || loc.getWorld() == null) {
            return null;
        }

        try {
            final EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(loc.getWorld()));

            final ClipboardHolder holder = new ClipboardHolder(clipboard);
            final Region region = clipboard.getRegion();

            final BlockVector3 to = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            final Operation operation = holder
                    .createPaste(editSession)
                    .to(to)
                    .ignoreAirBlocks(RMConfig.file().getBoolean("RealMines.ignoreAirBlocksSchematicPasting", true))
                    .copyBiomes(false)
                    .copyEntities(false)
                    .build();

            Operations.completeLegacy(operation);
            editSession.flushSession();

            final BlockVector3 clipboardOffset = clipboard.getRegion().getMinimumPoint().subtract(clipboard.getOrigin());
            final Vector3 min = to.toVector3().add(holder.getTransform().apply(clipboardOffset.toVector3()));
            final Vector3 max = min.add(holder.getTransform().apply(region.getMaximumPoint().subtract(region.getMinimumPoint()).toVector3()));

            return new Location[]{toLocation(min, loc.getWorld()), toLocation(max, loc.getWorld())};
        } catch (final Exception e) {
            RealMinesAPI.getInstance().getPlugin().getLogger().severe("Failed to paste a schematic. Is it too big? Is WorldEdit/FAWE properly enabled and supported?");
            RealMinesAPI.getInstance().getPlugin().getLogger().severe(String.valueOf(e.getMessage()));
            return null;
        }
    }

    /**
     * Loads and pastes a schematic by file name. Used for the optional shell decoration pasted around a
     * private mine's grid slot.
     */
    public static boolean pasteSchematic(final String schematicName, final Location loc) {
        return pasteSchematic(loadSchematic(schematicName), loc) != null;
    }
}
