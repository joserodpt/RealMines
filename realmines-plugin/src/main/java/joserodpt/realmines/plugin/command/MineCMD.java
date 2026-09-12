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
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Single;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Command({"realmines", "mine", "rm"})
public class MineCMD {

    private final RealMines rm;

    public MineCMD(final RealMines rm) {
        this.rm = rm;
    }

    @CommandPlaceholder
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

    @Subcommand({"reload", "rl"})
    @CommandPermission("realmines.admin")
    @SuppressWarnings("unused")
    public void reload(final CommandSender commandSender) {
        this.rm.reload();
        TranslatableLine.SYSTEM_RELOADED.send(commandSender);
    }

    @Subcommand({"mines", "p", "panel"})
    @CommandPermission("realmines.admin")
    @SuppressWarnings("unused")
    public void minescmd(final Player p) {
        final MineListGUI v = new MineListGUI(this.rm, p, MineListGUI.MineListSort.DEFAULT);
        v.openInventory(p);
    }

    @Subcommand("stoptasks")
    @CommandPermission("realmines.admin")
    @SuppressWarnings("unused")
    public void stoptaskscmd(final CommandSender commandSender) {
        rm.getMineManager().stopTasks();
        TranslatableLine.SYSTEM_STOPPED_MINE_TASKS.send(commandSender);
    }

    @Subcommand("starttasks")
    @CommandPermission("realmines.admin")
    @SuppressWarnings("unused")
    public void starttaskcmd(final CommandSender commandSender) {
        rm.getMineManager().startTasks();
        TranslatableLine.SYSTEM_STARTED_MINE_TASKS.send(commandSender);
    }

    @Subcommand({"list", "l"})
    @CommandPermission("realmines.admin")
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

