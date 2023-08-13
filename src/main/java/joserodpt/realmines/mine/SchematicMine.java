package joserodpt.realmines.mine;

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
import com.sk89q.worldedit.session.ClipboardHolder;
import joserodpt.realmines.RealMines;
import joserodpt.realmines.manager.MineManager;
import joserodpt.realmines.mine.component.MineCuboid;
import joserodpt.realmines.mine.component.MineSign;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class SchematicMine extends RMine {

    private final String schematicFile;
    private Location pasteLocation;
    private final Clipboard pasteClipboard;
    private final MineManager mm;

    public SchematicMine(final String n, final String displayname, final ArrayList<MineSign> si, final Location pasteLocation, final String schematicFile, final Material i,
                         final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final String color, final Location pos1, final Location pos2, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent, final MineManager mm) {

        super(n, displayname, si, i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent, mm);

        this.mm = mm;

        this.schematicFile = schematicFile;
        this.pasteClipboard = this.loadSchematic(schematicFile);
        this.setPasteLocation(pasteLocation);

        super.setPOS(pos1, pos2);
        this.fill();
        this.updateSigns();
    }

    private void setPasteLocation(final Location pasteLocation) {
        this.pasteLocation = pasteLocation;
    }

    @Override
    public void fill() {
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            this.placeSchematic(this.pasteClipboard, this.pasteLocation);
        }
    }

    @Override
    public String getType() {
        return "SCHEMATIC";
    }

    public Location getSchematicPlace() {
        return this.pasteLocation;
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

    public EditSession placeSchematic(final Clipboard clipboard, final Location loc) {
        if (clipboard != null) {
            try (final EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(loc.getWorld()))) {
                final Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                        .ignoreAirBlocks(true)
                        .copyEntities(false)
                        .build();
                Operations.complete(operation);

                return editSession;
            } catch (final WorldEditException e) {
                RealMines.getPlugin().log(Level.SEVERE, "Failed to paste schematic named " + name);
                RealMines.getPlugin().log(Level.SEVERE, e.getMessage());
            }
        }
        return null;
    }

}
