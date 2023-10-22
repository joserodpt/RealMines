package joserodpt.realmines;

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

import com.google.gson.Gson;
import joserodpt.realmines.command.MineCMD;
import joserodpt.realmines.command.MineResetTaskCMD;
import joserodpt.realmines.config.Config;
import joserodpt.realmines.config.Language;
import joserodpt.realmines.config.MineResetTasks;
import joserodpt.realmines.config.Mines;
import joserodpt.realmines.event.BlockEvents;
import joserodpt.realmines.event.PlayerEvents;
import joserodpt.realmines.gui.GUIManager;
import joserodpt.realmines.gui.BlockPickerGUI;
import joserodpt.realmines.gui.MineItensGUI;
import joserodpt.realmines.gui.MineColorPickerGUI;
import joserodpt.realmines.gui.MineFacesGUI;
import joserodpt.realmines.gui.MineResetGUI;
import joserodpt.realmines.gui.MineListGUI;
import joserodpt.realmines.manager.MineManager;
import joserodpt.realmines.manager.MineResetTasksManager;
import joserodpt.realmines.mine.RMine;
import joserodpt.realmines.util.GUIBuilder;
import joserodpt.realmines.util.PlayerInput;
import joserodpt.realmines.util.Text;
import me.mattstudios.mf.base.CommandManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RealMines extends JavaPlugin {

    static RealMines pl;
    private Random rand = new Random();
    public Boolean newUpdate = false;
    private PluginManager pm = Bukkit.getPluginManager();
    private CommandManager commandManager;
    private BukkitTask mineHighlight;
    private final MineManager mineManager = new MineManager(this);
    private final MineResetTasksManager mineResetTasksManager = new MineResetTasksManager(this);
    private final GUIManager guiManager = new GUIManager(this);
    private Economy econ;

    public Random getRand() {
        return rand;
    }

    public Economy getEconomy() {
        return econ;
    }

    public static RealMines getPlugin() {
        return pl;
    }

    public MineManager getMineManager() {
        return this.mineManager;
    }

    public GUIManager getGUIManager() {
        return this.guiManager;
    }

    public MineResetTasksManager getMineResetTasksManager() {
        return this.mineResetTasksManager;
    }

    public void log(final Level l, final String s) {
        Bukkit.getLogger().log(l, s);
    }

    public void reload() {
        Config.reload();
        Language.reload();
        Mines.reload();
        this.mineManager.unloadMines();
        this.mineManager.loadMines();
        this.log(Level.INFO, "[RealMines] Loaded " + this.mineManager.getMines().size() + " mines and " + this.mineManager.getSigns().size() + " mine signs.");
    }

    public void onEnable() {
        pl = this;
        new Metrics(this, 10574);

        final String star = "<------------------ RealMines PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion());
        this.log(Level.INFO, star);
        this.log(Level.INFO, "Loading Config Files.");
        this.saveDefaultConfig();
        Config.setup(this);
        MineResetTasks.setup(this);
        Language.setup(this);

        //mkdir folder
        final File folder = new File(this.getDataFolder(), "schematics");
        if (!folder.exists()) {
            folder.mkdir();
        }
        Mines.setup(this);

        this.pm.registerEvents(new PlayerEvents(this), this);
        this.pm.registerEvents(new BlockEvents(this), this);
        this.pm.registerEvents(MineListGUI.getListener(), this);
        this.pm.registerEvents(GUIBuilder.getListener(), this);
        this.pm.registerEvents(MineFacesGUI.getListener(), this);
        this.pm.registerEvents(BlockPickerGUI.getListener(), this);
        this.pm.registerEvents(MineItensGUI.getListener(), this);
        this.pm.registerEvents(PlayerInput.getListener(), this);
        this.pm.registerEvents(MineResetGUI.getListener(), this);
        this.pm.registerEvents(MineColorPickerGUI.getListener(), this);

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

        this.commandManager = new CommandManager(this);

        this.commandManager.hideTabComplete(true);
        //command suggestions
        this.commandManager.getCompletionHandler().register("#createsuggestions", input -> IntStream.range(0, 100)
                .mapToObj(i -> "Mine" + i)
                .collect(Collectors.toList()));
        this.commandManager.getCompletionHandler().register("#minetasksuggestions", input ->
                IntStream.range(0, 50)
                        .mapToObj(i -> "MineResetTask" + i)
                        .collect(Collectors.toList())
        );

        this.commandManager.getCompletionHandler().register("#converters", input ->
                new ArrayList<>(this.mineManager.getConverters().keySet())
        );

        this.commandManager.getCompletionHandler().register("#mines", input -> this.mineManager.getRegisteredMines());
        this.commandManager.getCompletionHandler().register("#minetasks", input -> this.mineResetTasksManager.getRegisteredTasks());

        //command messages
        this.commandManager.getMessageHandler().register("cmd.no.exists", sender -> Text.send(sender, Language.file().getString("System.Error-Command")));
        this.commandManager.getMessageHandler().register("cmd.no.permission", sender -> Text.send(sender, Language.file().getString("System.Error-Permission")));
        this.commandManager.getMessageHandler().register("cmd.wrong.usage", sender -> Text.send(sender, Language.file().getString("System.Error-Usage")));

        //registo de comandos #portugal
        this.commandManager.register(new MineCMD(this));
        this.commandManager.register(new MineResetTaskCMD(this));
        this.log(Level.INFO, "Loading Mines.");
        this.mineManager.loadMines();
        this.mineResetTasksManager.loadTasks();
        this.log(Level.INFO, "Loaded " + this.mineManager.getMines().size() + " mines and " + this.mineManager.getSigns().size() + " mine signs.");
        this.log(Level.INFO, "Loaded " + this.mineResetTasksManager.getTasks().size() + " mine tasks.");
        this.mineHighlight = new BukkitRunnable() {
            @Override
            public void run() {
                mineManager.getMines().values().forEach(RMine::highlight);
            }

        }.runTaskTimerAsynchronously(this, 0, 10);


        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RealMinesPlaceholderAPI(this).register();
            this.log(Level.INFO, "Hooked onto PlaceholderAPI!");
        }

        this.log(Level.INFO, "Plugin has been loaded.");
        this.log(Level.INFO, "Author: JoseGamer_PT | " + this.getDescription().getWebsite());
        this.log(Level.INFO, star);

        new UpdateChecker(this, 73707).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                this.getLogger().info("The plugin is updated to the latest version.");
            } else {
                this.newUpdate = true;
                this.getLogger().info("There is a new update available! Version: " + version + " https://www.spigotmc.org/resources/realmines-1-14-to-1-20-1.73707/");
            }
        });
    }

    public void onDisable() {
        if (this.mineHighlight != null) {
            this.mineHighlight.cancel();
        }
        this.getMineManager().clearMemory();
    }

    public boolean hasNewUpdate() {
        return this.newUpdate;
    }
}
