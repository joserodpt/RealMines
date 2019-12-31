package josegamerpt.realmines.classes;

import org.bukkit.Location;
import org.bukkit.Material;

public class SelectionBlock {

	public Location loc;
	public Material old;

	public SelectionBlock(Location l, Material o) {
		this.loc = l;
		this.old = o;
		
		loc.getWorld().getBlockAt(loc).setType(Material.SEA_LANTERN);
	}

	public Material getMaterial() {
		return old;
	}

	public Location getLocation() {
		return loc;
	}
	
	public void revert()
	{
		loc.getBlock().setType(old);
	}

}
