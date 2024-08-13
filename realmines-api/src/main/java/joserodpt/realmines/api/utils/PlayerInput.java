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
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerInput implements Listener {

    private static final Map<UUID, PlayerInput> inputs = new HashMap<>();
    private final UUID uuid;

    private final List<String> texts = Text
            .color(RMLanguageConfig.file().getStringList("System.Type-Input"));

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
        }.runTaskTimer(RealMinesAPI.getInstance().getPlugin(), 0L, 20);

        this.register();
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onPlayerChat(final AsyncPlayerChatEvent event) {
                final Player p = event.getPlayer();
                final String input = event.getMessage();
                final UUID uuid = p.getUniqueId();

                if (inputs.containsKey(uuid)) {
                    event.setCancelled(true);
                    handlePlayerInput(p, input, uuid);
                }
            }
        };
    }

    public static SimplePacketListenerAbstract getPacketListener() {
        return new SimplePacketListenerAbstract(PacketListenerPriority.LOWEST) {

            @Override
            public void onPacketPlayReceive(final PacketPlayReceiveEvent event) {
                if (event.getPacketType() != PacketType.Play.Client.CHAT_MESSAGE) return;

                final WrapperPlayClientChatMessage chatMessage = new WrapperPlayClientChatMessage(event);
                final Player p = event.getPlayer();
                final String input = chatMessage.getMessage();
                final UUID uuid = p.getUniqueId();

                if (inputs.containsKey(uuid)) {
                    event.setCancelled(true);
                    handlePlayerInput(p, input, uuid);
                }
            }
        };
    }

    private static void handlePlayerInput(final Player p, final String input, final UUID uuid) {
        final PlayerInput current = inputs.get(uuid);
        try {
            current.taskId.cancel();
            p.sendTitle("", "", 0, 1, 0);
            current.unregister();
            if (input.equalsIgnoreCase("cancel")) {
                TranslatableLine.SYSTEM_INPUT_CANCELLED.send(p);
                Bukkit.getScheduler().scheduleSyncDelayedTask(RealMinesAPI.getInstance().getPlugin(), () -> current.runCancel.run(input), 3);
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(RealMinesAPI.getInstance().getPlugin(), () -> current.runGo.run(input), 3);
            }
        } catch (final Exception e) {
            TranslatableLine.SYSTEM_ERROR_OCCURRED.send(p);
            RealMinesAPI.getInstance().getPlugin().getLogger().warning(e.getMessage());
        }
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
