package josegamerpt.realmines;

import josegamerpt.realmines.classes.Mine;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.gui.GUIManager;
import josegamerpt.realmines.gui.MineViewer;
import josegamerpt.realmines.utils.Text;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
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
                Arrays.asList("", "         &9Real&bMines", "&7Release &a" + RealMines.getPlugin().getDescription().getVersion(), ""));
    }

    @SubCommand("reload")
    @Permission("realmines.reload")
    public void reloadcmd(final CommandSender commandSender) {
        RealMines.reload();
        Text.send(commandSender, "&aReloaded.");
    }

    @SubCommand("panel")
    @Alias("p")
    @Permission("realmines.panel")
    public void panelcmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = Bukkit.getPlayer(commandSender.getName());
            MineViewer v = new MineViewer(p);
            v.openInventory(p);
        } else {
            commandSender.sendMessage(playerOnly);
        }
    }

    @SubCommand("stoptasks")
    @Permission("realmines.admin")
    public void stoptaskscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MineManager.stopTasks();
            Text.send(commandSender, Language.file().getString("System.Stopped-Mine-Tasks"));
        } else {
            commandSender.sendMessage(playerOnly);
        }
    }

    @SubCommand("starttasks")
    @Permission("realmines.admin")
    public void starttaskcmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MineManager.startTasks();
            Text.send(commandSender, Language.file().getString("System.Started-Mine-Tasks"));
        } else {
            commandSender.sendMessage(playerOnly);
        }
    }

    @SubCommand("list")
    @Permission("realmines.admin")
    public void listcmd(final CommandSender commandSender) {
        Text.send(commandSender, RealMines.getPrefix());
        MineManager.getMines().forEach(mine -> Text.send(commandSender, " &7> &f" + mine.getName() + " &r&7(&f" + mine.getDisplayName() + "&r&7)"));
    }

    @SubCommand("create")
    @Completion("#createsuggestions")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine create <name>")
    public void createcmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Mine m = MineManager.get(name);
            if (m == null) {
                MineManager.createMine(Bukkit.getPlayer(commandSender.getName()), name);
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Exists"));
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
            Mine m = MineManager.get(name);
            if (m != null) {
                m.setTeleport(((Player) commandSender).getLocation());
                m.saveData(Mine.Data.TELEPORT);

                Text.send(commandSender, Language.file().getString("Mines.Teleport-Set").replaceAll("%mine%", m.getDisplayName()));
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            commandSender.sendMessage(playerOnly);
        }
    }

    @SubCommand("tp")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine tp <name>")
    public void tpmine(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Mine m = MineManager.get(name);
            if (m != null) {
                MineManager.teleport(((Player) commandSender), m, false);
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
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
            Mine m = MineManager.get(name);
            if (m != null) {
                GUIManager.openMine(m, Bukkit.getPlayer(commandSender.getName()));
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
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
            Mine m = MineManager.get(name);
            if (m != null) {
                m.reset();
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            commandSender.sendMessage(playerOnly);
        }
    }

    @SubCommand("delete")
    @Alias("del")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine delete <name>")
    public void deletecmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            MineManager.deleteMine(MineManager.get(name));
            Text.send(commandSender, "&fMine deleted.");
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
            Mine m = MineManager.get(name);
            if (m != null) {
                m.clear();
                Text.send(commandSender, Language.file().getString("System.Mine-Clear"));
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
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
            Mine m = MineManager.get(name);

            if (m != null) {
                MineManager.setRegion(name, Bukkit.getPlayer(commandSender.getName()));
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            commandSender.sendMessage(playerOnly);
        }
    }
}