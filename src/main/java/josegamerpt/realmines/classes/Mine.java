package josegamerpt.realmines.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Config;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import josegamerpt.realmines.classes.Enum.Data;
import josegamerpt.realmines.managers.MineManager;
import josegamerpt.realmines.utils.Text;

public class Mine {

	private String name;
	private ArrayList<MineBlock> blocks;
	private ArrayList<Material> sorted = new ArrayList<Material>();
	private ArrayList<MineSign> signs;
	private Location teleport;
	private Location pos1;
	private Location pos2;
	private Material icon;
	private MineCuboid c;
	private boolean resetByPercentage = true;
	private boolean resetByTime = true;
	private int resetByPercentageValue;
	private int resetByTimeValue;
	private MineTimer timer;
	private boolean highlight = false;

	public Mine(String n, ArrayList<MineBlock> b, ArrayList<MineSign> si, Location p1, Location p2, Material i,
			Location t, Boolean resetByPercentag, Boolean resetByTim, int rbpv, int rbtv) {
		this.name = n;
		this.blocks = b;
		this.signs = si;
		this.pos1 = p1;
		this.pos2 = p2;
		this.icon = i;
		this.teleport = t;
		this.resetByPercentage = resetByPercentag;
		this.resetByTime = resetByTim;
		this.resetByPercentageValue = rbpv;
		this.resetByTimeValue = rbtv;
		timer = new MineTimer(this);
		if (resetByTim == true) {
			timer.start();
		}
		fillMine();
		updateSigns();
	}

	public boolean hasTP() {
		return this.teleport != null;
	}

	public ArrayList<String> getSignList() {
		ArrayList<String> l = new ArrayList<String>();
		for (MineSign ms : signs) {
			Block b = ms.block;
			l.add(b.getWorld().getName() + ";" + b.getX() + ";" + b.getY() + ";" + b.getZ() + ";" + ms.mod);
		}
		return l;
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
		c = new MineCuboid(pos1, pos2);
		sortBlocks();
		if (blocks.size() != 0) {
			for (Block block : c)
				block.setType(getBlock());
		}
	}

	private void sortBlocks() {
		double per = 0;
		for (MineBlock d : blocks) {
			per = per + d.percentage;
		}
		sorted.clear();
		for (MineBlock d : blocks) {
			for (int i = 0; i < d.getPerInt(); i++) {
				sorted.add(d.material);
			}
		}
	}

	private Material getBlock() {
		Random r = new Random();
		Material m = null;
		for (@SuppressWarnings("unused")
		MineBlock d : blocks) {
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
		if (this.resetByTime == false) {
			timer.kill();
		} else {
			timer.restart();
		}
	}

	public ArrayList<String> getBlockList() {
		ArrayList<String> l = new ArrayList<String>();
		for (MineBlock b : blocks) {
			l.add(b.material.name() + ";" + b.percentage);
		}
		return l;
	}

	public ArrayList<MineBlockIcon> getBlocks() {
		ArrayList<MineBlockIcon> l = new ArrayList<MineBlockIcon>();
		for (MineBlock b : blocks) {
			l.add(new MineBlockIcon(b));
		}
		if (l.size() == 0) {
			l.add(new MineBlockIcon());
		}
		return l;
	}

	public void reset() {
		kickPlayers(this.name + " &fis being &ereset.");
		fillMine();
		updateSigns();
		if(Config.file().getBoolean("RealMines.announceResets")) {
			Bukkit.broadcastMessage(Text.color(RealMines.getPrefix() + Config.file().getString("RealMines.resetAnnouncement").replace("%mine%", this.name)));
		}
	}

	public void addSign(Block block, String modif) {
		signs.add(new MineSign(block, modif));
		this.saveData(Data.SIGNS);
	}

	public void updateSigns() {
		for (MineSign ms : signs) {
			if (ms.block.getType().name().contains("SIGN")) {
				Sign sign = (Sign) ms.block.getState();
				String modif = ms.mod;

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
				sign.setLine(3, "" + Text.color(this.name));
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

	public void setName(String input) {
		this.timer.kill();
		MineManager.unregisterMine(this);
		this.name = input;
		this.register();
		saveData(Data.ALL);
		saveData(Data.TELEPORT);
	}

	public void clear() {
		for (Block block : c)
			block.setType(Material.AIR);
	}

	public void kickPlayers(String s) {
		broadcastMessage(s);
		for (Player p : getPlayersInMine()) {
			MineManager.teleport(p, this, false);
		}
	}

	public void broadcastMessage(String s) {
		for (Player p : getPlayersInMine()) {
			p.sendMessage(Text.color(RealMines.getPrefix() + s));
		}
	}

	public ArrayList<Player> getPlayersInMine() {
		ArrayList<Player> ps = new ArrayList<Player>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (c.contains(p.getLocation())) {
				ps.add(p);
			}
		}
		return ps;
	}

	public int getPlayersInMineCount() {
		return getPlayersInMine().size();
	}

	public void removeDependencies() {
		for (MineSign ms : signs)
		{
			ms.block.getLocation().getWorld().getBlockAt(ms.block.getLocation()).setType(Material.AIR);
		}
	}

	public List<Location> getCube() {
		List<Location> result = new ArrayList<>();
		World world = pos1.getWorld();
		double minX = Math.min(pos1.getX(), pos2.getX());
		double minY = Math.min(pos1.getY(), pos2.getY());
		double minZ = Math.min(pos1.getZ(), pos2.getZ());
		double maxX = Math.max(pos1.getX() + 1, pos2.getX() + 1);
		double maxY = Math.max(pos1.getY() + 1, pos2.getY() + 1);
		double maxZ = Math.max(pos1.getZ() + 1, pos2.getZ() + 1);
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

	public boolean isResetBy(Enum.Reset e)
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

	public int getResetValue(Enum.Reset e) {
		switch (e)
		{
			case PERCENTAGE:
				return this.resetByPercentageValue;
			case TIME:
				return this.resetByTimeValue;
		}
		return -1;
	}

	public void setResetStatus(Enum.Reset e, boolean b) {
		switch (e)
		{
			case PERCENTAGE:
				this.resetByPercentage = b;
			case TIME:
				this.resetByTime = b;
		}
	}

	public void setResetValue(Enum.Reset e, int d) {
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

	public Location getPosition(int i) {
		switch (i)
		{
			case 1:
				return this.pos1;
			case 2:
				return this.pos2;
		}
		return null;
	}

	public Location getTeleport() {
		return this.teleport;
	}

	public ArrayList<MineSign> getSigns() {
		return this.signs;
	}

	public void setPosition(int i, Location l) {
		switch (i)
		{
			case 1:
				this.pos1 = l;
			case 2:
				this.pos1 = l;
		}
	}

	public MineTimer getTimer() {
		return this.timer;
	}

	public void setTeleport(Location location) {
		this.teleport = location;
	}
}
