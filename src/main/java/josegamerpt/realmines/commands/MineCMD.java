package josegamerpt.realmines.commands;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.gui.MineViewer;
import josegamerpt.realmines.mines.RMine;
import josegamerpt.realmines.utils.Text;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Command("realmines")
@Alias({"mine", "rm"})
public class MineCMD extends CommandBase {

    String playerOnly = Language.file().getString("System.Player-Only");
    private RealMines rm;

    public MineCMD(RealMines rm) {
        this.rm = rm;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        Text.sendList(commandSender,
                Arrays.asList("", "         &9Real&bMines", "         &7Release &a" + RealMines.getInstance().getDescription().getVersion(), ""));
    }

    @SubCommand("reload")
    @Permission("realmines.reload")
    public void reloadcmd(final CommandSender commandSender) {
        this.rm.reload();
        Text.send(commandSender, Language.file().getString("System.Reloaded"));
    }

    @SubCommand("panel")
    @Alias("p")
    @Permission("realmines.panel")
    public void panelcmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = Bukkit.getPlayer(commandSender.getName());
            MineViewer v = new MineViewer(rm, p);
            v.openInventory(p);
        } else {
            Text.send(commandSender, playerOnly);
        }
    }

    @SubCommand("stoptasks")
    @Permission("realmines.admin")
    public void stoptaskscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            RealMines.getInstance().getMineManager().stopTasks();
            Text.send(commandSender, Language.file().getString("System.Stopped-Mine-Tasks"));
        } else {
            Text.send(commandSender, playerOnly);
        }
    }

    @SubCommand("starttasks")
    @Permission("realmines.admin")
    public void starttaskcmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            RealMines.getInstance().getMineManager().startTasks();
            Text.send(commandSender, Language.file().getString("System.Started-Mine-Tasks"));
        } else {
            Text.send(commandSender, playerOnly);
        }
    }

    @SubCommand("list")
    @Permission("realmines.admin")
    public void listcmd(final CommandSender commandSender) {
        RealMines.getInstance().getMineManager().getMines().forEach(mine -> Text.send(commandSender, "&7> &f" + mine.getName() + " &r&7(&f" + mine.getDisplayName() + "&r&7)"));
    }

    @SubCommand("create")
    @Completion("#createsuggestions")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine create <name>")
    public void createcmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            RMine m = RealMines.getInstance().getMineManager().get(name);
            if (m == null) {
                RealMines.getInstance().getGUIManager().openMineChooserType((Player) commandSender, name);
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Exists"));
            }
        } else {
            Text.send(commandSender, playerOnly);
        }
    }

    @SubCommand("settp")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine settp <name>")
    public void settpcmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            RMine m = RealMines.getInstance().getMineManager().get(name);
            if (m != null) {
                m.setTeleport(((Player) commandSender).getLocation());
                m.saveData(RMine.Data.TELEPORT);
                Text.send(commandSender, Language.file().getString("Mines.Teleport-Set").replaceAll("%mine%", m.getDisplayName()));
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, playerOnly);
        }
    }

    @SubCommand("tp")
    @Completion("#mines")
    @Permission("realmines.tp")
    @WrongUsage("&c/mine tp <name>")
    public void tpmine(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            RMine m = RealMines.getInstance().getMineManager().get(name);
            if (m != null) {
                RealMines.getInstance().getMineManager().teleport(((Player) commandSender), m, false);
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, playerOnly);
        }
    }

    @SubCommand("silent")
    @Completion("#mines")
    @Permission("realmines.silent")
    @WrongUsage("&c/mine silent <name>")
    public void silent(final CommandSender commandSender, String name) {
        RMine m = RealMines.getInstance().getMineManager().get(name);
        if (m != null) {
            m.setResetStatus(RMine.Reset.SILENT, !m.isSilent());
            m.saveData(RMine.Data.OPTIONS);

            if (!m.isSilent()) {
                Text.send(commandSender, Language.file().getString("System.Silent-Off").replaceAll("%mine%", name));
            } else {
                Text.send(commandSender, Language.file().getString("System.Silent-On").replaceAll("%mine%", name));
            }
        } else {
            Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
        }
    }

    @SubCommand("silentall")
    @Permission("realmines.silent")
    @WrongUsage("&c/mine silentall <true/false>")
    public void silentall(final CommandSender commandSender, Boolean bol) {
        for (RMine m : RealMines.getInstance().getMineManager().getMines()) {
            m.setResetStatus(RMine.Reset.SILENT, bol);
            m.saveData(RMine.Data.OPTIONS);

            if (!m.isSilent()) {
                Text.send(commandSender, Language.file().getString("System.Silent-Off").replaceAll("%mine%", m.getName()));
            } else {
                Text.send(commandSender, Language.file().getString("System.Silent-On").replaceAll("%mine%", m.getName()));
            }
        }
    }

    @SubCommand("highlight")
    @Completion("#mines")
    @Permission("realmines.highlight")
    @WrongUsage("&c/mine highlight <name>")
    public void highlight(final CommandSender commandSender, String name) {
        if (commandSender instanceof Player) {
            RMine m = RealMines.getInstance().getMineManager().get(name);
            if (m != null) {
                m.setHighlight(!m.isHighlighted());
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, playerOnly);
        }
    }

    @SubCommand("open")
    @Alias("o")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine open <name>")
    public void opencmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            RMine m = RealMines.getInstance().getMineManager().get(name);
            if (m != null) {
                rm.getGUIManager().openMine(m, Bukkit.getPlayer(commandSender.getName()));
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, playerOnly);
        }
    }

    @SubCommand("reset")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine reset <name>")
    public void resetcmd(final CommandSender commandSender, final String name) {
        RMine m = RealMines.getInstance().getMineManager().get(name);
        if (m != null) {
            m.reset();
        } else {
            Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
        }
    }

    @SubCommand("delete")
    @Alias("del")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine delete <name>")
    public void deletecmd(final CommandSender commandSender, final String name) {
        RealMines.getInstance().getMineManager().deleteMine(RealMines.getInstance().getMineManager().get(name));
        Text.send(commandSender, Language.file().getString("System.Mine-Deleted"));
    }

    @SubCommand("clear")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine clear <name>")
    public void clearcmd(final CommandSender commandSender, final String name) {
        RMine m = RealMines.getInstance().getMineManager().get(name);
        if (m != null) {
            m.clear();
            Text.send(commandSender, Language.file().getString("System.Mine-Clear"));
        } else {
            Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
        }
    }

    @SubCommand("setregion")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine setregion <name>")
    public void setregioncmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            RMine m = RealMines.getInstance().getMineManager().get(name);
            if (m != null) {
                RealMines.getInstance().getMineManager().setRegion(name, Bukkit.getPlayer(commandSender.getName()));
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, playerOnly);
        }
    }

}