package josegamerpt.realmines.util;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInput implements Listener {

    private static final Map<UUID, PlayerInput> inputs = new HashMap<>();
    private final UUID uuid;

    private final ArrayList<String> texts = Text
            .color(Language.file().getStringList("System.Type-Input"));

    private final InputRunnable runGo;
    private final InputRunnable runCancel;
    private final BukkitTask taskId;

    public PlayerInput(final Player p, final InputRunnable correct, final InputRunnable cancel) {
        this.uuid = p.getUniqueId();
        p.closeInventory();
        this.runGo = correct;
        this.runCancel = cancel;
        this.taskId = new BukkitRunnable() {
            public void run() {
                p.getPlayer().sendTitle(PlayerInput.this.texts.get(0), PlayerInput.this.texts.get(1), 0, 21, 0);
            }
        }.runTaskTimer(RealMines.getInstance(), 0L, 20);

        this.register();
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onPlayerChat(final AsyncPlayerChatEvent event) {
                final Player p = event.getPlayer();
                final String input = event.getMessage();
                final UUID uuid = p.getUniqueId();
                if (inputs.containsKey(uuid)) {
                    final PlayerInput current = inputs.get(uuid);
                    event.setCancelled(true);
                    try {
                        if (input.equalsIgnoreCase("cancel")) {
                            Text.send(p, Language.file().getString("System.Input-Cancelled"));
                            current.taskId.cancel();
                            p.sendTitle("", "", 0, 1, 0);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.getInstance(), () -> current.runCancel.run(input), 3);
                            current.unregister();
                            return;
                        }

                        current.taskId.cancel();
                        Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.getInstance(), () -> current.runGo.run(input), 3);
                        p.sendTitle("", "", 0, 1, 0);
                        current.unregister();
                    } catch (final Exception e) {
                        Text.send(p, Language.file().getString("System.Error-Occurred"));
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private void register() {
        inputs.put(this.uuid, this);
    }

    private void unregister() {
        inputs.remove(this.uuid);
    }

    @FunctionalInterface
    public interface InputRunnable {
        void run(String input);
    }
}
