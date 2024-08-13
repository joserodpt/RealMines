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
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import com.github.retrooper.packetevents.PacketEvents;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.RMMinesConfig;
import joserodpt.realmines.api.config.RPMineResetTasksConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.converters.RMSupportedConverters;
import joserodpt.realmines.api.event.RealMinesPluginLoadedEvent;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.utils.GUIBuilder;
import joserodpt.realmines.api.utils.PercentageInput;
import joserodpt.realmines.api.utils.PlayerInput;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.command.MineCMD;
import joserodpt.realmines.plugin.command.MineResetTaskCMD;
import joserodpt.realmines.plugin.events.BlockEvents;
import joserodpt.realmines.plugin.events.PlayerEvents;
import joserodpt.realmines.plugin.gui.BlockPickerGUI;
import joserodpt.realmines.plugin.gui.MineBreakActionsGUI;
import joserodpt.realmines.plugin.gui.MineColorPickerGUI;
import joserodpt.realmines.plugin.gui.MineFacesGUI;
import joserodpt.realmines.plugin.gui.MineItensGUI;
import joserodpt.realmines.plugin.gui.MineListGUI;
import joserodpt.realmines.plugin.gui.MineResetGUI;
import joserodpt.realmines.plugin.gui.RealMinesGUI;
import joserodpt.realmines.plugin.gui.SettingsGUI;
import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.pluginhook.ExternalPlugin;
import joserodpt.realpermissions.api.pluginhook.ExternalPluginPermission;
import me.mattstudios.mf.base.CommandManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RealMinesPlugin extends JavaPlugin {

    static RealMinesPlugin instance;
    private static RealMines realMines;

    public Boolean newUpdate = false;
    private PluginManager pm = Bukkit.getPluginManager();
    private BukkitTask mineHighlight;
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

        //mkdir folder
        final File folder = new File(this.getDataFolder(), "schematics");
        if (!folder.exists()) {
            folder.mkdir();
        }
        RMMinesConfig.setup(this);

        this.pm.registerEvents(new PlayerEvents(realMines), this);
        this.pm.registerEvents(new BlockEvents(realMines), this);
        this.pm.registerEvents(MineListGUI.getListener(), this);
        this.pm.registerEvents(GUIBuilder.getListener(), this);
        this.pm.registerEvents(MineFacesGUI.getListener(), this);
        this.pm.registerEvents(BlockPickerGUI.getListener(), this);
        this.pm.registerEvents(MineItensGUI.getListener(), this);
        this.pm.registerEvents(MineResetGUI.getListener(), this);
        this.pm.registerEvents(MineColorPickerGUI.getListener(), this);
        this.pm.registerEvents(MineBreakActionsGUI.getListener(), this);
        this.pm.registerEvents(RealMinesGUI.getListener(), this);
        this.pm.registerEvents(SettingsGUI.getListener(), this);
        this.pm.registerEvents(PercentageInput.getListener(), this);

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

        CommandManager commandManager = new CommandManager(this);

        commandManager.hideTabComplete(true);
        //command suggestions
        commandManager.getCompletionHandler().register("#createsuggestions", input -> IntStream.range(0, 100)
                .mapToObj(i -> "Mine" + i)
                .collect(Collectors.toList()));
        commandManager.getCompletionHandler().register("#minetasksuggestions", input ->
                IntStream.range(0, 50)
                        .mapToObj(i -> "MineResetTask" + i)
                        .collect(Collectors.toList())
        );

        commandManager.getCompletionHandler().register("#converters", input ->
                new ArrayList<>(Arrays.stream(RMSupportedConverters.values()).map(RMSupportedConverters::getSourceName).collect(Collectors.toList()))
        );

        commandManager.getCompletionHandler().register("#mines", input -> realMines.getMineManager().getRegisteredMines());
        commandManager.getCompletionHandler().register("#minetasks", input -> realMines.getMineResetTasksManager().getRegisteredTasks());

        //command messages
        commandManager.getMessageHandler().register("cmd.no.exists", TranslatableLine.SYSTEM_ERROR_COMMAND::send);
        commandManager.getMessageHandler().register("cmd.no.permission", TranslatableLine.SYSTEM_ERROR_PERMISSION::send);
        commandManager.getMessageHandler().register("cmd.wrong.usage", TranslatableLine.SYSTEM_ERROR_USAGE::send);

        //registo de comandos #portugal
        commandManager.register(new MineCMD(realMines));
        commandManager.register(new MineResetTaskCMD(realMines));
        getLogger().info("Loading Mines.");
        realMines.getMineManager().loadMines();
        realMines.getMineResetTasksManager().loadTasks();
        getLogger().info("Loaded " + realMines.getMineManager().getMines().size() + " mines and " + realMines.getMineManager().getSigns().size() + " mine signs.");
        getLogger().info("Loaded " + realMines.getMineResetTasksManager().getTasks().size() + " mine tasks.");
        this.mineHighlight = new BukkitRunnable() {
            @Override
            public void run() {
                realMines.getMineManager().getMines().values().forEach(RMine::highlight);
            }

        }.runTaskTimerAsynchronously(this, 0, 10);

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
                        new ExternalPluginPermission("realmines.reset", "Allow permission to reset all mines."),
                        new ExternalPluginPermission("realmines.update.notify", "Notification of a plugin update to the player.")
                ), this.getDescription().getVersion()));
            } catch (Exception e) {
                getLogger().warning("Error while trying to register RealMines permissions onto RealPermissions.");
                e.printStackTrace();
            }
        }

        if (getServer().getPluginManager().getPlugin("packetevents") != null) {
            getLogger().info("Hooked into packetevents for player input.");
            PacketEvents.getAPI().getEventManager().registerListener(PlayerInput.getPacketListener());
        } else {
            this.pm.registerEvents(PlayerInput.getListener(), this);
            getLogger().info("Hooked into chat for player input. For better reading, consider using PacketEvents lib.");
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
        realMines.getMineManager().clearMemory();

    }

    public static RealMinesPlugin getPlugin() {
        return instance;
    }

    public Economy getEconomy() {
        return econ;
    }
}
