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

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMAchievementsConfig;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.RMMinesOldConfig;
import joserodpt.realmines.api.config.RMPrivateMinesConfig;
import joserodpt.realmines.api.config.RMSQLConfig;
import joserodpt.realmines.api.config.RPMineResetTasksConfig;
import joserodpt.realmines.api.event.RealMinesPluginLoadedEvent;
import joserodpt.realmines.api.managers.PrivateMinesWorld;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.types.farm.FarmItem;
import joserodpt.realmines.api.utils.PercentageInput;
import joserodpt.realmines.plugin.command.RMCommandManager;
import joserodpt.realmines.plugin.events.BlockEvents;
import joserodpt.realmines.plugin.events.PlayerEvents;
import joserodpt.realmines.plugin.events.StatsEvents;
import joserodpt.realmines.plugin.gui.AchievementBoardGUI;
import joserodpt.realmines.plugin.gui.DirectoryBrowserGUI;
import joserodpt.realmines.plugin.gui.LeaderboardGUI;
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
import joserodpt.realutils.dialog.Dialogs;
import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.pluginhook.ExternalPlugin;
import joserodpt.realpermissions.api.pluginhook.ExternalPluginPermission;
import joserodpt.realutils.RealUtils;
import joserodpt.realutils.gui.MaterialPickerGUI;
import joserodpt.realutils.input.PlayerInput;
import joserodpt.realutils.item.Items;
import joserodpt.realutils.text.Text;
import joserodpt.realutils.update.UpdateChecker;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;

import static joserodpt.realmines.api.config.TranslatableLine.TranslatableLinePlaceholder.MATERIAL;

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

        //before any GUI opens: this registers the GUIBuilder and MaterialPickerGUI listeners
        RealUtils.setup(this);
        //the old send was the coloured prefix followed by "&f" + the message
        Text.prefix(() -> RMConfig.file().getString("RealMines.Prefix") + "&f");
        //crop blocks aren't items, so show the item they grow from instead of RealUtils' stone
        Items.materialMapper(m -> {
            final Material icon = m.isItem() ? null : FarmItem.findIconForCrop(m);
            return icon != null && icon.isItem() ? icon : m;
        });
        //read each time a picker opens, so a reloaded language.yml applies
        MaterialPickerGUI.labels(() -> {
            final MaterialPickerGUI.Labels labels = new MaterialPickerGUI.Labels();
            labels.nextName = TranslatableLine.GUI_NEXT_PAGE_NAME.get();
            labels.nextLore = RMLanguageConfig.file().getStringList("GUI.Items.Next.Description");
            labels.previousName = TranslatableLine.GUI_PREVIOUS_PAGE_NAME.get();
            labels.previousLore = RMLanguageConfig.file().getStringList("GUI.Items.Back.Description");
            labels.closeName = TranslatableLine.GUI_CLOSE_NAME.get();
            labels.closeLore = RMLanguageConfig.file().getStringList("GUI.Items.Close.Description");
            labels.searchName = TranslatableLine.GUI_SEARCH_ITEM_NAME.get();
            //the search button has always shared the close button's description
            labels.searchLore = RMLanguageConfig.file().getStringList("GUI.Items.Close.Description");
            labels.pickName = m -> TranslatableLine.GUI_PICK_NAME.with(MATERIAL, Text.beautifyMaterialName(m)).get();
            labels.pickLore = RMLanguageConfig.file().getStringList("GUI.Items.Pick.Description");
            return labels;
        });

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
                MineFacesGUI.getListener(),
                MineDepthGUI.getListener(),
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
        //typed input and the settings are asked for in dialogs on servers that have them
        Dialogs.setup(this, () -> RMConfig.file().getBoolean("RealMines.useDialogs", true));
        Dialogs.labels(TranslatableLine.SYSTEM_DIALOG_CONFIRM.get(), TranslatableLine.SYSTEM_DIALOG_CANCEL.get(), null, null, null);
        PlayerInput.setup(this,
                p -> RMLanguageConfig.file().getStringList("System.Type-Input"),
                p -> RMLanguageConfig.file().getStringList("System.Type-Input-Dialog"),
                TranslatableLine.SYSTEM_INPUT_CANCELLED::send,
                TranslatableLine.SYSTEM_ERROR_OCCURRED::send);

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
        //Lamp owns the command tree: the suggestions, the permissions and the language-file errors
        new RMCommandManager(realMines);

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

        //on the main thread: the mine map is a plain HashMap that claims, releases and reloads change there
        }.runTaskTimer(this, 0, 10);

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
                        new ExternalPluginPermission("realmines.privatemines.admin", "Allow managing private mine templates and everyone's private mines.", Arrays.asList("pmine templates", "pmine template list", "pmine template create", "pmine template update", "pmine template edit", "pmine template delete", "pmine list", "pmine delete", "pmine addsharik")),
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
        Dialogs.shutdown();
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

        //waits out pending writes, then does the last one synchronously before closing the connection
        if (realMines.getDatabaseManager() != null) {
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
