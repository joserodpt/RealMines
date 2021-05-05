package josegamerpt.realmines;

import josegamerpt.realmines.commands.MineCMD;
import josegamerpt.realmines.commands.MineResetTaskCMD;
import josegamerpt.realmines.config.*;
import josegamerpt.realmines.gui.GUIManager;
import josegamerpt.realmines.managers.MineManager;
import josegamerpt.realmines.managers.MineResetTasksManager;
import josegamerpt.realmines.mines.Mine;
import josegamerpt.realmines.events.BlockEvents;
import josegamerpt.realmines.gui.*;
import josegamerpt.realmines.utils.GUIBuilder;
import josegamerpt.realmines.utils.PlayerInput;
import josegamerpt.realmines.utils.Text;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class RealMines extends JavaPlugin {

    public Boolean newUpdate = false;

    static RealMines pl;
    PluginManager pm = Bukkit.getPluginManager();
    CommandManager commandManager;
    private BukkitTask mineHighlight;
    public Random r = new Random();

    private MineManager mineManager = new MineManager();
    private MineResetTasksManager mineResetTasksManager = new MineResetTasksManager(this);
    private GUIManager guiManager = new GUIManager(this);

    public MineManager getMineManager()
    {
        return this.mineManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public MineResetTasksManager getMineResetTasksManager()
    {
        return this.mineResetTasksManager;
    }

    public void log(Level l, String s) {
        Bukkit.getLogger().log(l, s);
    }

    public String getPrefix() {
        return Text.color(Config.file().getString("RealMines.Prefix"));
    }

    public static RealMines getInstance() {
        return pl;
    }

    public void reload() {
        Config.reload();
        Mines.reload();
        this.mineManager.unloadMines();
        this.mineManager.loadMines();
        log(Level.INFO, "[RealMines] Loaded " + this.mineManager.getMines().size() + " mines and " + this.mineManager.getSigns().size() + " mine signs.");
    }

    static void failMessage(String reason) {
        Arrays.asList("Failed to load RealScoreboard.", reason,
                "If you think this is a bug, please contact JoseGamer_PT.", "https://www.spigotmc.org/members/josegamer_pt.40267/").forEach(s -> Bukkit.getLogger().log(Level.WARNING, s));
    }

    public void onEnable() {
        pl = this;

        String star = "<------------------ RealMines PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion());
        log(Level.INFO, star);
        log(Level.INFO, "Loading Config Files.");
        saveDefaultConfig();
        Config.setup(this);
        MineResetTasks.setup(this);
        Language.setup(this);

        new Metrics(this, 10574);

        log(Level.INFO, "Your config version is: " + Configer.getConfigVersion());
        Configer.updateConfig();

        if (Configer.checkForErrors()) {
            failMessage("There are some problems with your config: " + Configer.getErrors() + "\nPlease check this errors. Plugin is disabled due to config errors.");
            log(Level.INFO, star);
            disablePlugin();
        } else {
            Mines.setup(this);

            log(Level.INFO, "Registering Events.");
            pm.registerEvents(new BlockEvents(this), this);
            pm.registerEvents(MineViewer.getListener(), this);
            pm.registerEvents(GUIBuilder.getListener(), this);
            pm.registerEvents(MineFaces.getListener(), this);
            pm.registerEvents(MaterialPicker.getListener(), this);
            pm.registerEvents(MineBlocksViewer.getListener(), this);
            pm.registerEvents(PlayerInput.getListener(), this);
            pm.registerEvents(MineResetMenu.getListener(), this);
            pm.registerEvents(MineColorPicker.getListener(), this);

            commandManager = new CommandManager(this);
            commandManager.hideTabComplete(true);
            //command suggestions
            commandManager.getCompletionHandler().register("#createsuggestions", input -> {
                List<String> sugests = new ArrayList<>();

                for (int i = 0; i < 100; i++) {
                    sugests.add("Mine" + i);
                }

                return sugests;
            });
            commandManager.getCompletionHandler().register("#minetasksuggestions", input -> {
                List<String> sugests = new ArrayList<>();

                for (int i = 0; i < 50; i++) {
                    sugests.add("MineResetTask" + i);
                }

                return sugests;
            });
            commandManager.getCompletionHandler().register("#mines", input -> this.mineManager.getRegisteredMines());
            commandManager.getCompletionHandler().register("#minetasks", input -> this.mineResetTasksManager.getRegisteredTasks());

            //command messages
            commandManager.getMessageHandler().register("cmd.no.exists", sender -> sender.sendMessage(this.getPrefix() + Text.color("&cThe command you're trying to run doesn't exist!")));
            commandManager.getMessageHandler().register("cmd.no.permission", sender -> sender.sendMessage(this.getPrefix() + Text.color("&fYou &cdon't &fhave permission to execute this command!")));
            commandManager.getMessageHandler().register("cmd.wrong.usage", sender -> sender.sendMessage(this.getPrefix() + Text.color("&cWrong usage for the command!")));

            //registo de comandos #portugal
            commandManager.register(new MineCMD(this));
            commandManager.register(new MineResetTaskCMD(this));
            log(Level.INFO, "Loading Mines.");
            this.mineManager.loadMines();
            this.mineResetTasksManager.loadTasks();
            log(Level.INFO, "Loaded " + this.mineManager.getMines().size() + " mines and " + this.mineManager.getSigns().size() + " mine signs.");
            log(Level.INFO, "Loaded " + this.mineResetTasksManager.getTasks().size() + " mine tasks.");
            this.mineHighlight = new BukkitRunnable() {
                @Override
                public void run() {
                    mineManager.getMines().forEach(Mine::highlight);
                }

            }.runTaskTimerAsynchronously(this, 0, 10);

            log(Level.INFO, "Plugin has been loaded.");
            log(Level.INFO, "Author: JoseGamer_PT | " + this.getDescription().getWebsite());
            log(Level.INFO, star);

            new UpdateChecker(this, 73707).getVersion(version -> {
                if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                    getLogger().info("The plugin is updated to the latest version.");
                } else {
                    this.newUpdate = true;
                    getLogger().info("There is a new update available!");
                }
            });
        }
    }

    private void disablePlugin() {
        HandlerList.unregisterAll(this);

        Bukkit.getPluginManager().disablePlugin(this);
    }


    public void onDisable() {
        if (this.mineHighlight != null) {
            this.mineHighlight.cancel();
        }
        this.getMineManager().clearMemory();
    }
}
