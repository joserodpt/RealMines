package joserodpt.realmines.api.mine.types;

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
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMMinesConfig;
import joserodpt.realmines.api.managers.MineManagerAPI;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.items.MineSchematicItem;
import joserodpt.realmines.api.mine.components.MineColor;
import joserodpt.realmines.api.mine.components.MineCuboid;
import joserodpt.realmines.api.mine.components.MineSign;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.utils.PickType;
import joserodpt.realmines.api.utils.WorldEditUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchematicMine extends RMine {

    private final String schematicFile;
    private Location pasteLocation;
    private final Clipboard pasteClipboard;
    private final MineManagerAPI mm;

    public SchematicMine(final World w, final String n, final String displayname, final List<MineSign> si, final Location pasteLocation, final String schematicFile, final Material i,
                         final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final MineColor color, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent, final boolean breakingPermissionOn, final MineManagerAPI mm) {

        super(w, n, displayname, si, new HashMap<>(), i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent, breakingPermissionOn, mm);

        this.mm = mm;

        this.schematicFile = schematicFile;
        this.pasteClipboard = this.loadSchematic(schematicFile);
        this.pasteLocation = pasteLocation;

        this.fill();
        this.processPastedBlocks();
        this.updateSigns();
    }

    public SchematicMine(final World w, final String n, final String displayname, final Map<Material, MineItem> b,final List<MineSign> si, final Location pasteLocation, final String schematicFile, final Material i,
                         final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final MineColor color, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent, final boolean breakingPermissionOn, final MineManagerAPI mm) {

        super(w, n, displayname, si, b, i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent, breakingPermissionOn, mm);

        this.mm = mm;

        this.schematicFile = schematicFile;
        this.pasteClipboard = this.loadSchematic(schematicFile);
        this.pasteLocation = pasteLocation;

        this.fill();
        if (RMMinesConfig.file().get(n + ".Blocks") == null) {
            processPastedBlocks();
        }
        this.updateSigns();
    }

    private void processPastedBlocks() {
        for (Block block : this.getMineCuboid()) {
            Material type = block.getType();
            if (type == Material.AIR) { continue; }

            if (!super.getMineItems().containsKey(type)) {
                super.getMineItems().put(type, new MineSchematicItem(type));
            }
        }

        Bukkit.getLogger().warning(super.getMineItems().toString());

        this.saveData(Data.BLOCKS);
    }

    @Override
    public void fill() {
        this.placeSchematic(this.pasteClipboard, this.pasteLocation);
        //faces
        for (final Map.Entry<MineCuboid.CuboidDirection, Material> pair : this.faces.entrySet()) {
            this.getMineCuboid().getFace(pair.getKey()).forEach(block -> block.setType(pair.getValue()));
        }
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
            RealMinesAPI.getInstance().getPlugin().getLogger().severe("Failed to load schematic named " + name);
            RealMinesAPI.getInstance().getPlugin().getLogger().severe(e.getMessage());
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
                RealMinesAPI.getInstance().getPlugin().getLogger().severe("Failed to paste schematic named " + name);
                RealMinesAPI.getInstance().getPlugin().getLogger().severe(e.getMessage());
            }
        }
    }

    @Override
    public PickType getBlockPickType() {
        return null;
    }

    @Override
    public void clearContents() {
        if (RMConfig.file().getBoolean("RealMines.useWorldEditForBlockPlacement")) {
            BlockVector3 point1 = BlockVector3.at(this.getMineCuboid().getPOS1().getX(), this.getMineCuboid().getPOS1().getY(), this.getMineCuboid().getPOS1().getZ());
            BlockVector3 point2 = BlockVector3.at(this.getMineCuboid().getPOS2().getX(), this.getMineCuboid().getPOS2().getY(), this.getMineCuboid().getPOS2().getZ());

            WorldEditUtils.setBlocks(new CuboidRegion(BukkitAdapter.adapt(this.getWorld()), point1, point2),
                    BukkitAdapter.adapt(Material.AIR.createBlockData()));
        } else {
            this.getMineCuboid().clear();
        }
    }

    public Location getSchematicPlace() {
        return this.pasteLocation;
    }
}
