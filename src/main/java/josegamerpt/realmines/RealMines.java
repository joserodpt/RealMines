package josegamerpt.realmines;

import josegamerpt.realmines.config.Config;
import josegamerpt.realmines.config.Mines;
import josegamerpt.realmines.events.BlockInteractions;
import josegamerpt.realmines.events.BlockModify;
import josegamerpt.realmines.events.PlayerEvents;
import josegamerpt.realmines.gui.MaterialPicker;
import josegamerpt.realmines.gui.MineBlocksViewer;
import josegamerpt.realmines.gui.MineResetMenu;
import josegamerpt.realmines.gui.MineViewer;
import josegamerpt.realmines.managers.MineManager;
import josegamerpt.realmines.managers.PlayerManager;
import josegamerpt.realmines.utils.GUIBuilder;
import josegamerpt.realmines.utils.Itens;
import josegamerpt.realmines.utils.PlayerInput;
import josegamerpt.realmines.utils.Text;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RealMines extends JavaPlugin {

    public static Plugin pl;
    public static String prefix;
    PluginManager pm = Bukkit.getPluginManager();

    CommandManager commandManager;

    public static void log(Level l, String s) {
        Bukkit.getLogger().log(l, s);
    }

    public static String getPrefix() {
        return prefix + " ";
    }

    public void onEnable() {
        pl = this;

        String star = "<------------------ RealMines PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion());
        log(Level.INFO, star);
        log(Level.INFO, "Loading Config Files.");
        saveDefaultConfig();
        Config.setup(this);
        prefix = Text.color(Config.file().getString("RealMines.Prefix"));

        Mines.setup(this);

        log(Level.INFO, "Registering Events.");
        pm.registerEvents(new BlockInteractions(), this);
        pm.registerEvents(new PlayerEvents(), this);
        pm.registerEvents(MineViewer.getListener(), this);
        pm.registerEvents(GUIBuilder.getListener(), this);
        pm.registerEvents(MaterialPicker.getListener(), this);
        pm.registerEvents(MineBlocksViewer.getListener(), this);
        pm.registerEvents(new BlockModify(), this);
        pm.registerEvents(PlayerInput.getListener(), this);
        pm.registerEvents(MineResetMenu.getListener(), this);

        commandManager = new CommandManager(this);
        commandManager.hideTabComplete(true);
        //command suggestions
        commandManager.getCompletionHandler().register("#createsuggestions", input -> {
            List<String> sugests = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                sugests.add("Mine" + i);
            }

            return sugests;
        });
        commandManager.getCompletionHandler().register("#mines", input -> MineManager.getRegisteredMines());

        //command messages
        commandManager.getMessageHandler().register("cmd.no.exists", sender -> {
            sender.sendMessage(RealMines.getPrefix() + Text.color("&cThe command you're trying to run doesn't exist!"));
        });
        commandManager.getMessageHandler().register("cmd.no.permission", sender -> {
            sender.sendMessage(RealMines.getPrefix() + Text.color("&fYou &cdon't &fhave permission to execute this command!"));
        });
        commandManager.getMessageHandler().register("cmd.wrong.usage", sender -> {
            sender.sendMessage(RealMines.getPrefix() + Text.color("&cWrong usage for the command!"));
        });

        //registo de comandos #portugal
        commandManager.register(new Commands());
        log(Level.INFO, "Loading Mines.");
        MineManager.loadMines();
        log(Level.INFO, "Loaded " + MineManager.mines.size() + " mines and " + MineManager.getSigns().size() + " mine signs.");

        Bukkit.getOnlinePlayers().forEach(player -> PlayerManager.loadPlayer(player));

        new BukkitRunnable() {

            @Override
            public void run() {
                PlayerManager.getPlayers().forEach(minePlayer -> minePlayer.getCube().getParticleLocations().forEach(location -> minePlayer.getCube().spawnParticle(location)));
                MineManager.getMines().forEach(mine -> mine.highlight());
            }

        }.runTaskTimer(this, 0, 10);

        log(Level.INFO, "Plugin has been loaded.");
        log(Level.INFO, "Author: JoseGamer_PT | " + this.getDescription().getWebsite());
        log(Level.INFO, star);
    }

    public void onDisable() {
        MineManager.clearMemory();
    }
}
