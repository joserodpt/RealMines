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
 * @author José Rodrigues © 2019-2026
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.task.MineResetTask;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Single;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Arrays;

import static joserodpt.realmines.api.config.TranslatableLine.TranslatableLinePlaceholder.DELAY;
import static joserodpt.realmines.api.config.TranslatableLine.TranslatableLinePlaceholder.OBJECT;
import static joserodpt.realmines.api.config.TranslatableLine.TranslatableLinePlaceholder.TASK;

@Command({"realminesresettask", "minesresettask", "rmrt"})
public class MineResetTaskCMD {

    private final RealMines rm;

    public MineResetTaskCMD(final RealMines rm) {
        this.rm = rm;
    }

    @CommandPlaceholder
    public void defaultCommand(final CommandSender commandSender) {
        Text.sendList(commandSender,
                Arrays.asList("", "         &9Real&bMines", "         &7Release &a" + rm.getPlugin().getDescription().getVersion(), ""));
    }

    @Subcommand("create")
    @CommandPermission("realmines.admin")
    @Usage("&c/rmrt create <name> <delay>")
    public void createcmd(final Player p, @SuggestFrom(RMSuggestion.NEW_TASK_NAMES) @Single final String name, final Integer delay) {
        //a delay of 0 would become a 1 tick period, resetting every linked mine 20 times a second
        if (delay == null || delay < 1) {
            Text.send(p, "&cThe delay must be at least 1 second.");
            return;
        }
        final MineResetTask mrt = this.rm.getMineResetTasksManager().getTask(ChatColor.stripColor(Text.color(name)));
        if (mrt == null) {
            this.rm.getMineResetTasksManager().addTask(ChatColor.stripColor(Text.color(name)), delay);
            TranslatableLine.SYSTEM_MINE_TASK_CREATED.with(TASK, name).with(DELAY, String.valueOf(delay)).send(p);
        } else {
            TranslatableLine.SYSTEM_MINE_TASK_EXISTS.send(p);
        }
    }

    @Subcommand("remove")
    @CommandPermission("realmines.admin")
    @Usage("&c/rmrt remove <name>")
    public void removecmd(final Player p, @SuggestFrom(RMSuggestion.RESET_TASKS) @Single final String name) {
        final MineResetTask mrt = this.rm.getMineResetTasksManager().getTask(name);
        if (mrt != null) {
            this.rm.getMineResetTasksManager().removeTask(mrt);
            TranslatableLine.SYSTEM_REMOVE.with(OBJECT, name).send(p);
        } else {
            TranslatableLine.SYSTEM_MINE_TASK_DOESNT_EXIST.send(p);
        }
    }

    @Subcommand("link")
    @CommandPermission("realmines.admin")
    @Usage("&c/rmrt link <taskname> <mine>")
    public void linkcmd(final Player p, @SuggestFrom(RMSuggestion.RESET_TASKS) @Single final String name,
                        @SuggestFrom(RMSuggestion.MINES) @Single final String mine) {
        final MineResetTask mrt = this.rm.getMineResetTasksManager().getTask(name);
        if (mrt != null) {
            final RMine m = this.rm.getMineManager().getMine(mine);
            if (m != null) {
                mrt.addMine(m);
                TranslatableLine.SYSTEM_MINE_LINKED.send(p);
            } else {
                TranslatableLine.SYSTEM_MINE_TASK_DOESNT_EXIST.send(p);
            }
        } else {
            TranslatableLine.SYSTEM_MINE_TASK_DOESNT_EXIST.send(p);
        }
    }

    @Subcommand("unlink")
    @CommandPermission("realmines.admin")
    @Usage("&c/rmrt unlink <taskname> <mine>")
    public void unlinkcmd(final Player p, @SuggestFrom(RMSuggestion.RESET_TASKS) @Single final String name,
                          @SuggestFrom(RMSuggestion.MINES) @Single final String mine) {
        final MineResetTask mrt = this.rm.getMineResetTasksManager().getTask(name);
        if (mrt != null) {
            final RMine m = this.rm.getMineManager().getMine(mine);
            if (m != null) {
                mrt.removeMine(m);
                TranslatableLine.SYSTEM_MINE_UNLINKED.send(p);
            } else {
                TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(p);
            }
        } else {
            TranslatableLine.SYSTEM_MINE_TASK_DOESNT_EXIST.send(p);
        }
    }
}
