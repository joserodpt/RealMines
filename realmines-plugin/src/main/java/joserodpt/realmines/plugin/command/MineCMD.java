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

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import dev.triumphteam.cmd.core.annotation.Suggestion;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.converters.RMSupportedConverters;
import joserodpt.realmines.api.database.RMPlayerData;
import joserodpt.realmines.api.database.RMPlayerStats;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
import joserodpt.realmines.plugin.gui.AchievementBoardGUI;
import joserodpt.realmines.plugin.gui.LeaderboardGUI;
import joserodpt.realmines.plugin.gui.MineItemsGUI;
import joserodpt.realmines.plugin.gui.MineListGUI;
import joserodpt.realmines.plugin.gui.RealMinesGUI;
import joserodpt.realmines.plugin.gui.SettingsGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Command(value = "realmines", alias = {"mine", "rm"})
public class MineCMD extends BaseCommandWA {

    private final RealMines rm;

    public MineCMD(final RealMines rm) {
        this.rm = rm;
    }

    @Default
    @SuppressWarnings("unused")
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

    @SubCommand(value = "reload", alias = "rl")
    @Permission("realmines.admin")
    @SuppressWarnings("unused")
    public void reload(final CommandSender commandSender) {
        this.rm.reload();
        TranslatableLine.SYSTEM_RELOADED.send(commandSender);
    }

    @SubCommand(value = "mines", alias = {"p", "panel"})
    @Permission("realmines.admin")
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public void stoptaskscmd(final CommandSender commandSender) {
        rm.getMineManager().stopTasks();
        TranslatableLine.SYSTEM_STOPPED_MINE_TASKS.send(commandSender);
    }

    @SubCommand("starttasks")
    @Permission("realmines.admin")
    @SuppressWarnings("unused")
    public void starttaskcmd(final CommandSender commandSender) {
        rm.getMineManager().startTasks();
        TranslatableLine.SYSTEM_STARTED_MINE_TASKS.send(commandSender);
    }

    @SubCommand(value = "list", alias = "l")
    @Permission("realmines.admin")
    @SuppressWarnings("unused")
    public void listcmd(final CommandSender commandSender) {
        rm.getMineManager().getMines().values().stream()
                .filter(mine -> !mine.isPrivate())
                .forEach(mine -> Text.send(commandSender, "&f" + mine.getName() + " &r&7(&f" + mine.getDisplayName() + "&r&7)"));

        //listing one line per player's private mine would drown this out, so just point at /pmine list
        final long privateMines = rm.getMineManager().getMines().values().stream().filter(RMine::isPrivate).count();
        if (privateMines > 0) {
            Text.send(commandSender, "&7" + privateMines + " private mine(s) &8- &7/pmine list");
        }
    }

