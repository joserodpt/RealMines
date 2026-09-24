package joserodpt.realmines.api.utils;

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
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerInput implements Listener {

    //read and taken from the async chat thread, written from the main thread
    private static final Map<UUID, PlayerInput> inputs = new ConcurrentHashMap<>();
    private final UUID uuid;

    private final List<String> texts = Text
            .color(RMLanguageConfig.file().getStringList("System.Type-Input"));

    private final InputRunnable runGo;
    private final InputRunnable runCancel;
    private final BukkitTask taskId;
    private boolean clearInput = true;

    public PlayerInput(final boolean clearInput, final Player p, final InputRunnable correct, final InputRunnable cancel) {
        this.uuid = p.getUniqueId();
        p.closeInventory();
        this.runGo = correct;
        this.runCancel = cancel;
        this.clearInput = clearInput;
        this.taskId = new BukkitRunnable() {
            public void run() {
                p.getPlayer().sendTitle(PlayerInput.this.texts.get(0), PlayerInput.this.texts.get(1), 0, 21, 0);
            }
        }.runTaskTimer(RealMinesAPI.getInstance().getPlugin(), 0L, 20);

        this.register();
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onPlayerChat(final AsyncPlayerChatEvent event) {
                final Player p = event.getPlayer();
                final String input = event.getMessage();

                //taken in one step, so two quick messages can't both answer the same prompt
                final PlayerInput current = inputs.remove(p.getUniqueId());
                if (current != null) {
                    event.setCancelled(true);
                    //this is the async chat thread: the task, the title and the callbacks belong on the main one
                    Bukkit.getScheduler().runTask(RealMinesAPI.getInstance().getPlugin(),
                            () -> handlePlayerInput(p, input, current));
                }
            }

            @EventHandler
            public void onQuit(final PlayerQuitEvent event) {
                //otherwise the title task runs forever and their first chat line after rejoining answers
                //a prompt from a previous session
                final PlayerInput current = inputs.remove(event.getPlayer().getUniqueId());
                if (current != null) {
                    current.taskId.cancel();
                }
            }
        };
    }

    private static void handlePlayerInput(final Player p, String input, final PlayerInput current) {
        if (current.clearInput) {
            input = ChatColor.stripColor(Text.color(input)).trim();
        }

        try {
            current.taskId.cancel();
            p.sendTitle("", "", 0, 1, 0);
            String cleanInput = ChatColor.stripColor(Text.color(input));
            if (input.equalsIgnoreCase("cancel")) {
                TranslatableLine.SYSTEM_INPUT_CANCELLED.send(p);
                Bukkit.getScheduler().scheduleSyncDelayedTask(RealMinesAPI.getInstance().getPlugin(), () -> current.runCancel.run(cleanInput), 3);
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(RealMinesAPI.getInstance().getPlugin(), () -> current.runGo.run(cleanInput), 3);
            }
        } catch (final Exception e) {
            TranslatableLine.SYSTEM_ERROR_OCCURRED.send(p);
            RealMinesAPI.getInstance().getPlugin().getLogger().warning(e.getMessage());
        }
    }

    private void register() {
        final PlayerInput previous = inputs.put(this.uuid, this);
        //a new prompt replaces an unanswered one, whose title task would otherwise never stop
        if (previous != null) {
            previous.taskId.cancel();
        }
    }

    @FunctionalInterface
    public interface InputRunnable {
        void run(String input);
    }
}
