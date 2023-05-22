package josegamerpt.realmines.mine;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import josegamerpt.realmines.mine.component.MineCuboid;
import josegamerpt.realmines.mine.component.MineSign;
import josegamerpt.realmines.util.WorldEditUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;

public class SchematicMine extends RMine {

    private final String schematicFile;
    private Location pasteLocation;
    private final Clipboard pasteClipboard;

    public SchematicMine(final String n, final String displayname, final ArrayList<MineSign> si, final Location pasteLocation, final String schematicFile, final Material i,
                         final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final String color, final Location pos1, final Location pos2, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent) {

        super(n, displayname, si, i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent);

        this.schematicFile = schematicFile;
        this.pasteClipboard = WorldEditUtils.loadSchematic(schematicFile);
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
        if (Bukkit.getOnlinePlayers().size() > 0) {
            WorldEditUtils.placeSchematic(this.pasteClipboard, this.pasteLocation);
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

}