    @Subcommand("create")
    @CommandPermission("realmines.admin")
    @Usage("&c/mine create <name> <type>")
    @SuppressWarnings("unused")
    public void createcmd(final Player p, @SuggestFrom(RMSuggestion.NEW_MINE_NAMES) @Single final String name,
                          @SuggestFrom(RMSuggestion.MINE_TYPES) @Single final String type) {
        if (name.isEmpty()) {
            Text.send(p, "&cInvalid mine name.");
            return;
        }
        if (type.isEmpty()) {
            Text.send(p, "&cInvalid mine type.");
            return;
        }

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
            TranslatableLine.SYSTEM_MINE_EXISTS.send(p);
        }
    }

    @Subcommand("settings")
    @CommandPermission("realmines.admin")
    @Usage("&c/mine settings")
    @SuppressWarnings("unused")
    public void settingscmd(final Player p) {
        final SettingsGUI v2 = new SettingsGUI(p, rm);
        v2.openInventory(p);
    }

    @Subcommand("settp")
    @CommandPermission("realmines.admin")
    @Usage("&c/mine settp <name>")
    @SuppressWarnings("unused")
    public void settpcmd(final Player p, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            m.setTeleport(p.getLocation());
            m.saveData(RMine.MineData.TELEPORT);
            TranslatableLine.MINE_TELEPORT_SET.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName())).send(p);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(p);
        }
    }

    /**
     * Where players are put when the ground goes out from under them, which today means a private mine
     * being released or expiring while they stand in it. Stored in config.yml, so it survives a restart.
     */
    @Subcommand("setdefaultlocation")
    @CommandPermission("realmines.admin")
    @Usage("&c/mine setdefaultlocation")
    @SuppressWarnings("unused")
    public void setdefaultlocationcmd(final Player p) {
        RMConfig.setDefaultLocation(p.getLocation());
        TranslatableLine.SYSTEM_DEFAULT_LOCATION_SET.send(p);
    }

    @Subcommand("setcountdown")
    @CommandPermission("realmines.admin")
    @Usage("&c/mine setcountdown <name> <seconds>")
    public void setcountdowncmd(final CommandSender commandSender, @SuggestFrom(RMSuggestion.MINES) @Single final String name,
                                @SuggestFrom(RMSuggestion.MINE_COUNTDOWN) final Integer seconds) {
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

    @Subcommand("resetcountdown")
    @CommandPermission("realmines.admin")
    @Usage("&c/mine resetcountdown <name>")
    public void resetcountdowncmd(final CommandSender commandSender, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
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

    @Subcommand("tp")
    @CommandPermission("realmines.tp")
    @Usage("&c/mine tp <name>")
    @SuppressWarnings("unused")
    public void tpmine(final Player p, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            rm.getMineManager().teleport(p, m, m.isSilent(), true);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(p);
        }
    }

    @Subcommand({"achievements", "ach"})
    @CommandPermission("realmines.achievements")
    @SuppressWarnings("unused")
    public void achievementscmd(final Player p) {
        if (!canShowBoard(p)) {
            return;
        }
        //their own stats are in memory while they are online, so this never waits on the database
        new AchievementBoardGUI(rm, p, p.getName(), rm.getDatabaseManager().getStats(p.getUniqueId())).openInventory(p);
    }

    @Subcommand({"viewachievements", "vach"})
    @CommandPermission("realmines.achievements.others")
    @Usage("&c/mine viewachievements <player>")
    @SuppressWarnings("unused")
    public void viewachievementscmd(final Player p, @SuggestFrom(RMSuggestion.PLAYERS) @Single final String name) {
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

    @Subcommand({"top", "leaderboard", "lb"})
    @CommandPermission("realmines.top")
    @SuppressWarnings("unused")
    public void topcmd(final Player p) {
        if (rm.getDatabaseManager() == null) {
            TranslatableLine.ACHIEVEMENTS_DISABLED.send(p);
            return;
        }

        final LeaderboardGUI lb = new LeaderboardGUI(rm, p);
        lb.openInventory(p);
    }

    @Subcommand("stats")
    @CommandPermission("realmines.achievements")
    @SuppressWarnings("unused")
    public void statscmd(final Player p) {
        if (rm.getDatabaseManager() == null) {
            TranslatableLine.ACHIEVEMENTS_DISABLED.send(p);
            return;
        }

        sendStats(p, p.getName(), rm.getDatabaseManager().getStats(p.getUniqueId()));
    }

    @Subcommand({"viewstats", "vstats"})
    @CommandPermission("realmines.achievements.others")
    @Usage("&c/mine viewstats <player>")
    @SuppressWarnings("unused")
    public void viewstatscmd(final CommandSender commandSender, @SuggestFrom(RMSuggestion.PLAYERS) @Single final String name) {
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

    @Subcommand({"import", "imp", "conv", "convert"})
    @CommandPermission("realmines.import")
    @Usage("&c/mine import <converter>")
    @SuppressWarnings("unused")
    public void importIntoRM(final CommandSender commandSender, @SuggestFrom(RMSuggestion.CONVERTERS) @Single final String name) {
        try {
            RMSupportedConverters conv = Arrays.stream(RMSupportedConverters.values()).filter(c -> c.getSourceName().equalsIgnoreCase(name)).findFirst().orElseThrow(() -> new IllegalArgumentException("Converter not found"));
            Objects.requireNonNull(conv.getConverter(rm)).convert(commandSender);
        } catch (IllegalArgumentException e) {
            TranslatableLine.SYSTEM_NO_CONVERTER_AVAILABLE.send(commandSender);
        }
    }

    @Subcommand({"silent", "s"})
    @CommandPermission("realmines.silent")
    @Usage("&c/mine silent <name>")
    @SuppressWarnings("unused")
    public void silent(final CommandSender commandSender, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
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

    @Subcommand({"silentall", "sa"})
    @CommandPermission("realmines.silent")
    @Usage("&c/mine silentall <true/false>")
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

    @Subcommand("highlight")
    @CommandPermission("realmines.admin")
    @Usage("&c/mine highlight <name>")
    @SuppressWarnings("unused")
    public void highlight(final Player p, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            m.setHighlight(!m.isHighlighted());
            Text.send(p, m.getDisplayName() + " &r&fhighlight: " + (m.isHighlighted() ? "&aON" : "&cOFF"));
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(p);
        }
    }

    @Subcommand("blocks")
    @CommandPermission("realmines.admin")
    @Usage("&c/mine blocks <name>")
    @SuppressWarnings("unused")
    public void blocks(final Player p, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            if (m.getBlockSets().isEmpty()) {
                return;
            }

            final MineItemsGUI v = new MineItemsGUI(rm, p, m);
            v.openInventory(p);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(p);
        }
    }

    @Subcommand({"mine", "m"})
    @CommandPermission("realmines.admin")
    @Usage("&c/mine m <name>")
    @SuppressWarnings("unused")
    public void minecmd(final Player p, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            this.rm.getGUIManager().openMine(m, p);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(p);
        }
    }

    @Subcommand({"reset", "r"})
    @CommandPermission("realmines.reset")
    @Usage("&c/mine reset <name>")
    @SuppressWarnings("unused")
    public void resetcmd(final CommandSender commandSender, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            m.reset(RMine.ResetCause.COMMAND);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @Subcommand({"rename", "rn"})
    @CommandPermission("realmines.admin")
    @Usage("&c/mine rename <name> <new_name>")
    @SuppressWarnings("unused")
    public void renamecmd(final CommandSender commandSender, @SuggestFrom(RMSuggestion.MINES) @Single final String name, @Single final String newName) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            rm.getMineManager().renameMine(m, newName);
            TranslatableLine.SYSTEM_MINE_RENAMED.setV1(TranslatableLine.ReplacableVar.NAME.eq(newName)).send(commandSender);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @Subcommand({"delete", "del"})
    @CommandPermission("realmines.admin")
    @Usage("&c/mine delete <name>")
    @SuppressWarnings("unused")
    public void deletecmd(final CommandSender commandSender, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            rm.getMineManager().deleteMine(m);
            TranslatableLine.SYSTEM_MINE_DELETED.send(commandSender);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @Subcommand({"clear", "c"})
    @CommandPermission("realmines.admin")
    @Usage("&c/mine clear <name>")
    @SuppressWarnings("unused")
    public void clearcmd(final CommandSender commandSender, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            m.clear();
            TranslatableLine.SYSTEM_MINE_CLEAR.send(commandSender);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    @Subcommand("setbounds")
    @CommandPermission("realmines.admin")
    @Usage("&c/mine setbounds <name>")
    @SuppressWarnings("unused")
    public void setboundscmd(final Player p, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            rm.getMineManager().setBounds(m, p);
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(p);
        }
    }

    @Subcommand("freeze")
    @CommandPermission("realmines.admin")
    @Usage("&c/mine freeze <name>")
    @SuppressWarnings("unused")
    public void freezecmd(final CommandSender commandSender, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
        final RMine m = rm.getMineManager().getMine(name);
        if (m != null) {
            m.setFreezed(!m.isFreezed());

            Text.send(commandSender, TranslatableLine.SYSTEM_MINE_FREEZE.get() + (m.isFreezed() ? "&aON" : "&cOFF"));
        } else {
            TranslatableLine.SYSTEM_MINE_DOESNT_EXIST.send(commandSender);
        }
    }

    /*
    @Subcommand("item2config")
    @CommandPermission("realmines.admin")
    @SuppressWarnings("unused")
    public void item2config(final CommandSender commandSender, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
        final Player p = (Player) commandSender;
        RMConfig.file().set("Items." + name, ItemStackSpringer.getItemSerializedJSON(p.getInventory().getItemInMainHand()));
        RMConfig.save();
    }

    @Subcommand("config2item")
    @CommandPermission("realmines.admin")
    @SuppressWarnings("unused")
    public void config2item(final CommandSender commandSender, @SuggestFrom(RMSuggestion.MINES) @Single final String name) {
        final Player p = (Player) commandSender;
        p.getInventory().addItem(ItemStackSpringer.getItemDeSerializedJSON(RMConfig.file().getString("Items." + name)));
    }

    @Subcommand("give")
    @CommandPermission("realmines.admin")
    @Usage("&c/mine ri <item name>")
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
