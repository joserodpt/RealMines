package josegamerpt.realmines.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import josegamerpt.realmines.RealMines;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WorldEditUtils {

    public static Clipboard loadSchematic(String name)  {
        File folder = new File(RealMines.getInstance().getDataFolder(), "schematics");
        File file = new File(folder, name);

        Clipboard clipboard = null;

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return clipboard;
    }

    public static EditSession placeSchematic(Clipboard clipboard, Location loc) {
        if (clipboard != null) {
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(loc.getWorld()), 999999999)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                        .ignoreAirBlocks(true)
                        .copyEntities(false)
                        .build();
                Operations.complete(operation);
                
                return editSession;
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
