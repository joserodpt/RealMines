package josegamerpt.realmines.mines;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import josegamerpt.realmines.mines.components.MineCuboid;
import josegamerpt.realmines.mines.components.MineSign;
import josegamerpt.realmines.utils.WorldEditUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;

public class SchematicMine extends RMine {

    private String schematicFile;
    private Location pasteLocation;
    private Clipboard pasteClipboard;

    public SchematicMine(String n, String displayname, ArrayList<MineSign> si, Location pasteLocation, String schematicFile, Material i,
                         Location t, Boolean resetByPercentag, Boolean resetByTim, int rbpv, int rbtv, String color, Location pos1, Location pos2, HashMap<MineCuboid.CuboidDirection, Material> faces, boolean silent) {

        super(n, displayname, si, i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent);

        this.schematicFile = schematicFile;
        this.pasteClipboard = WorldEditUtils.loadSchematic(schematicFile);
        this.setPasteLocation(pasteLocation);

        super.setPOS(pos1, pos2);
        this.fill();
        this.updateSigns();
    }

    private void setPasteLocation(Location pasteLocation) {
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
