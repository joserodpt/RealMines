package joserodpt.realmines.plugin.command;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.ArgName;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import dev.triumphteam.cmd.core.annotation.Suggestion;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.task.MineResetTask;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Command(value = "realminesresettask", alias = {"minesresettask", "rmrt"})
public class MineResetTaskCMD extends BaseCommand {

    private final RealMines rm;

    public MineResetTaskCMD(final RealMines rm) {
        this.rm = rm;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        Text.sendList(commandSender,
                Arrays.asList("", "         &9Real&bMines", "         &7Release &a" + rm.getPlugin().getDescription().getVersion(), ""));
    }

    @SubCommand("create")
    @Permission("realmines.admin")
    //@WrongUsage("&c/rmrt create <name> <delay>")
    public void createcmd(final CommandSender commandSender, @Suggestion("#minetasksuggestions") final String name, @ArgName("#int") final Integer delay) {
        if (commandSender instanceof Player) {
            final MineResetTask mrt = this.rm.getMineResetTasksManager().getTask(ChatColor.stripColor(Text.color(name)));
            if (mrt == null) {
                this.rm.getMineResetTasksManager().addTask(ChatColor.stripColor(Text.color(name)), delay);
                TranslatableLine.SYSTEM_MINE_TASK_CREATED.setV1(TranslatableLine.ReplacableVar.TASK.eq(name)).setV2(TranslatableLine.ReplacableVar.DELAY.eq(String.valueOf(delay))).send(commandSender);
            } else {
                TranslatableLine.SYSTEM_MINE_TASK_EXISTS.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("remove")
    @Permission("realmines.admin")
    //@WrongUsage("&c/rmrt create <name> <delay>")
    public void removecmd(final CommandSender commandSender, @Suggestion("#minetasks") final String name) {
        if (commandSender instanceof Player) {
            final MineResetTask mrt = this.rm.getMineResetTasksManager().getTask(name);
            if (mrt != null) {
                this.rm.getMineResetTasksManager().removeTask(mrt);
                TranslatableLine.SYSTEM_REMOVE.setV1(TranslatableLine.ReplacableVar.OBJECT.eq(name)).send(commandSender);
            } else {
                TranslatableLine.SYSTEM_MINE_TASK_DOESNT_EXIST.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("link")
    @Permission("realmines.admin")
    //@WrongUsage("&c/rmrt link <taskname> <mine>")
    public void linkcmd(final CommandSender commandSender, @Suggestion("#minetasks") final String name, @Suggestion("#mines") final String mine) {
        if (commandSender instanceof Player) {
            final MineResetTask mrt = this.rm.getMineResetTasksManager().getTask(name);
            if (mrt != null) {
                final RMine m = this.rm.getMineManager().getMine(mine);
                if (m != null) {
                    mrt.addMine(m);
                    TranslatableLine.SYSTEM_MINE_LINKED.send(commandSender);
                } else {
                    TranslatableLine.SYSTEM_MINE_TASK_DOESNT_EXIST.send(commandSender);
                }
            } else {
                TranslatableLine.SYSTEM_MINE_TASK_DOESNT_EXIST.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("unlink")
    @Permission("realmines.admin")
    //@WrongUsage("&c/rmrt unlink <taskname> <mine>")
    public void unlinkcmd(final CommandSender commandSender, @Suggestion("#minetasks") final String name, @Suggestion("#mines") final String mine) {
        if (commandSender instanceof Player) {
            final MineResetTask mrt = this.rm.getMineResetTasksManager().getTask(name);
            if (mrt != null) {
                final RMine m = this.rm.getMineManager().getMine(mine);
                if (m != null) {
                    mrt.removeMine(m);
                    TranslatableLine.SYSTEM_MINE_UNLINKED.send(commandSender);
                } else {
                    TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
                }
            } else {
                TranslatableLine.SYSTEM_MINE_TASK_DOESNT_EXIST.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

}