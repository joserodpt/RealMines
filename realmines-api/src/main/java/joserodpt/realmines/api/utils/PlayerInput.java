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
    private static final String FIELD = "input";
    private final UUID uuid;

    private final List<String> texts = Text
            .color(RMLanguageConfig.file().getStringList("System.Type-Input"));

    private final InputRunnable runGo;
    private final InputRunnable runCancel;
    /** The title reminder, or null while the question is asked in a dialog instead. */
    private BukkitTask taskId;
    private boolean clearInput = true;

    public PlayerInput(final boolean clearInput, final Player p, final InputRunnable correct, final InputRunnable cancel) {
        this(clearInput, p, RMLanguageConfig.file().getStringList("System.Type-Input-Dialog"), correct, cancel);
    }

    /**
     * Waits for the player to type something in chat, with the two title lines from
     * {@code System.Type-Input} on screen.
     *
     * <p>On a server with dialogs (1.21.6 and up) the question is asked in a text box instead,
     * titled and described by {@code dialog}; the chat still answers it if the box cannot be shown.</p>
     *
     * @param dialog the text box's title and description, or null to always ask in chat
     */
    public PlayerInput(final boolean clearInput, final Player p, final List<String> dialog,
                       final InputRunnable correct, final InputRunnable cancel) {
        this.uuid = p.getUniqueId();
        p.closeInventory();
        this.runGo = correct;
        this.runCancel = cancel;
        this.clearInput = clearInput;
        this.register();

        final boolean asked = dialog != null && dialog.size() >= 2 && new DialogForm(dialog.get(0), dialog.get(1))
                .text(FIELD, "", "", 256)
                //Escape would close it without telling the server, leaving the prompt waiting forever
                .closeWithEscape(false)
                .open(p, answers -> this.answered(p, answers.text(FIELD, "")), () -> this.cancelled(p), () -> {
                    //shown in chat instead, unless it has been answered or replaced since
                    if (inputs.get(this.uuid) == this) {
                        this.startTitles(p);
                    }
                });
        if (!asked) {
            this.startTitles(p);
        }
    }

    private void startTitles(final Player p) {
        this.taskId = new BukkitRunnable() {
            public void run() {
                p.getPlayer().sendTitle(PlayerInput.this.texts.get(0), PlayerInput.this.texts.get(1), 0, 21, 0);
            }
        }.runTaskTimer(RealMinesAPI.getInstance().getPlugin(), 0L, 20);
    }

    /** Stops whatever is asking the question: the title reminder, or the text box. */
    private void stop() {
        if (this.taskId != null) {
            this.taskId.cancel();
        }
        DialogForm.close(this.uuid);
    }

    /** What was typed in the text box, answered as if it had been typed in chat. */
    private void answered(final Player p, final String input) {
        //only while this is still the prompt waiting, not one a newer prompt replaced
        if (inputs.remove(this.uuid, this)) {
            handlePlayerInput(p, input, this);
        }
    }

    private void cancelled(final Player p) {
        if (inputs.remove(this.uuid, this)) {
            this.stop();
            TranslatableLine.SYSTEM_INPUT_CANCELLED.send(p);
            Bukkit.getScheduler().scheduleSyncDelayedTask(RealMinesAPI.getInstance().getPlugin(), () -> this.runCancel.run(""), 3);
        }
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
                    current.stop();
                }
            }
        };
    }

    private static void handlePlayerInput(final Player p, String input, final PlayerInput current) {
        if (current.clearInput) {
            input = ChatColor.stripColor(Text.color(input)).trim();
        }

        try {
            current.stop();
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
            previous.stop();
        }
    }

    @FunctionalInterface
    public interface InputRunnable {
        void run(String input);
    }
}
