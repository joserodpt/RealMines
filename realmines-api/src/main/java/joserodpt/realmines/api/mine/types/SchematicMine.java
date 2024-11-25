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
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.managers.MineManagerAPI;
import joserodpt.realmines.api.mine.RMFailedToLoadException;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.items.MineSchematicItem;
import joserodpt.realmines.api.utils.WorldEditUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SchematicMine extends RMine {

    private String schematicFile;
    private Location pasteLocation;
    private Clipboard pasteClipboard;
    private MineManagerAPI mm;

    /*
    public SchematicMine(final World w, final String n, final String displayname, final List<MineSign> si, final Location pasteLocation, final String schematicFile, final Material i,
                         final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final MineColor color, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent, final boolean breakingPermissionOn, final MineManagerAPI mm) {

        super(w, n, displayname, si, new HashMap<>(), i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent, breakingPermissionOn, mm);

        this.mm = mm;

        this.schematicFile = schematicFile;
        this.pasteClipboard = this.loadSchematic(schematicFile);
        this.pasteLocation = pasteLocation;

        this.fillContent();
        this.processPastedBlocks();
        this.updateSigns();
    }

    public SchematicMine(final World w, final String n, final String displayname, final Map<Material, MineItem> b, final List<MineSign> si, final Location pasteLocation, final String schematicFile, final Material i,
                         final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final MineColor color, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent, final boolean breakingPermissionOn, final MineManagerAPI mm) {

        super(w, n, displayname, si, b, i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent, breakingPermissionOn, mm);

        this.mm = mm;

        this.schematicFile = schematicFile;
        this.pasteClipboard = this.loadSchematic(schematicFile);
        this.pasteLocation = pasteLocation;

        this.fillContent();
        if (RMMinesOldConfig.file().get(n + ".Blocks") == null) {
            processPastedBlocks();
        }
        this.updateSigns();
    }

     */

    //converting from old config to new config
    public SchematicMine(String name, Section mineConfigSection) throws RMFailedToLoadException {
        super(name, mineConfigSection);
    }

    //after converting from old config to new config
    public SchematicMine(String name, YamlConfiguration config) throws RMFailedToLoadException {
        super(name, config);
    }

    private void processPastedBlocks() {
        for (Block block : this.getMineCuboid()) {
            Material type = block.getType();
            if (type == Material.AIR) {
                continue;
            }

            if (!super.getMineItems().containsKey(type)) {
                super.getMineItems().put(type, new MineSchematicItem(type));
            }
        }

        this.saveData(MineData.BLOCKS);
    }

    @Override
    public void fillContent() {
        this.placeSchematic(this.pasteClipboard, this.pasteLocation);
        super.fillFaces();
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
            try {
                final EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(loc.getWorld()));

                ClipboardHolder holder = new ClipboardHolder(clipboard);
                Region region = clipboard.getRegion();

                BlockVector3 to = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                Operation operation = holder
                        .createPaste(editSession)
                        .to(to)
                        .ignoreAirBlocks(RMConfig.file().getBoolean("RealMines.ignoreAirBlocksSchematicPasting", true))
                        .copyBiomes(false)
                        .copyEntities(false)
                        .build();

                Operations.completeLegacy(operation);
                editSession.flushSession();

                BlockVector3 clipboardOffset = clipboard.getRegion().getMinimumPoint().subtract(clipboard.getOrigin());
                Vector3 min = to.toVector3().add(holder.getTransform().apply(clipboardOffset.toVector3()));
                Vector3 max = min.add(holder.getTransform().apply(region.getMaximumPoint().subtract(region.getMinimumPoint()).toVector3()));

                this.setPOS(WorldEditUtils.toLocation(min, getWorld()), WorldEditUtils.toLocation(max, getWorld()));
            } catch (final Exception e) {
                RealMinesAPI.getInstance().getPlugin().getLogger().severe("Failed to paste schematic named: " + name + " is the schematic too big? Is WorldEdit/FAWE properly enabled and supported?");
                RealMinesAPI.getInstance().getPlugin().getLogger().severe(e.getMessage());
            }
        }
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
