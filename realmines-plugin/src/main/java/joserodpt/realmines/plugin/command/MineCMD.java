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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */


import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.utils.ItemStackSpringer;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.api.utils.converters.RMSupportedConverters;
import joserodpt.realmines.plugin.RealMines;
import joserodpt.realmines.plugin.gui.MineItensGUI;
import joserodpt.realmines.plugin.gui.MineListGUI;
import joserodpt.realmines.plugin.gui.RealMinesGUI;
import joserodpt.realmines.plugin.gui.SettingsGUI;
import me.mattstudios.mf.annotations.Alias;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.annotations.WrongUsage;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;

@Command("realmines")
@Alias({"mine", "rm"})
public class MineCMD extends CommandBase {

    private final RealMines rm;

    public MineCMD(final RealMines rm) {
        this.rm = rm;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        Text.sendList(commandSender,
                Arrays.asList("         &fReal&9Mines", "         &7Release &a" + rm.getPlugin().getDescription().getVersion()));
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            if (p.hasPermission("realmines.admin") || p.isOp()) {
                final RealMinesGUI rmg = new RealMinesGUI(p, rm);
                rmg.openInventory(p);
            }
        }
    }

    @SubCommand("reload")
    @Alias("rl")
    @Permission("realmines.admin")
    public void reloadcmd(final CommandSender commandSender) {
        this.rm.reload();
        TranslatableLine.SYSTEM_RELOADED.send(commandSender);
    }

    @SubCommand("mines")
    @Alias({"p", "panel"})
    @Permission("realmines.admin")
    public void minescmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            final Player p = (Player) commandSender;
            final MineListGUI v = new MineListGUI(this.rm, p, MineListGUI.MineListSort.DEFAULT);
            v.openInventory(p);
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("stoptasks")
    @Permission("realmines.admin")
    public void stoptaskscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            rm.getMineManager().stopTasks();
            TranslatableLine.SYSTEM_STOPPED_MINE_TASKS.send(commandSender);
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("starttasks")
    @Permission("realmines.admin")
    public void starttaskcmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            rm.getMineManager().startTasks();
            TranslatableLine.SYSTEM_STARTED_MINE_TASKS.send(commandSender);
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("list")
    @Alias("l")
    @Permission("realmines.admin")
    public void listcmd(final CommandSender commandSender) {
        rm.getMineManager().getMines().values().forEach(mine -> Text.send(commandSender, "&7> &f" + mine.getName() + " &r&7(&f" + mine.getDisplayName() + "&r&7)"));
    }

    @SubCommand("create")
    @Completion("#createsuggestions")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine create <name>")
    public void createcmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            final RMine m = rm.getMineManager().getMine(name);
            if (m == null) {
                rm.getGUIManager().openMineChooserType((Player) commandSender, name);
            } else {
                TranslatableLine.SYSTEM_MINE_EXISTS.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("settings")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine settings")
    public void settingscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            final Player p = (Player) commandSender;
            final SettingsGUI v2 = new SettingsGUI(p, rm);
            v2.openInventory(p);
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("settp")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine settp <name>")
    public void settpcmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            final RMine m = rm.getMineManager().getMine(name);
            if (m != null) {
                m.setTeleport(((Player) commandSender).getLocation());
                m.saveData(RMine.Data.TELEPORT);
                TranslatableLine.MINE_TELEPORT_SET.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName())).send(commandSender);
            } else {
                TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("tp")
    @Completion("#mines")
    @Permission("realmines.tp")
    @WrongUsage("&c/mine tp <name>")
    public void tpmine(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            final RMine m = rm.getMineManager().getMine(name);
            if (m != null) {
                rm.getMineManager().teleport(((Player) commandSender), m, m.isSilent(), true);
            } else {
                TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("import")
    @Completion("#converters")
    @Alias({"convert", "imp", "conv"})
    @Permission("realmines.import")
    @WrongUsage("&c/mine import <converter>")
    public void importIntoRM(final CommandSender commandSender, final String name) {
        try {
            RMSupportedConverters conv = Arrays.stream(RMSupportedConverters.values()).filter(c -> c.getSourceName().equalsIgnoreCase(name)).findFirst().orElseThrow(() -> new IllegalArgumentException("Converter not found"));
            Objects.requireNonNull(conv.getConverter(rm)).convert(commandSender);
        } catch (IllegalArgumentException e) {
            TranslatableLine.SYSTEM_NO_CONVERTER_AVAILABLE.send(commandSender);
        }
    }

    @SubCommand("silent")
    @Alias("s")
    @Completion("#mines")
    @Permission("realmines.silent")
    @WrongUsage("&c/mine silent <name>")
    public void silent(final CommandSender commandSender, final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            m.setResetStatus(RMine.Reset.SILENT, !m.isSilent());
            m.saveData(RMine.Data.SETTINGS);

            if (!m.isSilent()) {
                TranslatableLine.SYSTEM_SILENT_OFF.setV1(TranslatableLine.ReplacableVar.MINE.eq(name)).send(commandSender);
            } else {
                TranslatableLine.SYSTEM_SILENT_ON.setV1(TranslatableLine.ReplacableVar.MINE.eq(name)).send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @SubCommand("silentall")
    @Alias("sa")
    @Permission("realmines.silent")
    @WrongUsage("&c/mine silentall <true/false>")
    public void silentall(final CommandSender commandSender, final Boolean bol) {
        for (final RMine m : rm.getMineManager().getMines().values()) {
            m.setResetStatus(RMine.Reset.SILENT, bol);
            m.saveData(RMine.Data.SETTINGS);

            if (!m.isSilent()) {
                TranslatableLine.SYSTEM_SILENT_OFF.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName())).send(commandSender);
            } else {
                TranslatableLine.SYSTEM_SILENT_ON.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName())).send(commandSender);
            }
        }
    }

    @SubCommand("highlight")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine highlight <name>")
    public void highlight(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            final RMine m = rm.getMineManager().getMine(name);
            if (m != null) {
                m.setHighlight(!m.isHighlighted());
            } else {
                TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("blocks")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine blocks <name>")
    public void blocks(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            final RMine m = rm.getMineManager().getMine(name);
            if (m != null) {
                final MineItensGUI v = new MineItensGUI(rm, (Player) commandSender, m);
                v.openInventory((Player) commandSender);
            } else {
                TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("mine")
    @Alias("m")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine m <name>")
    public void minecmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            final RMine m = rm.getMineManager().getMine(name);
            if (m != null) {
                this.rm.getGUIManager().openMine(m, (Player) commandSender);
            } else {
                TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("reset")
    @Alias("r")
    @Completion("#mines")
    @Permission("realmines.reset")
    @WrongUsage("&c/mine reset <name>")
    public void resetcmd(final CommandSender commandSender, final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            m.reset();
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @SubCommand("rename")
    @Alias("rn")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine rename <name> <new_name>")
    public void renamecmd(final CommandSender commandSender, final String name, final String newName) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            rm.getMineManager().renameMine(m, newName);
            TranslatableLine.SYSTEM_MINE_RENAMED.setV1(TranslatableLine.ReplacableVar.NAME.eq(newName)).send(commandSender);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @SubCommand("delete")
    @Alias("del")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine delete <name>")
    public void deletecmd(final CommandSender commandSender, final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            rm.getMineManager().deleteMine(m);
            TranslatableLine.SYSTEM_MINE_DELETED.send(commandSender);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @SubCommand("clear")
    @Alias("c")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine clear <name>")
    public void clearcmd(final CommandSender commandSender, final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            m.clear();
            TranslatableLine.SYSTEM_MINE_CLEAR.send(commandSender);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @SubCommand("setbounds")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine setbounds <name>")
    public void setboundscmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            final RMine m = rm.getMineManager().getMine(name);
            if (m != null) {
                rm.getMineManager().setBounds(m, (Player) commandSender);
            } else {
                TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("freeze")
    @Completion("#mines")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine freeze <name>")
    public void freezecmd(final CommandSender commandSender, final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            m.setFreezed(!m.isFreezed());

            Text.send(commandSender, TranslatableLine.SYSTEM_MINE_FREEZE.get() + (m.isFreezed() ? "&aON" : "&cOFF"));
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @SubCommand("registerItemInConfig")
    @Alias("ri")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine ri <item name>")
    public void registerItemInConfig(final CommandSender commandSender, final String name) {
        final Player p = (Player) commandSender;
        RMConfig.file().set("Items." + name, ItemStackSpringer.getItemSerializedJSON(p.getInventory().getItemInMainHand()));
        RMConfig.save();
    }

}