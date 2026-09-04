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
 * @author José Rodrigues © 2019-2026
 * @link https://github.com/joserodpt/RealMines
 */

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.RMBlockSet;
import joserodpt.realmines.api.mine.components.RMFailedToLoadException;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.mine.components.items.MineSchematicItem;
import joserodpt.realmines.api.utils.WorldEditUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class SchematicMine extends RMine {

    private String schematicFile;
    private Clipboard pasteClipboard;

    //converting from old config to new config
    public SchematicMine(String name, Section mineConfigSection) throws RMFailedToLoadException {
        super(name, mineConfigSection);

        this.schematicFile = mineConfigSection.getString("Schematic-Filename");
        this.pasteClipboard = this.loadSchematic(schematicFile);

        this.fillContent();
        this.processPastedBlocks();
        this.updateSigns();
    }

    //after converting from old config to new config
    public SchematicMine(String name, YamlConfiguration config) throws RMFailedToLoadException {
        super(name, config);

        this.schematicFile = config.getString("schematic");
        this.pasteClipboard = this.loadSchematic(schematicFile);

        if (!RMConfig.file().getBoolean("RealMines.disableMineResetOnServerStart", false)) {
            this.fillContent();
            this.processPastedBlocks();
        }
        this.updateSigns();
    }

    //new schematic mine
    public SchematicMine(String name, Location l, String schematicFile) throws RMFailedToLoadException {
        super(name, l.getWorld());

        this.schematicFile = schematicFile;
        this.getMineConfig().set("schematic", schematicFile);
        this.setPOS(l, null);
        this.saveConfig();
        this.pasteClipboard = this.loadSchematic(schematicFile);

        this.fillContent();
        this.processPastedBlocks();
        this.updateSigns();
    }

    private void processPastedBlocks() {
        //add default blockset if it doesn't exist
        RMBlockSet defaultBlockSet = this.getBlockSet("default");
        if (defaultBlockSet == null) {
            defaultBlockSet = addBlockSet("default");
        }

        if (defaultBlockSet.getItems().isEmpty()) {
            for (Block block : this.getMineCuboid()) {
                Material type = block.getType();
                if (type == Material.AIR) {
                    continue;
                }

                defaultBlockSet.add(new MineSchematicItem(type));
            }

            this.saveData(MineData.BLOCKS);
        }
    }

    @Override
    public List<MineItem> getBlockIcons(String blockSet) {
        processPastedBlocks(); // in case the schematic was pasted after the mine was created, we need to update the blockset
        return super.getBlockIcons(blockSet);
    }

    @Override
    public void fillContent() {
        this.placeSchematic(this.pasteClipboard, this.getPOS1());
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
        return WorldEditUtils.loadSchematic(name);
    }

    public void placeSchematic(final Clipboard clipboard, final Location loc) {
        final Location[] pasted = WorldEditUtils.pasteSchematic(clipboard, loc);
        if (pasted != null) {
            this.setPOS(pasted[0], pasted[1]);
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
}
