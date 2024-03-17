package joserodpt.realmines.api.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;


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

}
