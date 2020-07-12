package josegamerpt.realmines.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.classes.MinePlayer;
import josegamerpt.realmines.managers.PlayerManager;

public class PlayerInput implements Listener {

	private static Map<UUID, PlayerInput> inputs = new HashMap<>();
	private UUID uuid;

	private ArrayList<String> texts = Text
			.color(Arrays.asList("&l&9Type in chat your input", "&fType &4cancel &fto cancel"));

	private InputRunnable runGo;
	private InputRunnable runCancel;
	private BukkitTask taskId;
	private Boolean inputMode;

	public PlayerInput(MinePlayer p, InputRunnable correct, InputRunnable cancel) {
		this.uuid = p.player.getUniqueId();
		p.player.closeInventory();
		this.inputMode = true;
		this.runGo = correct;
		this.runCancel = cancel;
		this.taskId = new BukkitRunnable() {
			public void run() {
				p.player.sendTitle(texts.get(0), texts.get(1), 0, 21, 0);
			}
		}.runTaskTimer(RealMines.pl, 0L, (long) 20);

		this.register();
	}

	private void register() {
		inputs.put(this.uuid, this);
	}

	private void unregister() {
		inputs.remove(this.uuid);
	}

	@FunctionalInterface
	public interface InputRunnable {
		public void run(String input);
	}

	public static Listener getListener() {
		return new Listener() {
			@EventHandler
			public void onPlayerChat(AsyncPlayerChatEvent event) {
				MinePlayer p = PlayerManager.get(event.getPlayer());
				String input = event.getMessage();
				UUID uuid = p.player.getUniqueId();
				if (inputs.containsKey(uuid)) {
					PlayerInput current = inputs.get(uuid);
					if (current.inputMode == true) {
						event.setCancelled(true);
						try {
							if (input.equalsIgnoreCase("cancel")) {
								p.sendMessage("&fInput canceled.");
								current.taskId.cancel();
								p.player.sendTitle("", "", 0, 1, 0);
								Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.pl, new Runnable() {
									@Override
									public void run() {
										current.runCancel.run(input);
									}
								}, 3);
								current.unregister();
								return;
							}

							current.taskId.cancel();
							Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.pl, new Runnable() {
								@Override
								public void run() {
									current.runGo.run(input);
								}
							}, 3);
							p.player.sendTitle("", "", 0, 1, 0);
							current.unregister();
						} catch (Exception e) {
							p.sendMessage("&cAn error ocourred. Contact JoseGamer_PT on Spigot.com");
							e.printStackTrace();
						}
					}
				}
			}

		};
	}
}
