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
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

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
}
