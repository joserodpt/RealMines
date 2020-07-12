package josegamerpt.realmines.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import josegamerpt.realmines.classes.Enum;
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
					Mines.file().getInt(s + ".Settings.Reset.ByPercentageValue"),
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
			if (p.pos1.getWorld().equals(p.pos2.getWorld())) {
				Location p1 = p.pos1;
				Location p2 = p.pos2;
				Mine m = new Mine(name, new ArrayList<MineBlock>(), new ArrayList<MineSign>(), p1, p2,
						Material.DIAMOND_ORE, null, false, true, 20, 60);
				m.saveData(Data.INIT);

				m.register();
				m.addBlock(new MineBlock(Material.STONE, 100D));
				m.reset();
				p.sendMessage("&fMine &acreated.");
				p.clearSelection();
			} else {
				p.sendMessage("&cYou cant create multi-world mines.");
			}
		} else {
			p.sendMessage("&cYou didnt set the boundaries.");
		}
	}

	public static void saveMine(Mine mine, Data t) {
		if (t.equals(Data.ALL) || t.equals(Data.INIT)) {
			Mines.file().set(mine.getName() + ".World", mine.getPosition(1).getWorld().getName());
			Mines.file().set(mine.getName() + ".POS1.X", mine.getPosition(1).getX());
			Mines.file().set(mine.getName() + ".POS1.Y", mine.getPosition(1).getY());
			Mines.file().set(mine.getName() + ".POS1.Z", mine.getPosition(1).getZ());
			Mines.file().set(mine.getName() + ".POS2.X", mine.getPosition(2).getX());
			Mines.file().set(mine.getName() + ".POS2.Y", mine.getPosition(2).getY());
			Mines.file().set(mine.getName() + ".POS2.Z", mine.getPosition(2).getZ());
			Mines.file().set(mine.getName() + ".Icon", mine.getIcon().name());
			Mines.file().set(mine.getName() + ".Signs", mine.getSignList());
			Mines.file().set(mine.getName() + ".Blocks", mine.getBlockList());
			Mines.file().set(mine.getName() + ".Settings.Reset.ByPercentage", mine.isResetBy(Enum.Reset.PERCENTAGE));
			Mines.file().set(mine.getName() + ".Settings.Reset.ByTime", mine.isResetBy(Enum.Reset.TIME));
			Mines.file().set(mine.getName() + ".Settings.Reset.ByPercentageValue", mine.getResetValue(Enum.Reset.PERCENTAGE));
			Mines.file().set(mine.getName() + ".Settings.Reset.ByTimeValue", mine.getResetValue(Enum.Reset.TIME));
		}
		if (t.equals(Data.ICON)) {
			Mines.file().set(mine.getName() + ".Icon", mine.getIcon().name());
		}
		if (t.equals(Data.OPTIONS)) {
			Mines.file().set(mine.getName() + ".Settings.Reset.ByPercentage", mine.isResetBy(Enum.Reset.PERCENTAGE));
			Mines.file().set(mine.getName() + ".Settings.Reset.ByTime", mine.isResetBy(Enum.Reset.TIME));
			Mines.file().set(mine.getName() + ".Settings.Reset.ByPercentageValue", mine.getResetValue(Enum.Reset.PERCENTAGE));
			Mines.file().set(mine.getName() + ".Settings.Reset.ByTimeValue", mine.getResetValue(Enum.Reset.TIME));
		}
		if (t.equals(Data.REGION)) {
			Mines.file().set(mine.getName() + ".World", mine.getPosition(1).getWorld().getName());
			Mines.file().set(mine.getName() + ".POS1.X", mine.getPosition(1).getX());
			Mines.file().set(mine.getName() + ".POS1.Y", mine.getPosition(1).getY());
			Mines.file().set(mine.getName() + ".POS1.Z", mine.getPosition(1).getZ());
			Mines.file().set(mine.getName() + ".POS2.X", mine.getPosition(2).getX());
			Mines.file().set(mine.getName() + ".POS2.Y", mine.getPosition(2).getY());
			Mines.file().set(mine.getName() + ".POS2.Z", mine.getPosition(2).getZ());
		}
		if (t.equals(Data.TELEPORT)) {
			Mines.file().set(mine.getName() + ".Teleport.X", mine.getTeleport().getX());
			Mines.file().set(mine.getName() + ".Teleport.Y", mine.getTeleport().getY());
			Mines.file().set(mine.getName() + ".Teleport.Z", mine.getTeleport().getZ());
			Mines.file().set(mine.getName() + ".Teleport.Yaw", mine.getTeleport().getYaw());
			Mines.file().set(mine.getName() + ".Teleport.Pitch", mine.getTeleport().getPitch());
		}
		if (t.equals(Data.SIGNS)) {
			Mines.file().set(mine.getName() + ".Signs", mine.getSignList());
		}
		if (t.equals(Data.BLOCKS)) {
			Mines.file().set(mine.getName() + ".Blocks", mine.getBlockList());
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
				target.teleport(m.getTeleport());
				target.sendMessage(RealMines.getPrefix() +Text.color("&fTeleported to mine &9" + m.getName()));
			} else {
				target.sendMessage(RealMines.getPrefix() +Text.color("&fThis mine &cdoesnt have a teleport location."));
			}
		} else {
			if (m.hasTP() == true) {
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

	public static void findBlockBreak(Block b) {
		for (Mine m : mines) {
			if (m.getMine().contains(b)) {
				MineBlockBreakEvent exampleEvent = new MineBlockBreakEvent(m);
				Bukkit.getPluginManager().callEvent(exampleEvent);
			}
		}
	}

	public static void despertar(Mine m) {
		m.updateSigns();
		if (m.isResetBy(Enum.Reset.PERCENTAGE) == true) {
			if ((double) m.getRemainingBlocksPer() < m.getResetValue(Enum.Reset.PERCENTAGE)) {
				m.kickPlayers("&6Warning &fThis mine is going to be reset.");
				Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.pl, new Runnable() {
					@Override
					public void run() {
						m.reset();
					}
				}, 10);
			}
		}
	}

	public static ArrayList<MineSign> getSigns() {
		ArrayList<MineSign> l = new ArrayList<MineSign>();
		for (Mine m : mines) {
			l.addAll(m.getSigns());
		}
		return l;
	}

	public static void unloadMines() {
		mines.clear();
	}

	public static void setRegion(String name, MinePlayer mp) {
		Mine m = getMine(name);
		if (mp.pos1 == null || mp.pos2 == null) {
			mp.sendMessage("&fYour boundaries &care not set.");
			return;
		}
		Location p1 = mp.pos1;
		Location p2 = mp.pos2;
		m.setPosition(1, mp.pos1);
		m.setPosition(2, mp.pos2);
		m.saveData(Data.REGION);
		mp.sendMessage("&fRegion &aupdated.");
		m.reset();
	}

	public static void stopTasks() {
		for (Mine m : mines) {
			m.getTimer().kill();
		}
	}

	public static void startTasks() {
		for (Mine m : mines) {
			m.getTimer().start();
		}
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
}
