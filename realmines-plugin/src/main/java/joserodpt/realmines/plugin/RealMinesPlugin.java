package joserodpt.realmines.plugin;

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

import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
import dev.triumphteam.cmd.core.message.MessageKey;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMAchievementsConfig;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.RMMinesOldConfig;
import joserodpt.realmines.api.config.RMPrivateMinesConfig;
import joserodpt.realmines.api.config.RMSQLConfig;
import joserodpt.realmines.api.config.RPMineResetTasksConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.converters.RMSupportedConverters;
import joserodpt.realmines.api.event.RealMinesPluginLoadedEvent;
import joserodpt.realmines.api.managers.PrivateMineTemplate;
import joserodpt.realmines.api.managers.PrivateMinesWorld;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.utils.GUIBuilder;
import joserodpt.realmines.api.utils.PercentageInput;
import joserodpt.realmines.api.utils.PlayerInput;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.command.BaseCommandWA;
import joserodpt.realmines.plugin.command.MineCMD;
import joserodpt.realmines.plugin.command.MineResetTaskCMD;
import joserodpt.realmines.plugin.command.PrivateMineCMD;
import joserodpt.realmines.plugin.events.BlockEvents;
import joserodpt.realmines.plugin.events.PlayerEvents;
import joserodpt.realmines.plugin.events.StatsEvents;
import joserodpt.realmines.plugin.gui.AchievementBoardGUI;
import joserodpt.realmines.plugin.gui.DirectoryBrowserGUI;
import joserodpt.realmines.plugin.gui.LeaderboardGUI;
import joserodpt.realmines.plugin.gui.MaterialPickerGUI;
import joserodpt.realmines.plugin.gui.MineBreakActionsGUI;
import joserodpt.realmines.plugin.gui.MineColorPickerGUI;
import joserodpt.realmines.plugin.gui.MineDepthGUI;
import joserodpt.realmines.plugin.gui.MineFacesGUI;
import joserodpt.realmines.plugin.gui.MineItemsGUI;
import joserodpt.realmines.plugin.gui.MineListGUI;
import joserodpt.realmines.plugin.gui.PrivateMineManageGUI;
import joserodpt.realmines.plugin.gui.PrivateMineTemplateGUI;
import joserodpt.realmines.plugin.gui.PrivateMineTemplatesGUI;
import joserodpt.realmines.plugin.gui.PrivateMinesGUI;
import joserodpt.realmines.plugin.gui.MineResetGUI;
import joserodpt.realmines.plugin.gui.RealMinesGUI;
import joserodpt.realmines.plugin.gui.SettingsGUI;
import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.pluginhook.ExternalPlugin;
import joserodpt.realpermissions.api.pluginhook.ExternalPluginPermission;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RealMinesPlugin extends JavaPlugin {

    static RealMinesPlugin instance;
    private static RealMines realMines;

    public Boolean newUpdate = false;
    private PluginManager pm = Bukkit.getPluginManager();
    private BukkitTask mineHighlight;
    private BukkitTask statsFlush;
    private BukkitTask privateMinesPurge;
    private Economy econ;

    @Override
    public void onEnable() {
        printASCII();

        final long start = System.currentTimeMillis();

        instance = this;
        RMConfig.setup(this);
        realMines = new RealMines(this);
        RealMinesAPI.setInstance(realMines);

        new Metrics(this, 10574);

        this.saveDefaultConfig();
        RMConfig.setup(this);
        RPMineResetTasksConfig.setup(this);
        RMLanguageConfig.setup(this);
        RMSQLConfig.setup(this);
        RMAchievementsConfig.setup(this);
        RMPrivateMinesConfig.setup(this);

        //stats have to be up before the listeners that write to them
        realMines.setupDatabase();
        realMines.getAchievementsManager().loadAchievements();

        //mkdir folder
        final File folder = new File(this.getDataFolder(), "schematics");
        if (!folder.exists()) {
            folder.mkdir();
        }
        final File folder2 = new File(this.getDataFolder(), "mines");
        if (!folder2.exists()) {
            folder2.mkdir();
        }
        RMPrivateMinesConfig.getTemplatesFolder(this);

        RMMinesOldConfig.setup(this);

        Arrays.asList(new PlayerEvents(realMines),
                new BlockEvents(realMines),
                new StatsEvents(realMines),
                AchievementBoardGUI.getListener(),
                LeaderboardGUI.getListener(),
                MineListGUI.getListener(),
                PrivateMinesGUI.getListener(),
                PrivateMineManageGUI.getListener(),
                PrivateMineTemplateGUI.getListener(),
                PrivateMineTemplatesGUI.getListener(),
                GUIBuilder.getListener(),
                MineFacesGUI.getListener(),
                MineDepthGUI.getListener(),
                MaterialPickerGUI.getListener(),
                MineItemsGUI.getListener(),
                MineResetGUI.getListener(),
                MineColorPickerGUI.getListener(),
                MineBreakActionsGUI.getListener(),
                RealMinesGUI.getListener(),
                SettingsGUI.getListener(),
                PercentageInput.getListener(),
                DirectoryBrowserGUI.getListener(),
                PlayerInput.getListener()
        ).forEach(listener -> this.pm.registerEvents(listener, this));

        //vault hook
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                econ = rsp.getProvider();
                if (econ != null) {
                    getLogger().info("Hooked into Vault!");
                }
            }
        }
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);

        //command suggestions
        commandManager.registerSuggestion(SuggestionKey.of("#createsuggestions"),
                (sender, context) -> IntStream.range(0, 100)
                        .mapToObj(i -> "Mine" + i)
                        .collect(Collectors.toList())
        );

        commandManager.registerSuggestion(SuggestionKey.of("#minetasksuggestions"),
                (sender, context) -> IntStream.range(0, 50)
                        .mapToObj(i -> "MineResetTask" + i)
                        .collect(Collectors.toList())
        );

        commandManager.registerSuggestion(SuggestionKey.of("#types"),
                (sender, context) -> Arrays.asList("b", "s", "f", "blocks", "farm", "schem", "schematic")
        );

        commandManager.registerSuggestion(SuggestionKey.of("#converters"),
                (sender, context) -> Arrays.stream(RMSupportedConverters.values())
                        .map(RMSupportedConverters::getSourceName)
                        .collect(Collectors.toList())
        );

        commandManager.registerSuggestion(SuggestionKey.of("#mines"),
                (sender, context) -> realMines.getMineManager().getRegisteredMines()
        );

        commandManager.registerSuggestion(SuggestionKey.of("#players"),
                (sender, context) -> Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList())
        );

        commandManager.registerSuggestion(SuggestionKey.of("#privatetemplates"),
                (sender, context) -> realMines.getPrivateMinesManager().getTemplates().stream()
                        .map(PrivateMineTemplate::getID)
                        .collect(Collectors.toList())
        );

        //  /pmine template <action> [id] [mine]  arrives as one joined argument, so this suggests by
        //  how many words have been typed so far
        commandManager.registerSuggestion(SuggestionKey.of("#privatetemplateargs"),
                (sender, context) -> {
                    switch (context.getArgs().size()) {
                        case 0:
                        case 1:
                            return Arrays.asList("create", "update", "edit", "delete", "list");
                        case 2:
                            return realMines.getPrivateMinesManager().getTemplates().stream()
                                    .map(PrivateMineTemplate::getID)
                                    .collect(Collectors.toList());
                        case 3:
                            return realMines.getMineManager().getRegisteredMines();
                        default:
                            return List.of();
                    }
                }
        );

        commandManager.registerSuggestion(SuggestionKey.of("#privateownedtemplates"),
                (sender, context) -> sender instanceof Player
                        ? realMines.getPrivateMinesManager().getMinesOf(((Player) sender).getUniqueId()).stream()
                        .map(mine -> mine.getPrivateData().getTemplate())
                        .collect(Collectors.toList())
                        : List.of()
        );

        commandManager.registerSuggestion(SuggestionKey.of("#minetasks"),
                (sender, context) -> realMines.getMineResetTasksManager().getRegisteredTasks()
        );

        commandManager.registerSuggestion(SuggestionKey.of("#minecountdowns"),
                (sender, context) -> {
                    RMine mine = realMines.getMineManager().getMine(context.getArgs().get(0));
                    if (mine != null && mine.getMineTimer() != null && mine.getMineTimer().getCountdown() != null) {
                        Integer countdown = mine.getCountdown();
                        if (countdown == null) return List.of();
                        return List.of(countdown.toString());
                    }
                    return List.of();
                }
        );

        //registo de comandos #portugal
        Map<String, BaseCommandWA> commands = new HashMap<>();
        registerCommand("realmines", new MineCMD(realMines), commands, commandManager);
        registerCommand("realminesresettask", new MineResetTaskCMD(realMines), commands, commandManager);
        registerCommand("privatemine", new PrivateMineCMD(realMines), commands, commandManager);

        //command messages
        commandManager.registerMessage(MessageKey.UNKNOWN_COMMAND, (sender, context) -> TranslatableLine.SYSTEM_ERROR_COMMAND.send(sender));
        commandManager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, (sender, context) -> {
            Text.send(sender, commands.get(context.getCommand()).getWrongUsage(context.getSubCommand()));
        });
        commandManager.registerMessage(BukkitMessageKey.NO_PERMISSION, (sender, context) -> TranslatableLine.SYSTEM_ERROR_PERMISSION.send(sender));

        getLogger().info("Loading Mines.");
        realMines.getMineManager().loadMines();
        realMines.getMineResetTasksManager().loadTasks();
        getLogger().info("Loaded " + realMines.getMineManager().getMines().size() + " mines and " + realMines.getMineManager().getSigns().size() + " mine signs.");
        getLogger().info("Loaded " + realMines.getMineResetTasksManager().getTasks().size() + " mine tasks.");

        //after loadTasks, because reset tasks hold direct mine references and purging first would
        //leave those links dangling
        realMines.getPrivateMinesManager().loadTemplates();
        realMines.getPrivateMinesManager().loadInstances();

        //scheduled even when the feature is off, so turning it on with /rm reload doesn't leave
        //time-limited mines running forever until the next restart. purgeExpired no-ops while disabled.
        final long purgeTicks = Math.max(1L, RMPrivateMinesConfig.file() == null ? 60
                : RMPrivateMinesConfig.file().getInt("Private-Mines.Purge-Interval-Seconds", 60)) * 20L;
        this.privateMinesPurge = new BukkitRunnable() {
            @Override
            public void run() {
                realMines.getPrivateMinesManager().purgeExpired();
            }
        }.runTaskTimer(this, purgeTicks, purgeTicks);
        this.mineHighlight = new BukkitRunnable() {
            @Override
            public void run() {
                realMines.getMineManager().getMines().values().forEach(RMine::highlight);
            }

        }.runTaskTimerAsynchronously(this, 0, 10);

        //blocks are counted in memory, this is what actually puts them on disk
        if (realMines.getDatabaseManager() != null) {
            final long flushTicks = Math.max(1L, RMConfig.file().getInt("RealMines.Stats.Flush-Interval-Seconds", 60)) * 20L;
            this.statsFlush = new BukkitRunnable() {
                @Override
                public void run() {
                    //already off the main thread, so both of these can query directly.
                    //flushing first means the leaderboards pick up what was just written.
                    realMines.getDatabaseManager().flushAll(false);
                    realMines.getDatabaseManager().refreshLeaderboards();
                }
            }.runTaskTimerAsynchronously(this, 20L, flushTicks);

            getLogger().info("Loaded " + realMines.getAchievementsManager().getAchievements().size() + " achievements.");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RealMinesPlaceholderAPI(realMines).register();
            getLogger().info("Hooked onto PlaceholderAPI!");
        }

        Bukkit.getPluginManager().callEvent(new RealMinesPluginLoadedEvent());

        if (RMConfig.file().getBoolean("RealMines.useWorldEditForBlockPlacement")) {
            getLogger().info("Using FAWE/WorldEdit for block placement.");
        }

        if (getServer().getPluginManager().getPlugin("RealPermissions") != null) {
            //register RealMines permissions onto RealPermissions
            try {
                RealPermissionsAPI.getInstance().getHooksAPI().addHook(new ExternalPlugin(this.getDescription().getName(), "&fReal&9Mines", this.getDescription().getDescription(), Material.DIAMOND_PICKAXE, Arrays.asList(
                        new ExternalPluginPermission("realmines.admin", "Allow access to the main operator commands of RealMines.", Arrays.asList("rm reload", "rm mines", "rm panel", "rm stoptasks", "rm starttasks", "rm list", "rm create", "rm settp", "rm tp", "rm clear", "rm reset")),
                        new ExternalPluginPermission("realmines.tp.<name>", "Allow permission to teleport to a mine.", Collections.singletonList("rm tp <name>")),
                        new ExternalPluginPermission("realmines.silent", "Allow permission to silence a mine.", Arrays.asList("rm silent", "rm silentall")),
                        new ExternalPluginPermission("realmines.privatemines", "Allow a player to claim and manage their own private mines.", Arrays.asList("pmine", "pmine claim", "pmine tp", "pmine info", "pmine trust", "pmine release")),
                        new ExternalPluginPermission("realmines.privatemines.admin", "Allow managing private mine templates and everyone's private mines.", Arrays.asList("pmine templates", "pmine template", "pmine template edit", "pmine list", "pmine delete", "pmine addsharik")),
                        new ExternalPluginPermission("realmines.privatemines.free", "Claim and renew private mines without being charged."),
                        new ExternalPluginPermission("realmines.reset", "Allow permission to reset all mines."),
                        new ExternalPluginPermission("realmines.update.notify", "Notification of a plugin update to the player."),
                        new ExternalPluginPermission("realmines.achievements", "Allow the player to see their own mining stats and achievements.", Arrays.asList("rm achievements", "rm stats")),
                        new ExternalPluginPermission("realmines.achievements.others", "Allow the player to see somebody else's mining stats and achievements.", Arrays.asList("rm viewachievements <player>", "rm viewstats <player>")),
                        new ExternalPluginPermission("realmines.top", "Allow the player to see the mining leaderboard.", Collections.singletonList("rm top"))
                ), this.getDescription().getVersion()));
            } catch (Exception e) {
                getLogger().warning("Error while trying to register RealMines permissions onto RealPermissions.");
                e.printStackTrace();
            }
        }

        getLogger().info("Finished loading in " + ((System.currentTimeMillis() - start) / 1000F) + " seconds.");
        getLogger().info("<------------------ RealMines vPT ------------------>".replace("PT", this.getDescription().getVersion()));

        new UpdateChecker(this, 73707).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                this.getLogger().info("The plugin is updated to the latest version.");
            } else {
                this.newUpdate = true;
                this.getLogger().warning("There is a new update available! Version: " + version + " https://www.spigotmc.org/resources/73707/");
            }
        });
    }

    /**
     * The generator behind the private mines world, so a world manager can be pointed at
     * {@code generator: RealMines} and rebuild it exactly as RealMines does: completely empty.
     */
    @Override
    public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String id) {
        return PrivateMinesWorld.getGenerator();
    }

    private void registerCommand(String realmines, BaseCommandWA mineCMD, Map<String, BaseCommandWA> commands, BukkitCommandManager<CommandSender> commandManager) {
        commands.put(realmines, mineCMD);
        commandManager.registerCommand(mineCMD);
    }

    private void printASCII() {
        logWithColor("&9   _____           ____  ____");
        logWithColor("&9  | ___ \\         | |  \\/  (_)  &8Version: &9" + this.getDescription().getVersion());
        logWithColor("&9  | |_/ /___  __ _| | .  . |_ _ __   ___  ___");
        logWithColor("&9  |    // _ \\/ _` | | |\\/| | | '_ \\ / _ \\/ __|");
        logWithColor("&9  | |\\ \\  __/ (_| | | |  | | | | | |  __/\\__ \\");
        logWithColor("&9  \\_| \\_\\___|\\__,_|_\\_|  |_/_|_| |_|\\___||___/");
        logWithColor("&9                         &8Made by: &9JoseGamer_PT");
    }

    public void logWithColor(String s) {
        getServer().getConsoleSender().sendMessage("[" + this.getDescription().getName() + "] " + Text.color(s));
    }

    @Override
    public void onDisable() {
        if (this.mineHighlight != null) {
            this.mineHighlight.cancel();
        }
        if (this.statsFlush != null) {
            this.statsFlush.cancel();
        }
        if (this.privateMinesPurge != null) {
            this.privateMinesPurge.cancel();
        }

        //session mines are deliberately left alone here. The scheduler refuses tasks from a disabling
        //plugin, so their regions can't be cleared now, and deleting the files would throw away the only
        //record of where those blocks are. loadInstances() clears and removes them on the next boot.

        //the scheduler refuses async tasks from here on, so this last write has to be synchronous
        if (realMines.getDatabaseManager() != null) {
            realMines.getDatabaseManager().flushAll(false);
            realMines.getDatabaseManager().close();
        }

        realMines.getMineManager().clearMemory();

    }

    public static RealMinesPlugin getPlugin() {
        return instance;
    }

    public Economy getEconomy() {
        return econ;
    }
}
