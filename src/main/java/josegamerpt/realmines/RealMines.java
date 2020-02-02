package josegamerpt.realmines;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import josegamerpt.realmines.config.Config;
import josegamerpt.realmines.config.Mines;
import josegamerpt.realmines.events.BlockInteractions;
import josegamerpt.realmines.events.BlockModify;
import josegamerpt.realmines.events.PlayerEvents;
import josegamerpt.realmines.gui.MaterialPicker;
import josegamerpt.realmines.gui.MineBlocksViewer;
import josegamerpt.realmines.gui.MineResetMenu;
import josegamerpt.realmines.gui.MineViewer;
import josegamerpt.realmines.managers.MineManager;
import josegamerpt.realmines.managers.PlayerManager;
import josegamerpt.realmines.utils.GUIBuilder;
import josegamerpt.realmines.utils.Itens;
import josegamerpt.realmines.utils.PlayerInput;
import josegamerpt.realmines.utils.Text;

public class RealMines extends JavaPlugin {

	public static ItemStack SelectionTool = Itens.createItem(Material.IRON_PICKAXE, 1, "&9Selection &fTool");

	PluginManager pm = Bukkit.getPluginManager();
	public static Plugin pl;

	public static String prefix;

	static String name = "[RealMines] ";

	public static String nmsver;

	public void onEnable() {
		pl = this;

		String star = "<------------------ RealMines PT ------------------>".replace("PT", "| " +
				this.getDescription().getVersion());
		log(star);
		log("Loading Config Files.");
		saveDefaultConfig();
		Config.setup(this);
		Mines.setup(this);

		log("Registering Events.");
		pm.registerEvents(new BlockInteractions(), this);
		pm.registerEvents(new PlayerEvents(), this);
		pm.registerEvents(MineViewer.getListener(), this);
		pm.registerEvents(GUIBuilder.getListener(), this);
		pm.registerEvents(MaterialPicker.getListener(), this);
		pm.registerEvents(MineBlocksViewer.getListener(), this);
		pm.registerEvents(new BlockModify(), this);
		pm.registerEvents(PlayerInput.getListener(), this);
		pm.registerEvents(MineResetMenu.getListener(), this);

		log("Registering Commands.");
		getCommand("realmines").setExecutor(new Commands());

		log("Loading Mines.");
		MineManager.loadMines();
		log("Loaded " + MineManager.mines.size() + " mines and " + MineManager.getSigns().size() + " mine signs.");

		nmsver = Bukkit.getServer().getClass().getPackage().getName();
		nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);

		prefix = Text.addColor(Config.file().getString("RealMines.Prefix"));

		log("Plugin has been loaded.");
		log("Author: JoseGamer_PT | " + this.getDescription().getWebsite());
		log(star);

		for (Player p : Bukkit.getOnlinePlayers()) {
			PlayerManager.loadPlayer(p);
		}
	}

	public static void log(String string) {
		System.out.print(name + string);
	}

	public static String getPrefix() {
		return prefix + " ";
	}
}
