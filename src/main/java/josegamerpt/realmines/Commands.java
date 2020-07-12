package josegamerpt.realmines;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import josegamerpt.realmines.classes.Enum.Data;
import josegamerpt.realmines.config.Config;
import josegamerpt.realmines.config.Mines;
import josegamerpt.realmines.classes.Mine;
import josegamerpt.realmines.classes.MinePlayer;
import josegamerpt.realmines.gui.MineViewer;
import josegamerpt.realmines.managers.MineManager;
import josegamerpt.realmines.managers.PlayerManager;
import josegamerpt.realmines.utils.Text;

public class Commands implements CommandExecutor {

	String nop = "&cSorry but you don't have permission to use this command.";

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if ((sender instanceof Player)) {
			Player p = (Player) sender;
			MinePlayer mp = PlayerManager.get(p);
			if ((cmd.getName().equalsIgnoreCase("mine")) && (p.hasPermission("RealMines.Admin"))) {
				if (args.length == 0) {
					printHelp(p);
				} else if (args.length == 1) {
					if (args[0].equals("create")) {
						Text.send(p,  "&cWrong usage. &f/mine create <name>");
					} else if (args[0].equals("wand")) {
						Text.send(p, "&fYou can use the &aSelection Tool &fto select a region for the new mine.");
						p.getInventory().addItem(RealMines.SelectionTool);
					} else if (args[0].equals("list")) {
						MineViewer v = new MineViewer(p);
						v.openInventory(p);
					} else if (args[0].equals("stopTasks")) {
						MineManager.stopTasks();
						mp.sendMessage("&cStopped &fall mine tasks.");
					} else if (args[0].equals("startTasks")) {
						MineManager.startTasks();
						mp.sendMessage("&aStarted &fall mine tasks.");
					} else if (args[0].equals("reload")) {
						Config.reload();
						Mines.reload();
						RealMines.prefix = Text.color(Config.file().getString("RealMines.Prefix"));
						MineManager.unloadMines();
						MineManager.loadMines();
						Text.send(p,  "&aReloaded.");
					} else {
						Text.send(p, "&fNo command has been found with that syntax.");
					}
				} else if (args.length == 2) {
					if (args[0].equals("create")) {
						String name = args[1];
						if (MineManager.exists(name) == false) {
							MineManager.createMine(mp, name);
						} else {
							Text.send(p,  "&cThere is already a mine with that name.");
						}
					} else if (args[0].equals("settp")) {
						if (MineManager.exists(args[1]) == true) {
							Mine m = MineManager.getMine(args[1]);
							m.setTeleport(p.getLocation());
							m.saveData(Data.TELEPORT);
							mp.sendMessage("&fTeleport &Aset.");
						} else {
							Text.send(p,  "&cNo mine exists with that name.");
						}
					} else if (args[0].equals("reset")) {
						if (MineManager.exists(args[1]) == true) {
							Mine m = MineManager.getMine(args[1]);
							m.reset();
						} else {
							Text.send(p, "&cNo mine exists with that name.");
						}
					} else if (args[0].equals("delete")) {
						if (MineManager.exists(args[1]) == true) {
							MineManager.deleteMine(MineManager.getMine(args[1]));
							Text.send(p,  "&fMine deleted.");
						} else {
							Text.send(p, "&cNo mine exists with that name.");
						}
					} else if (args[0].equals("clear")) {
						if (MineManager.exists(args[1]) == true) {
							Mine m = MineManager.getMine(args[1]);
							m.clear();
							mp.sendMessage(Text.color("&fMine has been &acleared."));
						} else {
							Text.send(p, "&cNo mine exists with that name.");
						}
					} else if (args[0].equals("setregion")) {
						String name = args[1];
						if (MineManager.exists(name) == true) {
							MineManager.setRegion(name, mp);
						} else {
							Text.send(p,  "&cThere is no mine with that name.");
						}
					} else {
						Text.send(p, "&fNo command has been found with that syntax.");
					}
				} else if (args.length == 3) {
					//
				} else {
					printHelp(p);
				}
			} else {
				Text.send(p,  nop);
			}
		} else {
			System.out.print("Only players can execute this command.");
		}
		return false;
	}

	private void printHelp(Player p) {
		Text.sendList(p,
				Arrays.asList("", "        &9Real&bMines", "&7Release &a" + RealMines.pl.getDescription().getVersion(), "",
						"/mine create <name>","/mine delete <name>", "/mine list", "/mine wand", "/mine settp", "/mine setregion",
						"/mine reset <name>", "/mine reload", "/mine startTasks", "/mine stopTasks", ""));
	}
}
