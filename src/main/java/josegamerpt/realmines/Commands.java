package josegamerpt.realmines;

import josegamerpt.realmines.classes.Mine;
import josegamerpt.realmines.classes.MinePlayer;
import josegamerpt.realmines.config.Config;
import josegamerpt.realmines.config.Mines;
import josegamerpt.realmines.gui.GUIManager;
import josegamerpt.realmines.gui.MineViewer;
import josegamerpt.realmines.managers.MineManager;
import josegamerpt.realmines.managers.PlayerManager;
import josegamerpt.realmines.utils.Text;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Command("realmines")
@Alias({"mine", "rm"})
public class Commands extends CommandBase {

	String playerOnly = "[RealMines] Only players can run this command.";

	@Default
	public void defaultCommand(final CommandSender commandSender) {
		Text.sendList(commandSender,
				Arrays.asList("", "        &9Real&bMines", "&7Release &a" + RealMines.pl.getDescription().getVersion(), "",
						"/mine create <name>","/mine delete <name>", "/mine list", "/mine settp", "/mine setregion",
						"/mine reset <name>", "/mine reload", "/mine startTasks", "/mine stopTasks", ""));
	}

	@SubCommand("reload")
	@Permission("realmines.reload")
	public void reloadcmd(final CommandSender commandSender) {
		RealMines.reload();
		Text.send(commandSender,  "&aReloaded.");
	}

	@SubCommand("list")
	@Alias("t")
	@Permission("realmines.list")
	public void listcmd(final CommandSender commandSender) {
		if (commandSender instanceof Player) {
			MineViewer v = new MineViewer(Bukkit.getPlayer(commandSender.getName()));
			v.openInventory(Bukkit.getPlayer(commandSender.getName()));
		} else {
			commandSender.sendMessage(playerOnly);
		}
	}

	@SubCommand("stoptasks")
	@Permission("realmines.admin")
	public void stoptaskscmd(final CommandSender commandSender) {
		if (commandSender instanceof Player) {
			MineManager.stopTasks();
			commandSender.sendMessage(Text.color(RealMines.getPrefix() + "&cStopped &fall mine tasks."));
		} else {
			commandSender.sendMessage(playerOnly);
		}
	}

	@SubCommand("starttasks")
	@Permission("realmines.admin")
	public void starttaskcmd(final CommandSender commandSender) {
		if (commandSender instanceof Player) {
			MineManager.startTasks();
			commandSender.sendMessage(Text.color(RealMines.getPrefix() + "&aStarted &fall mine tasks."));
		} else {
			commandSender.sendMessage(playerOnly);
		}
	}

	@SubCommand("create")
	@Completion("#createsuggestions")
	@Permission("realmines.admin")
	@WrongUsage("&c/mine create <name>")
	public void createcmd(final CommandSender commandSender, final String name) {
		if (commandSender instanceof Player) {
			String mineName = ChatColor.stripColor(name);
			if (!MineManager.exists(mineName)) {
				MineManager.createMine(Bukkit.getPlayer(commandSender.getName()), mineName);
			} else {
				Text.send(commandSender,  "&cThere is already a mine with that name.");
			}
		} else {
			commandSender.sendMessage(playerOnly);
		}
	}

	@SubCommand("settp")
	@Completion("#mines")
	@Permission("realmines.admin")
	@WrongUsage("&c/mine settp <name>")
	public void settpcmd(final CommandSender commandSender, final String name) {
		if (commandSender instanceof Player) {
			if (MineManager.exists(name)) {
				Mine m = MineManager.getMine(name);
				MinePlayer mp = PlayerManager.get(Bukkit.getPlayer(commandSender.getName()));
				m.setTeleport(mp.getPlayer().getLocation());
				m.saveData(Mine.Data.TELEPORT);
				mp.sendMessage("&fTeleport &Aset &fon mine " + m.getDisplayName());
			} else {
				Text.send(commandSender,  "&cNo mine exists with that name.");
			}
		} else {
			commandSender.sendMessage(playerOnly);
		}
	}

	@SubCommand("open")
	@Alias("o")
	@Completion("#mines")
	@Permission("realmines.admin")
	@WrongUsage("&c/mine open <name>")
	public void opencmd(final CommandSender commandSender, final String name) {
		if (commandSender instanceof Player) {
			if (MineManager.exists(name)) {
				Mine m = MineManager.getMine(name);
				GUIManager.openMine(m, Bukkit.getPlayer(commandSender.getName()));
			} else {
				Text.send(commandSender,  "&cNo mine exists with that name.");
			}
		} else {
			commandSender.sendMessage(playerOnly);
		}
	}

	@SubCommand("reset")
	@Completion("#mines")
	@Permission("realmines.admin")
	@WrongUsage("&c/mine reset <name>")
	public void resetcmd(final CommandSender commandSender, final String name) {
		if (commandSender instanceof Player) {
			if (MineManager.exists(name)) {
				Mine m = MineManager.getMine(name);
				m.reset();
				Text.send(commandSender, "&fMine " + m.getDisplayName() + " &fhas been &aresetted.");
			} else {
				Text.send(commandSender, "&cNo mine exists with that name.");
			}
		} else {
			commandSender.sendMessage(playerOnly);
		}
	}

	@SubCommand("delete")
	@Completion("#mines")
	@Permission("realmines.admin")
	@WrongUsage("&c/mine delete <name>")
	public void deletecmd(final CommandSender commandSender, final String name) {
		if (commandSender instanceof Player) {
			if (MineManager.exists(name)) {
				MineManager.deleteMine(MineManager.getMine(name));
				Text.send(commandSender,  "&fMine deleted.");
			} else {
				Text.send(commandSender, "&cNo mine exists with that name.");
			}
		} else {
			commandSender.sendMessage(playerOnly);
		}
	}

	@SubCommand("clear")
	@Completion("#mines")
	@Permission("realmines.admin")
	@WrongUsage("&c/mine clear <name>")
	public void clearcmd(final CommandSender commandSender, final String name) {
		if (commandSender instanceof Player) {
			if (MineManager.exists(name)) {
				Mine m = MineManager.getMine(name);
				m.clear();
				Text.send(commandSender, "&fMine has been &acleared.");
			} else {
				Text.send(commandSender, "&cNo mine exists with that name.");
			}
		} else {
			commandSender.sendMessage(playerOnly);
		}
	}

	@SubCommand("setregion")
	@Completion("#mines")
	@Permission("realmines.admin")
	@WrongUsage("&c/mine setregion <name>")
	public void setregioncmd(final CommandSender commandSender, final String name) {
		if (commandSender instanceof Player) {
			if (MineManager.exists(name)) {
				MineManager.setRegion(name, Bukkit.getPlayer(commandSender.getName()));
			} else {
				Text.send(commandSender,  "&cThere is no mine with that name.");
			}
		} else {
			commandSender.sendMessage(playerOnly);
		}
	}
}