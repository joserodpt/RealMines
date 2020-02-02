package josegamerpt.realmines.classes;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import josegamerpt.realmines.classes.Enum.Data;
import josegamerpt.realmines.managers.MineManager;
import josegamerpt.realmines.utils.Text;

public class Mine {

	public String name;
	public ArrayList<MineBlock> blocks;
	public ArrayList<Material> sorted = new ArrayList<Material>();
	public ArrayList<MineSign> signs;
	public Location teleport;
	public Location pos1;
	public Location pos2;
	public Material icon;
	public MineCuboid c;
	public boolean resetByPercentage = true;
	public boolean resetByTime = true;
	public Double resetByPercentageValue;
	public int resetByTimeValue;
	public MineTimer timer;

	public Mine(String n, ArrayList<MineBlock> b, ArrayList<MineSign> si, Location p1, Location p2, Material i,
			Location t, Boolean resetByPercentag, Boolean resetByTim, Double rbpv, int rbtv) {
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
		kickPlayers(this.name + " &fis being &eresetted.");
		fillMine();
		updateSigns();
		Bukkit.broadcastMessage("§fMine §9" + this.name + " §fjust §aresetted.");
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
				sign.setLine(3, "" + Text.addColor(this.name));
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
			p.sendMessage(Text.addColor(s));
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
}
