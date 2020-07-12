package josegamerpt.realmines.utils;

import josegamerpt.realmines.classes.MinePlayer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CubeVisualizer {

    private MinePlayer mp;
    private double dist = 0.5;

    public CubeVisualizer(MinePlayer mp) {
        this.mp = mp;
    }

    public List<Location> getCube() {
        if (mp.pos1 != null && mp.pos2 != null) {
            List<Location> result = new ArrayList<>();
            World world = mp.pos1.getWorld();
            double minX = Math.min(mp.pos1.getX(), mp.pos2.getX());
            double minY = Math.min(mp.pos1.getY(), mp.pos2.getY());
            double minZ = Math.min(mp.pos1.getZ(), mp.pos2.getZ());
            double maxX = Math.max(mp.pos1.getX() + 1, mp.pos2.getX() + 1);
            double maxY = Math.max(mp.pos1.getY() + 1, mp.pos2.getY() + 1);
            double maxZ = Math.max(mp.pos1.getZ() + 1, mp.pos2.getZ() + 1);

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
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    public void spawnParticle(Location location) {
        location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(), 0, 0.001, 1, 0, 1, new Particle.DustOptions(Color.BLUE, 1));
    }
}
