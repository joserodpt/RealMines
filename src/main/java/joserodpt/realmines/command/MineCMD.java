package joserodpt.realmines.command;

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

import joserodpt.realmines.RealMines;
import joserodpt.realmines.config.Config;
import joserodpt.realmines.config.Language;
import joserodpt.realmines.gui.MineItensGUI;
import joserodpt.realmines.gui.MineListGUI;
import joserodpt.realmines.mine.RMine;
import joserodpt.realmines.util.ItemStackSpringer;
import joserodpt.realmines.util.Text;
import joserodpt.realmines.util.converters.RMConverterBase;
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

@Command("realmines")
@Alias({"mine", "rm"})
public class MineCMD extends CommandBase {

    private final String playerOnly = Language.file().getString("System.Player-Only");
    private final RealMines rm;

    public MineCMD(final RealMines rm) {
        this.rm = rm;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        Text.sendList(commandSender,
                Arrays.asList("         &9Real&bMines", "         &7Release &a" + rm.getDescription().getVersion()));
    }

    @SubCommand("reload")
    @Alias("rl")
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
            final Player p = (Player) commandSender;
            final MineListGUI v = new MineListGUI(this.rm, p);
            v.openInventory(p);
        } else {
            Text.send(commandSender, this.playerOnly);
        }
    }

    @SubCommand("stoptasks")
    @Permission("realmines.admin")
    public void stoptaskscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            rm.getMineManager().stopTasks();
            Text.send(commandSender, Language.file().getString("System.Stopped-Mine-Tasks"));
        } else {
            Text.send(commandSender, this.playerOnly);
        }
    }

    @SubCommand("starttasks")
    @Permission("realmines.admin")
    public void starttaskcmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            rm.getMineManager().startTasks();
            Text.send(commandSender, Language.file().getString("System.Started-Mine-Tasks"));
        } else {
            Text.send(commandSender, this.playerOnly);
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
                Text.send(commandSender, Language.file().getString("System.Mine-Exists"));
            }
        } else {
            Text.send(commandSender, this.playerOnly);
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
                Text.send(commandSender, Language.file().getString("Mines.Teleport-Set").replaceAll("%mine%", m.getDisplayName()));
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, this.playerOnly);
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
                rm.getMineManager().teleport(((Player) commandSender), m, m.isSilent());
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, this.playerOnly);
        }
    }

    @SubCommand("import")
    @Completion("#converters")
    @Permission("realmines.import")
    @WrongUsage("&c/mine import <converter>")
    public void importIntoRM(final CommandSender commandSender, final String name) {
        RMConverterBase conv = rm.getMineManager().getConverters().get(name);

        if (conv == null) {
            Text.send(commandSender, "&cThere is no converter named " + name);
            return;
        }

        conv.convert(commandSender);
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
                Text.send(commandSender, Language.file().getString("System.Silent-Off").replaceAll("%mine%", name));
            } else {
                Text.send(commandSender, Language.file().getString("System.Silent-On").replaceAll("%mine%", name));
            }
        } else {
            Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
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
    public void highlight(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            final RMine m = rm.getMineManager().getMine(name);
            if (m != null) {
                m.setHighlight(!m.isHighlighted());
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, this.playerOnly);
        }
    }

    @SubCommand("blocks")
    @Completion("#mines")
    @Permission("realmines.blocks")
    @WrongUsage("&c/mine blocks <name>")
    public void blocks(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            final RMine m = rm.getMineManager().getMine(name);
            if (m != null) {
                if (m.getType() != RMine.Type.SCHEMATIC) {
                    final MineItensGUI v = new MineItensGUI(rm, (Player) commandSender, m);
                    v.openInventory((Player) commandSender);
                }
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, this.playerOnly);
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
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, this.playerOnly);
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
            Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
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
            Text.send(commandSender, Language.file().getString("System.Mine-Deleted"));
        } else {
            Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
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
            final RMine m = rm.getMineManager().getMine(name);
            if (m != null) {
                rm.getMineManager().setRegion(m, (Player) commandSender);
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, this.playerOnly);
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
            Text.send(commandSender, m.getDisplayName() + " &r&f freeze: " + (m.isFreezed() ? "&aON" : "&cOFF"));
        } else {
            Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
        }
    }

    @SubCommand("registeritem")
    @Alias("ri")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine ri <item name>")
    public void acmd(final CommandSender commandSender, final String name) {
        final Player p = (Player) commandSender;

        Config.file().set("Items." + name, ItemStackSpringer.getItemSerializedJSON(p.getInventory().getItemInMainHand()));
        Config.save();
    }
}