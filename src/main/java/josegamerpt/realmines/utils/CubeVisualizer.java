package josegamerpt.realmines.utils;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import josegamerpt.realmines.classes.Mine;
import josegamerpt.realmines.classes.MineBlock;
import josegamerpt.realmines.classes.MinePlayer;
import org.bukkit.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CubeVisualizer {

    private MinePlayer mp;
    private double dist = 0.5;

    public CubeVisualizer(MinePlayer mp) {
        this.mp = mp;
    }

    public List<Location> getParticleLocations() {

        WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            com.sk89q.worldedit.regions.Region r = w.getSession(mp.getPlayer()).getSelection(w.getSession(mp.getPlayer()).getSelectionWorld());

            if (r != null) {
                Location pos1 = new Location(mp.getPlayer().getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                Location pos2 = new Location(mp.getPlayer().getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                List<Location> result = new ArrayList<>();
                World world = pos1.getWorld();
                double minX = Math.min(pos1.getX(), pos2.getX());
                double minY = Math.min(pos1.getY(), pos2.getY());
                double minZ = Math.min(pos1.getZ(), pos2.getZ());
                double maxX = Math.max(pos1.getX() + 1, pos2.getX() + 1);
                double maxY = Math.max(pos1.getY() + 1, pos2.getY() + 1);
                double maxZ = Math.max(pos1.getZ() + 1, pos2.getZ() + 1);

                for (double x = minX; x <= maxX; x += dist) {
                    for (double y = minY; y <= maxY; y += dist) {
                        for (double z = minZ; z <= maxZ; z += dist) {
                            int components = 0;
                            if (x == minX || x == maxX) components++;
                            if (y == minY || y == maxY) components++;
                            if (z == minZ || z == maxZ) components++;
                            if (components >= 2) {
                                result.add(new Location(world, x, y, z));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    public void spawnParticle(Location location) {
        location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(), 0, 0.001, 1, 0, 1, new Particle.DustOptions(Color.BLUE, 1));
    }
}
