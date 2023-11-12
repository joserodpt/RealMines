package joserodpt.realmines.mine.types;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

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
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import joserodpt.realmines.RealMines;
import joserodpt.realmines.gui.BlockPickerGUI;
import joserodpt.realmines.manager.MineManager;
import joserodpt.realmines.mine.RMine;
import joserodpt.realmines.mine.components.MineColor;
import joserodpt.realmines.mine.components.MineCuboid;
import joserodpt.realmines.mine.components.MineSign;
import joserodpt.realmines.mine.components.items.MineItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class SchematicMine extends RMine {

    private final String schematicFile;
    private Location pasteLocation;
    private final Clipboard pasteClipboard;
    private final MineManager mm;

    public SchematicMine(final World w, final String n, final String displayname, final List<MineSign> si, final Location pasteLocation, final String schematicFile, final Material i,
                         final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final MineColor color, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent, final MineManager mm) {

        super(w, n, displayname, si, i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent, mm);

        this.mm = mm;

        this.schematicFile = schematicFile;
        this.pasteClipboard = this.loadSchematic(schematicFile);
        this.pasteLocation = pasteLocation;

        this.fill();
        this.updateSigns();
    }

    @Override
    public void fill() {
        this.placeSchematic(this.pasteClipboard, this.pasteLocation);
    }

    @Override
    public Map<Material, MineItem> getMineItems() {
        return new HashMap<>();
    }

    @Override
    public RMine.Type getType() {
        return Type.SCHEMATIC;
    }

    public String getSchematicFilename() {
        return this.schematicFile;
    }

    //WORLD EDIT UTILS
    public Clipboard loadSchematic(final String name) {
        final File folder = new File(mm.getSchematicFolder(), "schematics");
        final File file = new File(folder, name);

        Clipboard clipboard = null;

        final ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (final ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()))) {
            clipboard = reader.read();
        } catch (final IOException e) {
            RealMines.getPlugin().log(Level.SEVERE, "Failed to load schematic named " + name);
            RealMines.getPlugin().log(Level.SEVERE, e.getMessage());
        }

        return clipboard;
    }

    public void placeSchematic(final Clipboard clipboard, final Location loc) {
        if (clipboard != null) {
            BlockVector3 pasteVec3 = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            try (final EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(loc.getWorld()))) {
                final Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(pasteVec3)
                        .ignoreAirBlocks(true)
                        .copyEntities(false)
                        .build();
                Operations.complete(operation);

                CuboidRegion r = clipboard.getRegion().getBoundingBox();
                BlockVector3 currentCenter = r.getCenter().toBlockPoint().subtract(r.getWidth() / 2, r.getHeight() / 2,0);
                BlockVector3 shiftVector = pasteVec3.subtract(currentCenter);
                r.shift(shiftVector);

                Location p1 = new Location(this.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());
                Location p2 = new Location(this.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());

                this.setPOS(p1, p2);
            } catch (final WorldEditException e) {
                RealMines.getPlugin().log(Level.SEVERE, "Failed to paste schematic named " + name);
                RealMines.getPlugin().log(Level.SEVERE, e.getMessage());
            }
        }
    }

    @Override
    public BlockPickerGUI.PickType getBlockPickType() {
        return null;
    }

    @Override
    public void clearContents() {
        this.getMineCuboid().clear();
    }

    public Location getSchematicPlace() {
        return this.pasteLocation;
    }
}
