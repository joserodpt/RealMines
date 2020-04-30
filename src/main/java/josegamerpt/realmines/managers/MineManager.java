package josegamerpt.realmines.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import josegamerpt.realmines.classes.Enum.Data;
import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.classes.Mine;
import josegamerpt.realmines.classes.MineBlock;
import josegamerpt.realmines.classes.MineIcon;
import josegamerpt.realmines.classes.MinePlayer;
import josegamerpt.realmines.classes.MineSign;
import josegamerpt.realmines.config.Mines;
import josegamerpt.realmines.events.MineBlockBreakEvent;
import josegamerpt.realmines.utils.Text;

public class MineManager {

	public static ArrayList<Mine> mines = new ArrayList<Mine>();
	public static List<String> signset = Arrays.asList("PM", "PL", "BM", "BR");

	public static ArrayList<String> getRegisteredMines() {
		Mines.reload();
		ArrayList<String> mines = new ArrayList<String>();
		ConfigurationSection cs = Mines.file().getConfigurationSection("");
		Set<String> keys = cs.getKeys(false);
		for (Iterator<String> iterator1 = keys.iterator(); iterator1.hasNext();) {
			mines.add((String) iterator1.next());
		}
		return mines;
	}

	public static void unregisterMine(Mine m) {
		Mines.file().set(m.name, null);
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
			ArrayList<MineSign> signs = new ArrayList<MineSign>();
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
			Mine m = new Mine(s, blocks, signs, pos1, pos2, ic, tp,
					Mines.file().getBoolean(s + ".Settings.Reset.ByPercentage"),
					Mines.file().getBoolean(s + ".Settings.Reset.ByTime"),
					Mines.file().getDouble(s + ".Settings.Reset.ByPercentageValue"),
					Mines.file().getInt(s + ".Settings.Reset.ByTimeValue"));
			m.register();
		}
	}

	private static ArrayList<MineBlock> getBlocks(String s) {
		ArrayList<MineBlock> list = new ArrayList<MineBlock>();
		for (String a : Mines.file().getStringList(s + ".Blocks")) {
			String[] content = a.split(";");
			Material mat = Material.valueOf(content[0]);
			Double per = Double.parseDouble(content[1]);
			list.add(new MineBlock(mat, per));
		}
		return list;
	}

	public static void createMine(MinePlayer p, String name) {
		if (p.pos1 != null && p.pos2 != null) {
			if (p.pos1.getLocation().getWorld().equals(p.pos2.getLocation().getWorld())) {
				Location p1 = p.pos1.getLocation();
				Location p2 = p.pos2.getLocation();
				Mine m = new Mine(name, new ArrayList<MineBlock>(), new ArrayList<MineSign>(), p1, p2,
						Material.DIAMOND_ORE, null, false, true, 20D, 60);
				m.saveData(Data.INIT);

				m.register();
				m.addBlock(new MineBlock(Material.STONE, 100D));
				m.reset();
				p.sendMessage("&fMine &acreated.");
			} else {
				p.sendMessage("&cYou cant create multi-world mines.");
			}
		} else {
			p.sendMessage("&cYou didnt set the boundaries.");
		}
	}

	public static void saveMine(Mine mine, Data t) {
		if (t.equals(Data.ALL) || t.equals(Data.INIT)) {
			Mines.file().set(mine.name + ".World", mine.pos1.getWorld().getName());
			Mines.file().set(mine.name + ".POS1.X", mine.pos1.getX());
			Mines.file().set(mine.name + ".POS1.Y", mine.pos1.getY());
			Mines.file().set(mine.name + ".POS1.Z", mine.pos1.getZ());
			Mines.file().set(mine.name + ".POS2.X", mine.pos2.getX());
			Mines.file().set(mine.name + ".POS2.Y", mine.pos2.getY());
			Mines.file().set(mine.name + ".POS2.Z", mine.pos2.getZ());
			Mines.file().set(mine.name + ".Icon", mine.icon.name());
			Mines.file().set(mine.name + ".Signs", mine.getSignList());
			Mines.file().set(mine.name + ".Blocks", mine.getBlockList());
			Mines.file().set(mine.name + ".Settings.Reset.ByPercentage", mine.resetByPercentage);
			Mines.file().set(mine.name + ".Settings.Reset.ByTime", mine.resetByTime);
			Mines.file().set(mine.name + ".Settings.Reset.ByPercentageValue", mine.resetByPercentageValue);
			Mines.file().set(mine.name + ".Settings.Reset.ByTimeValue", mine.resetByTimeValue);
		}
		if (t.equals(Data.ICON)) {
			Mines.file().set(mine.name + ".Icon", mine.icon.name());
		}
		if (t.equals(Data.OPTIONS)) {
			Mines.file().set(mine.name + ".Settings.Reset.ByPercentage", mine.resetByPercentage);
			Mines.file().set(mine.name + ".Settings.Reset.ByTime", mine.resetByTime);
			Mines.file().set(mine.name + ".Settings.Reset.ByPercentageValue", mine.resetByPercentageValue);
			Mines.file().set(mine.name + ".Settings.Reset.ByTimeValue", mine.resetByTimeValue);
		}
		if (t.equals(Data.REGION)) {
			Mines.file().set(mine.name + ".World", mine.pos1.getWorld().getName());
			Mines.file().set(mine.name + ".POS1.X", mine.pos1.getX());
			Mines.file().set(mine.name + ".POS1.Y", mine.pos1.getY());
			Mines.file().set(mine.name + ".POS1.Z", mine.pos1.getZ());
			Mines.file().set(mine.name + ".POS2.X", mine.pos2.getX());
			Mines.file().set(mine.name + ".POS2.Y", mine.pos2.getY());
			Mines.file().set(mine.name + ".POS2.Z", mine.pos2.getZ());
		}
		if (t.equals(Data.TELEPORT)) {
			Mines.file().set(mine.name + ".Teleport.X", mine.teleport.getX());
			Mines.file().set(mine.name + ".Teleport.Y", mine.teleport.getY());
			Mines.file().set(mine.name + ".Teleport.Z", mine.teleport.getZ());
			Mines.file().set(mine.name + ".Teleport.Yaw", mine.teleport.getYaw());
			Mines.file().set(mine.name + ".Teleport.Pitch", mine.teleport.getPitch());
		}
		if (t.equals(Data.SIGNS)) {
			Mines.file().set(mine.name + ".Signs", mine.getSignList());
		}
		if (t.equals(Data.BLOCKS)) {
			Mines.file().set(mine.name + ".Blocks", mine.getBlockList());
		}
		Mines.save();
	}

	public static boolean exists(String name) {
		for (Mine m : mines) {
			if (ChatColor.stripColor(Text.addColor(m.name)).equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<MineIcon> getMineList() {
		ArrayList<MineIcon> l = new ArrayList<MineIcon>();
		for (Mine mine : mines) {
			l.add(new MineIcon(mine));
		}
		if (l.size() == 0) {
			l.add(new MineIcon());
		}
		return l;
	}

	public static void teleport(Player target, Mine m, Boolean silent) {
		if (silent == false) {
			if (m.hasTP() == true) {
				target.teleport(m.teleport);
				target.sendMessage(RealMines.getPrefix() +Text.addColor("&fTeleported to mine &9" + m.name));
			} else {
				target.sendMessage(RealMines.getPrefix() +Text.addColor("&fThis mine &cdoesnt have a teleport location."));
			}
		} else {
			if (m.hasTP() == true) {
				target.teleport(m.teleport);
			}
		}
	}

	public static Mine getMine(String name) {
		for (Mine m : mines) {
			if (ChatColor.stripColor(Text.addColor(m.name)).equalsIgnoreCase(name)) {
				return m;
			}
		}
		return null;
	}

	public static void findBlockBreak(Block b) {
		for (Mine m : mines) {
			if (m.c.contains(b)) {
				MineBlockBreakEvent exampleEvent = new MineBlockBreakEvent(m);
				Bukkit.getPluginManager().callEvent(exampleEvent);
			}
		}
	}

	public static void despertar(Mine m) {
		m.updateSigns();
		if (m.resetByPercentage == true) {
			if ((double) m.getRemainingBlocksPer() < m.resetByPercentageValue) {
				m.kickPlayers("&6Warning &fThis mine is going to be reset.");
				Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.pl, new Runnable() {
					@Override
					public void run() {
						m.reset();

						//  The reset method above has the broadcast. this has been commented out as it seems like it would do double announcements
						//Bukkit.broadcastMessage("&fMine �9" + m.name + " &fjust &aresetted.");
					}
				}, 10);
			}
		}
	}

	public static ArrayList<MineSign> getSigns() {
		ArrayList<MineSign> l = new ArrayList<MineSign>();
		for (Mine m : mines) {
			l.addAll(m.signs);
		}
		return l;
	}

	public static void unloadMines() {
		mines.clear();
	}

	public static void setRegion(String name, MinePlayer mp) {
		Mine m = getMine(name);
		if (mp.pos1 == null || mp.pos2 == null) {
			mp.sendMessage("&fYour boundaries &carent set.");
			return;
		}
		Location p1 = mp.pos1.getLocation();
		Location p2 = mp.pos2.getLocation();
		m.pos1 = p1;
		m.pos2 = p2;
		m.saveData(Data.REGION);
		mp.sendMessage("&fRegion &aupdated.");
		m.reset();
	}

	public static void stopTasks() {
		for (Mine m : mines) {
			m.timer.kill();
		}
	}

	public static void startTasks() {
		for (Mine m : mines) {
			m.timer.start();
		}
	}

	public static void deleteMine(Mine mine) {
		mine.clear();
		mine.timer.kill();
		mine.removeDependencies();
		unregisterMine(mine);
	}
}
