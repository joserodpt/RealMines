package josegamerpt.realmines.managers;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.classes.*;
import josegamerpt.realmines.classes.Mine.Data;
import josegamerpt.realmines.classes.Mine.Reset;
import josegamerpt.realmines.config.Mines;
import josegamerpt.realmines.events.MineBlockBreakEvent;
import josegamerpt.realmines.utils.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class MineManager {

    public static ArrayList<Mine> mines = new ArrayList<>();
    public static List<String> signset = Arrays.asList("PM", "PL", "BM", "BR");

    public static ArrayList<String> getRegisteredMines() {
        Mines.reload();
        ArrayList<String> mines = new ArrayList<>();
        ConfigurationSection cs = Mines.file().getConfigurationSection("");
        cs.getKeys(false).forEach(s -> mines.add(ChatColor.stripColor(s)));
        return mines;
    }

    public static void unregisterMine(Mine m) {
        Mines.file().set(m.getName(), null);
        Mines.save();
        MineManager.mines.remove(m);
    }

    public static void loadMines() {
        for (String s : getRegisteredMines()) {
            World w = Bukkit.getWorld(Mines.file().getString(s + ".World"));

            Location pos1 = new Location(w, Mines.file().getDouble(s + ".POS1.X"),
                    Mines.file().getDouble(s + ".POS1.Y"), Mines.file().getDouble(s + ".POS1.Z"));
            Location pos2 = new Location(w, Mines.file().getDouble(s + ".POS2.X"),
                    Mines.file().getDouble(s + ".POS2.Y"), Mines.file().getDouble(s + ".POS2.Z"));
            Location tp = null;

            if (Mines.file().get(s + ".Teleport") != null) {
                tp = new Location(w, Mines.file().getDouble(s + ".Teleport.X"),
                        Mines.file().getDouble(s + ".Teleport.Y"), Mines.file().getDouble(s + ".Teleport.Z"),
                        Float.parseFloat(Mines.file().getString(s + ".Teleport.Yaw")),
                        Float.parseFloat(Mines.file().getString(s + ".Teleport.Pitch")));
            }
            ArrayList<MineSign> signs = new ArrayList<>();
            if (Mines.file().get(s + ".Signs") != null) {
                for (String sig : Mines.file().getStringList(s + ".Signs")) {
                    String[] parse = sig.split(";");
                    World sigw = Bukkit.getWorld(parse[0]);
                    Location loc = new Location(w, Double.parseDouble(parse[1]), Double.parseDouble(parse[2]),
                            Double.parseDouble(parse[3]));
                    String mod = parse[4];
                    MineSign ms = new MineSign(sigw.getBlockAt(loc), mod);
                    System.out.print(loc);
                    signs.add(ms);
                }
            }
            Material ic = Material.valueOf(Mines.file().getString(s + ".Icon"));
            ArrayList<MineBlock> blocks = getBlocks(s);
            Mine m = new Mine(s, Mines.file().getString(s + ".Display-Name"), blocks, signs, pos1, pos2, ic, tp,
                    Mines.file().getBoolean(s + ".Settings.Reset.ByPercentage"),
                    Mines.file().getBoolean(s + ".Settings.Reset.ByTime"),
                    Mines.file().getInt(s + ".Settings.Reset.ByPercentageValue"),
                    Mines.file().getInt(s + ".Settings.Reset.ByTimeValue"));
            m.register();
        }
    }

    private static ArrayList<MineBlock> getBlocks(String s) {
        ArrayList<MineBlock> list = new ArrayList<>();
        for (String a : Mines.file().getStringList(s + ".Blocks")) {
            String[] content = a.split(";");
            Material mat = Material.valueOf(content[0]);
            Double per = Double.parseDouble(content[1]);
            list.add(new MineBlock(mat, per));
        }
        return list;
    }

    public static void createMine(Player p, String name) {
		WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		try {
			com.sk89q.worldedit.regions.Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

			if (r != null) {
				Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
				Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

				Mine m = new Mine(name, name, new ArrayList<>(), new ArrayList<>(), pos1, pos2,
						Material.DIAMOND_ORE, null, false, true, 20, 60);
				m.saveData(Mine.Data.INIT);

				m.register();
				m.addBlock(new MineBlock(Material.STONE, 100D));
				m.reset();
				m.setTeleport(p.getLocation());
				m.saveData(Data.TELEPORT);
                Text.send(p, "&fMine &acreated.");
			}
		} catch (Exception e) {
			Text.send(p, "&fYour boundaries &care not set.");
        }
    }

    public static void saveMine(Mine mine, Mine.Data t) {

        switch (t)
        {
            case NAME:
                Mines.file().set(mine.getName() + ".Display-Name", mine.getDisplayName());
                break;
            case ALL:
            case INIT:
                Mines.file().set(mine.getName() + ".Display-Name", mine.getDisplayName());
                Mines.file().set(mine.getName() + ".World", mine.getPOS1().getWorld().getName());
                Mines.file().set(mine.getName() + ".POS1.X", mine.getPOS1().getX());
                Mines.file().set(mine.getName() + ".POS1.Y", mine.getPOS1().getY());
                Mines.file().set(mine.getName() + ".POS1.Z", mine.getPOS1().getZ());
                Mines.file().set(mine.getName() + ".POS2.X", mine.getPOS2().getX());
                Mines.file().set(mine.getName() + ".POS2.Y", mine.getPOS2().getY());
                Mines.file().set(mine.getName() + ".POS2.Z", mine.getPOS2().getZ());
                Mines.file().set(mine.getName() + ".Icon", mine.getIcon().name());
                Mines.file().set(mine.getName() + ".Signs", mine.getSignList());
                Mines.file().set(mine.getName() + ".Blocks", mine.getBlockList());
                Mines.file().set(mine.getName() + ".Settings.Reset.ByPercentage", mine.isResetBy(Reset.PERCENTAGE));
                Mines.file().set(mine.getName() + ".Settings.Reset.ByTime", mine.isResetBy(Reset.TIME));
                Mines.file().set(mine.getName() + ".Settings.Reset.ByPercentageValue", mine.getResetValue(Reset.PERCENTAGE));
                Mines.file().set(mine.getName() + ".Settings.Reset.ByTimeValue", mine.getResetValue(Reset.TIME));
                break;
            case BLOCKS:
                Mines.file().set(mine.getName() + ".Blocks", mine.getBlockList());
                break;
            case ICON:
                Mines.file().set(mine.getName() + ".Icon", mine.getIcon().name());
                break;
            case OPTIONS:
                Mines.file().set(mine.getName() + ".Settings.Reset.ByPercentage", mine.isResetBy(Reset.PERCENTAGE));
                Mines.file().set(mine.getName() + ".Settings.Reset.ByTime", mine.isResetBy(Reset.TIME));
                Mines.file().set(mine.getName() + ".Settings.Reset.ByPercentageValue", mine.getResetValue(Reset.PERCENTAGE));
                Mines.file().set(mine.getName() + ".Settings.Reset.ByTimeValue", mine.getResetValue(Reset.TIME));
                break;
            case REGION:
                Mines.file().set(mine.getName() + ".World", mine.getPOS1().getWorld().getName());
                Mines.file().set(mine.getName() + ".POS1.X", mine.getPOS1().getX());
                Mines.file().set(mine.getName() + ".POS1.Y", mine.getPOS1().getY());
                Mines.file().set(mine.getName() + ".POS1.Z", mine.getPOS1().getZ());
                Mines.file().set(mine.getName() + ".POS2.X", mine.getPOS2().getX());
                Mines.file().set(mine.getName() + ".POS2.Y", mine.getPOS2().getY());
                Mines.file().set(mine.getName() + ".POS2.Z", mine.getPOS2().getZ());
                break;
            case TELEPORT:
                if (mine.getTeleport() != null) {
                    Mines.file().set(mine.getName() + ".Teleport.X", mine.getTeleport().getX());
                    Mines.file().set(mine.getName() + ".Teleport.Y", mine.getTeleport().getY());
                    Mines.file().set(mine.getName() + ".Teleport.Z", mine.getTeleport().getZ());
                    Mines.file().set(mine.getName() + ".Teleport.Yaw", mine.getTeleport().getYaw());
                    Mines.file().set(mine.getName() + ".Teleport.Pitch", mine.getTeleport().getPitch());
                }
                break;
            case SIGNS:
                Mines.file().set(mine.getName() + ".Signs", mine.getSignList());
                break;
        }

        Mines.save();
    }

    public static boolean exists(String name) {
        for (Mine m : mines) {
            if (ChatColor.stripColor(Text.color(m.getName())).equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<MineIcon> getMineList() {
        ArrayList<MineIcon> l = new ArrayList<>();
        for (Mine mine : mines) {
            l.add(new MineIcon(mine));
        }
        if (l.size() == 0) {
            l.add(new MineIcon());
        }
        return l;
    }

    public static void teleport(Player target, Mine m, Boolean silent) {
        if (!silent) {
            if (m.hasTP()) {
                target.teleport(m.getTeleport());
                target.sendMessage(RealMines.getPrefix() + Text.color("&fTeleported to mine &9" + m.getDisplayName()));
            } else {
                target.sendMessage(RealMines.getPrefix() + Text.color("&fThis mine &cdoesnt have a teleport location."));
            }
        } else {
            if (m.hasTP()) {
                target.teleport(m.getTeleport());
            }
        }
    }

    public static Mine getMine(String name) {
        for (Mine m : mines) {
            if (ChatColor.stripColor(Text.color(m.getName())).equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }

    public static void findBlockUpdate(Block b) {
        for (Mine m : mines) {
            if (m.getMine().contains(b)) {
                MineBlockBreakEvent exampleEvent = new MineBlockBreakEvent(m);
                Bukkit.getPluginManager().callEvent(exampleEvent);
            }
        }
    }

    public static void despertar(Mine m) {
        m.updateSigns();
        if (m.isResetBy(Reset.PERCENTAGE)) {
            if ((double) m.getRemainingBlocksPer() < m.getResetValue(Reset.PERCENTAGE)) {
                m.kickPlayers("&6Warning &fThis mine is going to be reset.");
                Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.pl, m::reset, 10);
            }
        }
    }

    public static ArrayList<MineSign> getSigns() {
        ArrayList<MineSign> l = new ArrayList<>();
        for (Mine m : mines) {
            l.addAll(m.getSigns());
        }
        return l;
    }

    public static void unloadMines() {
        mines.clear();
    }

    public static void setRegion(String name, Player p) {
        Mine m = getMine(name);

        WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            com.sk89q.worldedit.regions.Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            if (r != null) {
                Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                m.setPOS(pos1, pos2);
                m.saveData(Data.REGION);
                Text.send(p, "&fRegion &aupdated.");
                m.reset();
            }
        } catch (Exception e) {
            Text.send(p, "&fYour boundaries &care not set.");
        }
    }

    public static void stopTasks() {
        mines.forEach(mine -> mine.getTimer().kill());
    }

    public static void startTasks() {
        mines.forEach(mine -> mine.getTimer().start());
    }

    public static void deleteMine(Mine mine) {
        mine.clear();
        mine.getTimer().kill();
        mine.removeDependencies();
        unregisterMine(mine);
    }

    public static void clearMemory() {
        mines.clear();
    }

    public static ArrayList<Mine> getMines() {
        return mines;
    }
}
