package josegamerpt.realmines.commands;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mines.Mine;
import josegamerpt.realmines.mines.MineResetTask;
import josegamerpt.realmines.utils.Text;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Command("realminesresettask")
@Alias({"minesresettask", "rmrt"})
public class MineResetTaskCMD extends CommandBase {

    String playerOnly = "[RealMines] Only players can run this command.";

    private RealMines rm;

    public MineResetTaskCMD(RealMines rm)
    {
        this.rm = rm;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        Text.sendList(commandSender,
                Arrays.asList("", "         &9Real&bMines", "&7Release &a" + RealMines.getInstance().getDescription().getVersion(), ""));
    }

    @SubCommand("create")
    @Completion({"#minetasksuggestions", "#range:300"})
    @Permission("realmines.admin")
    @WrongUsage("&c/rmrt create <name> <delay>")
    public void createcmd(final CommandSender commandSender, final String name, final Integer delay) {
        if (commandSender instanceof Player) {
            MineResetTask mrt = rm.getMineResetTasksManager().getTask(ChatColor.stripColor(Text.color(name)));
            if (mrt == null) {
                rm.getMineResetTasksManager().addTask(ChatColor.stripColor(Text.color(name)), delay);
                Text.send(commandSender, Language.file().getString("System.Mine-Task-Created").replace("%task%", name).replace("%delay%", delay + ""));
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Task-Exists"));
            }
        } else {
            commandSender.sendMessage(playerOnly);
        }
    }

    @SubCommand("remove")
    @Completion("#minetasks")
    @Permission("realmines.admin")
    @WrongUsage("&c/rmrt create <name> <delay>")
    public void removecmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            MineResetTask mrt = rm.getMineResetTasksManager().getTask(name);
            if (mrt != null) {
                rm.getMineResetTasksManager().removeTask(mrt);
                Text.send(commandSender, Language.file().getString("System.Remove").replace("%object%", name));
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Task-Doesnt-Exist"));
            }
        } else {
            commandSender.sendMessage(playerOnly);
        }
    }

    @SubCommand("link")
    @Completion({"#minetasks", "#mines"})
    @Permission("realmines.admin")
    @WrongUsage("&c/rmrt link <taskname> <mine>")
    public void linkcmd(final CommandSender commandSender, final String name, final String mine) {
        if (commandSender instanceof Player) {
            MineResetTask mrt = rm.getMineResetTasksManager().getTask(name);
            if (mrt != null) {
                Mine m = rm.getMineManager().get(mine);
                if (m != null) {
                    mrt.addMine(m);
                    Text.send(commandSender, Language.file().getString("System.Mine-Linked"));
                } else {
                    Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
                }
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Task-Doesnt-Exist"));
            }
        } else {
            commandSender.sendMessage(playerOnly);
        }
    }

    @SubCommand("unlink")
    @Completion({"#minetasks", "#mines"})
    @Permission("realmines.admin")
    @WrongUsage("&c/rmrt unlink <taskname> <mine>")
    public void unlinkcmd(final CommandSender commandSender, final String name, final String mine) {
        if (commandSender instanceof Player) {
            MineResetTask mrt = rm.getMineResetTasksManager().getTask(name);
            if (mrt != null) {
                Mine m = rm.getMineManager().get(mine);
                if (m != null) {
                    mrt.removeMine(m);
                    Text.send(commandSender, Language.file().getString("System.Mine-Unlinked"));
                } else {
                    Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
                }
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Task-Doesnt-Exist"));
            }
        } else {
            commandSender.sendMessage(playerOnly);
        }
    }

}