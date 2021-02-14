package josegamerpt.realmines.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Config;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import josegamerpt.realmines.managers.MineManager;
import josegamerpt.realmines.utils.Text;

public class Mine {

	public enum Reset {PERCENTAGE, TIME}

	public enum Data {BLOCKS, ICON, ALL, TELEPORT, SIGNS, REGION, INIT, OPTIONS, NAME}

	private String name;
	private String displayName;
	private ArrayList<MineBlock> blocks;

	private ArrayList<Material> sorted = new ArrayList<>();
	private ArrayList<MineSign> signs;
	private Location teleport;
	private Material icon;
	private MineCuboid c;
	private boolean resetByPercentage;
	private boolean resetByTime;
	private int resetByPercentageValue;
	private int resetByTimeValue;
	private MineTimer timer;
	private boolean highlight = false;

	private Location l1;
	private Location l2;

	public Mine(String n, String displayname, ArrayList<MineBlock> b, ArrayList<MineSign> si, Location p1, Location p2, Material i,
			Location t, Boolean resetByPercentag, Boolean resetByTim, int rbpv, int rbtv) {
		this.name = ChatColor.stripColor(Text.color(n));
		this.displayName = displayname;
		this.blocks = b;
		this.signs = si;
		this.icon = i;
		this.teleport = t;
		this.resetByPercentage = resetByPercentag;
		this.resetByTime = resetByTim;
		this.resetByPercentageValue = rbpv;
		this.resetByTimeValue = rbtv;

		timer = new MineTimer(this);
		if (resetByTim) {
			timer.start();
		}

		setPOS(p1, p2);
		updateSigns();
	}

	public Location getPOS1() {
		return this.l1;
	}

	public Location getPOS2() {
		return this.l2;
	}

	public void setPOS(Location p1, Location p2) {
		this.l1 = p1;
		this.l2 = p2;
		fillMine();
	}

	public boolean hasTP() {
		return this.teleport != null;
	}

	public ArrayList<String> getSignList() {
		ArrayList<String> l = new ArrayList<>();
		signs.forEach(mineSign -> l.add(mineSign.getBlock().getWorld().getName() + ";" + mineSign.getBlock().getX() + ";" + mineSign.getBlock().getY() + ";" + mineSign.getBlock().getZ() + ";" + mineSign.getModifier()));

		return l;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public MineCuboid getMine()
	{
		return this.c;
	}

	public int getBlockCount() {
		return c.getTotalVolume();
	}

	public int getRemainingBlocks() {
		return c.getTotalBlocks();
	}

	public int getRemainingBlocksPer() {
		return (c.getTotalBlocks() * 100 / getBlockCount());
	}

	public int getMinedBlocks() {
		return getBlockCount() - getRemainingBlocks();
	}

	public int getMinedBlocksPer() {
		return (getMinedBlocks() * 100 / getBlockCount());
	}

	public void fillMine() {
		c = new MineCuboid(this.l1, this.l2);
		sortBlocks();
		if (blocks.size() != 0) {
			c.forEach(block -> block.setType(getBlock()));
		}
		updateSigns();
	}

	private void sortBlocks() {
		sorted.clear();
		for (MineBlock d : blocks) {
			for (int i = 0; i < d.getPerInt(); i++) {
				sorted.add(d.getMaterial());
			}
		}
	}

	private Material getBlock() {
		Random r = new Random();
		Material m = null;
		for (int i = 0; i < blocks.size(); i++) {
			int chance = r.nextInt(sorted.size());
			m = sorted.get(chance);
		}
		return m;
	}

	public void register() {
		MineManager.mines.add(this);
	}

	public void saveData(Data t) {
		MineManager.saveMine(this, t);
		if (!this.resetByTime) {
			timer.kill();
		} else {
			timer.restart();
		}
	}

	public ArrayList<String> getBlockList() {
		ArrayList<String> l = new ArrayList<>();
		blocks.forEach(mineBlock -> l.add(mineBlock.getMaterial().name() + ";" + mineBlock.getPercentage()));
		return l;
	}

	public ArrayList<MineBlockIcon> getBlocks() {
		ArrayList<MineBlockIcon> l = new ArrayList<>();
		blocks.forEach(mineBlock -> l.add(new MineBlockIcon(mineBlock)));
		if (l.size() == 0) {
			l.add(new MineBlockIcon());
		}
		return l;
	}

	public void reset() {
		kickPlayers(getDisplayName() + " &fis being &ereset.");
		fillMine();
		updateSigns();
		if(Config.file().getBoolean("RealMines.announceResets")) {
			Bukkit.broadcastMessage(Text.color(RealMines.getPrefix() + Config.file().getString("RealMines.resetAnnouncement").replace("%mine%", getDisplayName())));
		}
	}

	public void addSign(Block block, String modif) {
		this.signs.add(new MineSign(block, modif));
		this.saveData(Data.SIGNS);
	}

	public void updateSigns() {
		for (MineSign ms : signs) {
			if (ms.getBlock().getType().name().contains("SIGN")) {
				Sign sign = (Sign) ms.getBlock().getState();
				String modif = ms.getModifier();

				if (modif.equalsIgnoreCase("PM")) {
					sign.setLine(1, getMinedBlocksPer() + "%");
					sign.setLine(2, "mined on");
				}
				if (modif.equalsIgnoreCase("BM")) {
					sign.setLine(1, "" + getMinedBlocks());
					sign.setLine(2, "mined blocks on");

				}
				if (modif.equalsIgnoreCase("BR")) {
					sign.setLine(1, "" + getRemainingBlocks());
					sign.setLine(2, "blocks on");

				}
				if (modif.equalsIgnoreCase("PL")) {
					sign.setLine(1, getRemainingBlocksPer() + "%");
					sign.setLine(2, "left on");
				}
				sign.setLine(0, "§7[§9Real§bMines§7]");
				sign.setLine(3, "" + Text.color(getDisplayName()));
				sign.update();
			}
		}
	}

	public void removeBlock(MineBlock mb) {
		blocks.remove(mb);
		saveData(Data.BLOCKS);
	}

	public void addBlock(MineBlock mineBlock) {
		blocks.add(mineBlock);
		saveData(Data.BLOCKS);
	}

	public void setDisplayName(String input) {
		this.displayName = input;
		saveData(Data.NAME);
	}

	public void clear() {
		this.c.forEach(block -> block.setType(Material.AIR));
	}

	public void kickPlayers(String s) {
		broadcastMessage(s);
		this.getPlayersInMine().forEach(player -> MineManager.teleport(player, this, false));
	}

	public void broadcastMessage(String s) {
		for (Player p : getPlayersInMine()) {
			p.sendMessage(Text.color(RealMines.getPrefix() + s));
		}
	}

	public ArrayList<Player> getPlayersInMine() {
		ArrayList<Player> ps = new ArrayList<>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (c.contains(p.getLocation())) {
				ps.add(p);
			}
		}
		return ps;
	}