    @SubCommand("create")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine create <name> <type>")
    @SuppressWarnings("unused")
    public void createcmd(final CommandSender commandSender, @Suggestion("#createsuggestions") final String name, @Suggestion("#types") final String type) {
        if (name == null || name.isEmpty()) {
            Text.send(commandSender, "&cInvalid mine name.");
            return;
        }
        if (type == null || type.isEmpty()) {
            Text.send(commandSender, "&cInvalid mine type.");
            return;
        }

        if (commandSender instanceof Player p) {
            final RMine m = rm.getMineManager().getMine(name);
            if (m == null) {
                switch (type) {
                    case "b":
                    case "blocks":
                        rm.getMineManager().createMine(p, name);
                        break;
                    case "f":
                    case "farm":
                        rm.getMineManager().createFarmMine(p, name);
                        break;
                    case "s":
                    case "schem":
                    case "schematic":
                        rm.getMineManager().createSchematicMine(p, name);
                        break;
                    default:
                        Text.send(p, "&cInvalid mine type.");
                        break;
                }
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
    @SuppressWarnings("unused")
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
    @Permission("realmines.admin")
    @WrongUsage("&c/mine settp <name>")
    @SuppressWarnings("unused")
    public void settpcmd(final CommandSender commandSender, @Suggestion("#mines") final String name) {
        if (commandSender instanceof Player) {
            final RMine m = rm.getMineManager().getMine(name);
            if (m != null) {
                m.setTeleport(((Player) commandSender).getLocation());
                m.saveData(RMine.MineData.TELEPORT);
                TranslatableLine.MINE_TELEPORT_SET.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName())).send(commandSender);
            } else {
                TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    /**
     * Where players are put when the ground goes out from under them, which today means a private mine
     * being released or expiring while they stand in it. Stored in config.yml, so it survives a restart.
     */
    @SubCommand("setdefaultlocation")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine setdefaultlocation")
    @SuppressWarnings("unused")
    public void setdefaultlocationcmd(final CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
            return;
        }

        RMConfig.setDefaultLocation(((Player) commandSender).getLocation());
        TranslatableLine.SYSTEM_DEFAULT_LOCATION_SET.send(commandSender);
    }

    @SubCommand("setcountdown")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine setcountdown <name> <seconds>")
    public void setcountdowncmd(final CommandSender commandSender, @Suggestion("#mines") final String name, @Suggestion("#minecountdowns") final Integer seconds) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            boolean success = m.setCountdown(seconds, true);
            if (success) {
                TranslatableLine.MINE_COUNTDOWN_SET.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName())).setV2(TranslatableLine.ReplacableVar.TIME.eq(String.valueOf(seconds))).send(commandSender);
            } else {
                TranslatableLine.MINE_COUNTDOWN_SET_UNSUCCESSFUL.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName())).send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @SubCommand("resetcountdown")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine resetcountdown <name>")
    public void resetcountdowncmd(final CommandSender commandSender, @Suggestion("#mines") final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            boolean success = m.resetCountdown(true);
            if (success) {
                TranslatableLine line = TranslatableLine.MINE_COUNTDOWN_SET.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName()));
                Integer countdown = m.getCountdown();
                if (countdown != null) {
                    line.setV2(TranslatableLine.ReplacableVar.TIME.eq(String.valueOf(countdown)));
                }
                line.send(commandSender);
            } else {
                TranslatableLine.MINE_COUNTDOWN_SET_UNSUCCESSFUL.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName())).send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @SubCommand("tp")
    @Permission("realmines.tp")
    @WrongUsage("&c/mine tp <name>")
    @SuppressWarnings("unused")
    public void tpmine(final CommandSender commandSender, @Suggestion("#mines") final String name) {
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

    @SubCommand(value = "achievements", alias = "ach")
    @Permission("realmines.achievements")
    @SuppressWarnings("unused")
    public void achievementscmd(final CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
            return;
        }

        final Player p = (Player) commandSender;
        if (!canShowBoard(p)) {
            return;
        }
        //their own stats are in memory while they are online, so this never waits on the database
        new AchievementBoardGUI(rm, p, p.getName(), rm.getDatabaseManager().getStats(p.getUniqueId())).openInventory(p);
    }

    @SubCommand(value = "viewachievements", alias = "vach")
    @Permission("realmines.achievements.others")
    @WrongUsage("&c/mine viewachievements <player>")
    @SuppressWarnings("unused")
    public void viewachievementscmd(final CommandSender commandSender, @Suggestion("#players") final String name) {
        if (!(commandSender instanceof Player)) {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
            return;
        }

        final Player p = (Player) commandSender;
        if (!canShowBoard(p)) {
            return;
        }

        //an offline player's rows have to be read off the database, so the GUI opens once they arrive
        findPlayer(p, name, data -> rm.getDatabaseManager().loadStats(data.getUUID(),
                stats -> new AchievementBoardGUI(rm, p, data.getName(), stats).openInventory(p)));
    }

    private boolean canShowBoard(final Player p) {
        if (rm.getDatabaseManager() == null) {
            TranslatableLine.ACHIEVEMENTS_DISABLED.send(p);
            return false;
        }
        if (rm.getAchievementsManager().getAchievements().isEmpty()) {
            TranslatableLine.ACHIEVEMENTS_NONE_CONFIGURED.send(p);
            return false;
        }
        return true;
    }

    @SubCommand(value = "top", alias = {"leaderboard", "lb"})
    @Permission("realmines.top")
    @SuppressWarnings("unused")
    public void topcmd(final CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
            return;
        }
        if (rm.getDatabaseManager() == null) {
            TranslatableLine.ACHIEVEMENTS_DISABLED.send(commandSender);
            return;
        }

        final LeaderboardGUI lb = new LeaderboardGUI(rm, (Player) commandSender);
        lb.openInventory((Player) commandSender);
    }

    @SubCommand("stats")
    @Permission("realmines.achievements")
    @SuppressWarnings("unused")
    public void statscmd(final CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
            return;
        }
        if (rm.getDatabaseManager() == null) {
            TranslatableLine.ACHIEVEMENTS_DISABLED.send(commandSender);
            return;
        }

        final Player p = (Player) commandSender;
        sendStats(p, p.getName(), rm.getDatabaseManager().getStats(p.getUniqueId()));
    }

    @SubCommand(value = "viewstats", alias = "vstats")
    @Permission("realmines.achievements.others")
    @WrongUsage("&c/mine viewstats <player>")
    @SuppressWarnings("unused")
    public void viewstatscmd(final CommandSender commandSender, @Suggestion("#players") final String name) {
        if (rm.getDatabaseManager() == null) {
            TranslatableLine.ACHIEVEMENTS_DISABLED.send(commandSender);
            return;
        }

        findPlayer(commandSender, name, data -> rm.getDatabaseManager().loadStats(data.getUUID(),
                stats -> sendStats(commandSender, data.getName(), stats)));
    }

    private void sendStats(final CommandSender to, final String targetName, final RMPlayerStats stats) {
        if (stats == null || stats.getTotalBlocksMined() <= 0) {
            Text.send(to, TranslatableLine.STATS_NO_DATA
                    .setV1(TranslatableLine.ReplacableVar.NAME.eq(targetName)).get());
            return;
        }

        Text.send(to, TranslatableLine.STATS_HEADER
                .setV1(TranslatableLine.ReplacableVar.NAME.eq(targetName)).get());
        Text.send(to, TranslatableLine.STATS_TOTAL_MINED
                .setV1(TranslatableLine.ReplacableVar.VALUE.eq(Text.formatNumber(stats.getTotalBlocksMined()))).get());

        final int total = rm.getAchievementsManager().getAchievements().size();
        final int unlocked = rm.getAchievementsManager().getUnlockedCount(stats);
        Text.send(to, TranslatableLine.STATS_ACHIEVEMENTS
                .setV1(TranslatableLine.ReplacableVar.VALUE.eq(unlocked + "/" + total))
                .setV2(TranslatableLine.ReplacableVar.PERCENTAGE.eq(
                        total == 0 ? "0" : String.valueOf(Math.round(unlocked * 100D / total)))).get());

        Material best = null;
        long bestAmount = 0;
        for (final Map.Entry<Material, Long> entry : stats.getBlocksMined().entrySet()) {
            if (entry.getValue() > bestAmount) {
                bestAmount = entry.getValue();
                best = entry.getKey();
            }
        }
        if (best != null) {
            Text.send(to, TranslatableLine.STATS_TOP_MATERIAL
                    .setV1(TranslatableLine.ReplacableVar.MATERIAL.eq(Text.beautifyMaterialName(best)))
                    .setV2(TranslatableLine.ReplacableVar.VALUE.eq(Text.formatNumber(bestAmount))).get());
        }
    }

    /**
     * Resolves a player by name without ever asking Mojang: online players first, then whoever has
     * mining stats saved. The callback only runs if somebody was found.
     */
    private void findPlayer(final CommandSender to, final String name, final Consumer<RMPlayerData> callback) {
        rm.getDatabaseManager().findPlayer(name, data -> {
            if (data == null) {
                Text.send(to, TranslatableLine.STATS_PLAYER_NOT_FOUND
                        .setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).get());
                return;
            }
            callback.accept(data);
        });
    }

    @SubCommand(value = "import", alias = {"imp", "conv", "convert"})
    @Permission("realmines.import")
    @WrongUsage("&c/mine import <converter>")
    @SuppressWarnings("unused")
    public void importIntoRM(final CommandSender commandSender, @Suggestion("#converters") final String name) {
        try {
            RMSupportedConverters conv = Arrays.stream(RMSupportedConverters.values()).filter(c -> c.getSourceName().equalsIgnoreCase(name)).findFirst().orElseThrow(() -> new IllegalArgumentException("Converter not found"));
            Objects.requireNonNull(conv.getConverter(rm)).convert(commandSender);
        } catch (IllegalArgumentException e) {
            TranslatableLine.SYSTEM_NO_CONVERTER_AVAILABLE.send(commandSender);
        }
    }

    @SubCommand(value = "silent", alias = "s")
    @Permission("realmines.silent")
    @WrongUsage("&c/mine silent <name>")
    @SuppressWarnings("unused")
    public void silent(final CommandSender commandSender, @Suggestion("#mines") final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            m.setSilent(!m.isSilent());

            if (!m.isSilent()) {
                TranslatableLine.SYSTEM_SILENT_OFF.setV1(TranslatableLine.ReplacableVar.MINE.eq(name)).send(commandSender);
            } else {
                TranslatableLine.SYSTEM_SILENT_ON.setV1(TranslatableLine.ReplacableVar.MINE.eq(name)).send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @SubCommand(value = "silentall", alias = "sa")
    @Permission("realmines.silent")
    @WrongUsage("&c/mine silentall <true/false>")
    @SuppressWarnings("unused")
    public void silentall(final CommandSender commandSender, final Boolean bol) {
        for (final RMine m : rm.getMineManager().getMines().values()) {
            //private mines belong to players and already announce only to their owner, so a server wide
            //toggle shouldn't touch them - it would also rewrite a file and restart a timer for each one
            if (m.isPrivate()) {
                continue;
            }
            m.setSilent(bol);

            if (!m.isSilent()) {
                TranslatableLine.SYSTEM_SILENT_OFF.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName())).send(commandSender);
            } else {
                TranslatableLine.SYSTEM_SILENT_ON.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName())).send(commandSender);
            }
        }
    }

    @SubCommand("highlight")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine highlight <name>")
    @SuppressWarnings("unused")
    public void highlight(final CommandSender commandSender, @Suggestion("#mines") final String name) {
        if (commandSender instanceof Player) {
            final RMine m = rm.getMineManager().getMine(name);
            if (m != null) {
                m.setHighlight(!m.isHighlighted());
                Text.send(commandSender, m.getDisplayName() + " &r&fhighlight: " + (m.isHighlighted() ? "&aON" : "&cOFF"));
            } else {
                TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand("blocks")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine blocks <name>")
    @SuppressWarnings("unused")
    public void blocks(final CommandSender commandSender, @Suggestion("#mines") final String name) {
        if (commandSender instanceof Player) {
            final RMine m = rm.getMineManager().getMine(name);
            if (m != null) {
                if (m.getBlockSets().isEmpty()) {
                    return;
                }

                final MineItemsGUI v = new MineItemsGUI(rm, (Player) commandSender, m);
                v.openInventory((Player) commandSender);
            } else {
                TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @SubCommand(value = "mine", alias = "m")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine m <name>")
    @SuppressWarnings("unused")
    public void minecmd(final CommandSender commandSender, @Suggestion("#mines") final String name) {
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

    @SubCommand(value = "reset", alias = "r")
    @Permission("realmines.reset")
    @WrongUsage("&c/mine reset <name>")
    @SuppressWarnings("unused")
    public void resetcmd(final CommandSender commandSender, @Suggestion("#mines") final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            m.reset(RMine.ResetCause.COMMAND);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @SubCommand(value = "rename", alias = "rn")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine rename <name> <new_name>")
    @SuppressWarnings("unused")
    public void renamecmd(final CommandSender commandSender, @Suggestion("#mines") final String name, final String newName) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            rm.getMineManager().renameMine(m, newName);
            TranslatableLine.SYSTEM_MINE_RENAMED.setV1(TranslatableLine.ReplacableVar.NAME.eq(newName)).send(commandSender);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @SubCommand(value = "delete", alias = "del")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine delete <name>")
    @SuppressWarnings("unused")
    public void deletecmd(final CommandSender commandSender, @Suggestion("#mines") final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            rm.getMineManager().deleteMine(m);
            TranslatableLine.SYSTEM_MINE_DELETED.send(commandSender);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @SubCommand(value = "clear", alias = "c")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine clear <name>")
    @SuppressWarnings("unused")
    public void clearcmd(final CommandSender commandSender, @Suggestion("#mines") final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            m.clear();
            TranslatableLine.SYSTEM_MINE_CLEAR.send(commandSender);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @SubCommand("setbounds")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine setbounds <name>")
    @SuppressWarnings("unused")
    public void setboundscmd(final CommandSender commandSender, @Suggestion("#mines") final String name) {
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
    @Permission("realmines.admin")
    @WrongUsage("&c/mine freeze <name>")
    @SuppressWarnings("unused")
    public void freezecmd(final CommandSender commandSender, @Suggestion("#mines") final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            m.setFreezed(!m.isFreezed());

            Text.send(commandSender, TranslatableLine.SYSTEM_MINE_FREEZE.get() + (m.isFreezed() ? "&aON" : "&cOFF"));
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    /*
    @SubCommand("item2config")
    @Permission("realmines.admin")
    @SuppressWarnings("unused")
    public void item2config(final CommandSender commandSender, @Suggestion("#mines") final String name) {
        final Player p = (Player) commandSender;
        RMConfig.file().set("Items." + name, ItemStackSpringer.getItemSerializedJSON(p.getInventory().getItemInMainHand()));
        RMConfig.save();
    }

    @SubCommand("config2item")
    @Permission("realmines.admin")
    @SuppressWarnings("unused")
    public void config2item(final CommandSender commandSender, @Suggestion("#mines") final String name) {
        final Player p = (Player) commandSender;
        p.getInventory().addItem(ItemStackSpringer.getItemDeSerializedJSON(RMConfig.file().getString("Items." + name)));
    }

    @SubCommand("give")
    @Permission("realmines.admin")
    @WrongUsage("&c/mine ri <item name>")
    @SuppressWarnings("unused")
    public void giveItems(final CommandSender commandSender) {
        final Player p = (Player) commandSender;

        ItemStack customSword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = customSword.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "Legendary Blade");
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(
                    UUID.randomUUID(), "extra_damage", 10.0, AttributeModifier.Operation.ADD_NUMBER));
            meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(
                    UUID.randomUUID(), "extra_speed", 0.05, AttributeModifier.Operation.ADD_SCALAR));
            customSword.setItemMeta(meta);
        }

        p.getInventory().addItem(customSword);

        // Second pickaxe with hidden Efficiency enchantment
        ItemStack hiddenPickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta hiddenMeta = hiddenPickaxe.getItemMeta();
        if (hiddenMeta != null) {
            hiddenMeta.addEnchant(Enchantment.LUCK, 3, true);
            hiddenMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            hiddenMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
            hiddenMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            hiddenPickaxe.setItemMeta(hiddenMeta);
        }

        // Give the player the items
        p.getInventory().addItem(hiddenPickaxe);
    }
     */
}