	public void removeDependencies() {
		signs.forEach(ms -> ms.getBlock().getLocation().getWorld().getBlockAt(ms.getBlock().getLocation()).setType(Material.AIR));
	}

	public List<Location> getCube() {
		List<Location> result = new ArrayList<>();
		World world = this.c.getPOS1().getWorld();
		double minX = Math.min(this.c.getPOS1().getX(), this.c.getPOS2().getX());
		double minY = Math.min(this.c.getPOS1().getY(), this.c.getPOS2().getY());
		double minZ = Math.min(this.c.getPOS1().getZ(), this.c.getPOS2().getZ());
		double maxX = Math.max(this.c.getPOS1().getX() + 1, this.c.getPOS2().getX() + 1);
		double maxY = Math.max(this.c.getPOS1().getY() + 1, this.c.getPOS2().getY() + 1);
		double maxZ = Math.max(this.c.getPOS1().getZ() + 1, this.c.getPOS2().getZ() + 1);
		double dist = 0.5D;
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
	}

	public void highlight() {
		if (highlight)
		{
			getCube().forEach(location -> location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(), 0, 0.001, 1, 0, 1, new Particle.DustOptions(Color.AQUA, 1)));
		}
	}

	public String getName() {
		return this.name;
	}

	public boolean isResetBy(Reset e)
	{
		switch (e)
		{
			case PERCENTAGE:
				return this.resetByPercentage;
			case TIME:
				return this.resetByTime;
		}
		return false;
	}

	public int getResetValue(Reset e) {
		switch (e)
		{
			case PERCENTAGE:
				return this.resetByPercentageValue;
			case TIME:
				return this.resetByTimeValue;
		}
		return -1;
	}

	public void setResetStatus(Reset e, boolean b) {
		switch (e)
		{
			case PERCENTAGE:
				this.resetByPercentage = b;
			case TIME:
				this.resetByTime = b;
		}
	}

	public void setResetValue(Reset e, int d) {
		switch (e)
		{
			case PERCENTAGE:
				this.resetByPercentageValue = d;
			case TIME:
				this.resetByTimeValue = d;
		}
	}

	public Material getIcon() {
		return this.icon;
	}

	public boolean isHighlighted() {
		return this.highlight;
	}

	public void setHighlight(boolean b) {
		this.highlight = b;
	}

	public void setIcon(Material a) {
		this.icon = a;
	}

	public Location getTeleport() {
		return this.teleport;
	}

	public ArrayList<MineSign> getSigns() {
		return this.signs;
	}


	public MineTimer getTimer() {
		return this.timer;
	}

	public void setTeleport(Location location) {
		this.teleport = location;
	}
}
