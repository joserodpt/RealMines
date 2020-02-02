package josegamerpt.realmines.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import josegamerpt.realmines.classes.Enum.Data;
import josegamerpt.realmines.classes.Mine;
import josegamerpt.realmines.classes.MinePlayer;
import josegamerpt.realmines.managers.PlayerManager;
import josegamerpt.realmines.utils.Itens;
import josegamerpt.realmines.utils.PlayerInput;
import josegamerpt.realmines.utils.PlayerInput.InputRunnable;

public class MineResetMenu {

	private static Map<UUID, MineResetMenu> inventories = new HashMap<>();
	private Inventory inv;

	private UUID uuid;
	private Mine min;

	public MineResetMenu(Player as, Mine m) {
		this.uuid = as.getUniqueId();
		inv = Bukkit.getServer().createInventory(null, InventoryType.HOPPER, m.name + " §rReset Options");
		this.min = m;

		load(m);

		this.register();
	}

	public void load(Mine m) {
		inv.clear();
		if (m.resetByPercentage == true) {
			this.inv.setItem(0, Itens.createItemLoreEnchanted(Material.BOOK, 1, "&9Reset By Percentage &7(&a&lON&r&7)",
					Arrays.asList("&7Left click to turn &cOFF", "&fRight Click to input a new percentage.", "&fCurrent Value: &b" + m.resetByPercentageValue + "%")));
		} else {
			this.inv.setItem(0, Itens.createItemLore(Material.BOOK, 1, "&9Reset By Percentage &7(&c&lOFF&r&7)",
					Arrays.asList("&7Left click to turn &aON", "&fRight Click to input a new percentage.", "&fCurrent Value: &b" + m.resetByPercentageValue + "%")));
		}

		if (m.resetByTime == true) {
			this.inv.setItem(4, Itens.createItemLoreEnchanted(Material.CLOCK, 1, "&9Reset By Time &7(&a&lON&r&7)",
					Arrays.asList("&7Left click to turn &cOFF", "&fRight Click to input a new time.", "&fCurrent Value: &b" + m.resetByTimeValue)));
		} else {
			this.inv.setItem(4, Itens.createItemLore(Material.CLOCK, 1, "&9Reset By Time &7(&c&lOFF&r&7)",
					Arrays.asList("&7Left click to turn &aON", "&fRight Click to input a new time.", "&fCurrent Value: &b" + m.resetByTimeValue)));

		}

		this.inv.setItem(2,
				Itens.createItemLore(Material.ACACIA_DOOR, 1, "&9Go Back", Arrays.asList("&7Click here to go back.")));
	}

	public void openInventory(Player target) {
		Inventory inv = getInventory();
		InventoryView openInv = target.getOpenInventory();
		if (openInv != null) {
			Inventory openTop = target.getOpenInventory().getTopInventory();
			if (openTop != null && openTop.getType().name().equalsIgnoreCase(inv.getType().name())) {
				openTop.setContents(inv.getContents());
			} else {
				target.openInventory(inv);
			}
		}
	}

	public static Listener getListener() {
		return new Listener() {
			@EventHandler
			public void onClick(InventoryClickEvent e) {
				HumanEntity clicker = e.getWhoClicked();
				if (clicker instanceof Player) {
					if (e.getCurrentItem() == null) {
						return;
					}
					UUID uuid = clicker.getUniqueId();
					if (inventories.containsKey(uuid)) {
						MineResetMenu current = inventories.get(uuid);
						if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
							return;
						}

						e.setCancelled(true);
						MinePlayer gp = PlayerManager.searchPlayer((Player) clicker);

						if (e.getRawSlot() == 2) {
							gp.player.closeInventory();
							GUIManager.openMine(current.min, gp.player);
						}

						if (e.getRawSlot() == 0) {
							if (e.getClick().equals(ClickType.LEFT)) {
								current.min.resetByPercentage = !current.min.resetByPercentage;
								current.load(current.min);
								current.min.saveData(Data.OPTIONS);
							}
							if (e.getClick().equals(ClickType.RIGHT)) {
								gp.player.closeInventory();
								current.editSetting(0, gp, current.min);
							}
						}

						if (e.getRawSlot() == 4) {
							if (e.getClick().equals(ClickType.LEFT)) {
								current.min.resetByTime = !current.min.resetByTime;
								current.load(current.min);
								current.min.saveData(Data.OPTIONS);
							}
							if (e.getClick().equals(ClickType.RIGHT)) {
								current.editSetting(1, gp, current.min);
							}
						}
					}
				}
			}

			@EventHandler
			public void onClose(InventoryCloseEvent e) {
				if (e.getPlayer() instanceof Player) {
					if (e.getInventory() == null) {
						return;
					}
					Player p = (Player) e.getPlayer();
					UUID uuid = p.getUniqueId();
					if (inventories.containsKey(uuid)) {
						inventories.get(uuid).unregister();
					}
				}
			}
		};
	}

	protected void editSetting(int i, MinePlayer gp, Mine m) {
		if (i == 0) {
			new PlayerInput(gp, new InputRunnable() {
				@Override
				public void run(String s) {
					Double d = -1D;
					try {
						d = Double.valueOf(s.replace("%", ""));
					} catch (Exception ex) {
						gp.sendMessage("&cInput a percentage from 0 to 100.");
						editSetting(0, gp, m);
						return;
					}

					if (d < 1 || d > 100) {
						gp.sendMessage("&cWrong input. Please input a percentage greater than 1 and lower or equal to 100");
						editSetting(0, gp, m);
						return;
					}

					m.resetByPercentageValue = d;
					m.saveData(Data.OPTIONS);
					gp.sendMessage("&fPercentage modified to &b" + d + "%");

					MineResetMenu v = new MineResetMenu(gp.player, m);
					v.openInventory(gp.player);
				}
			}, new InputRunnable() {
				@Override
				public void run(String s) {
					MineResetMenu v = new MineResetMenu(gp.player, m);
					v.openInventory(gp.player);
				}
			});
		}
		if (i == 1) {
			new PlayerInput(gp, new InputRunnable() {
				@Override
				public void run(String s) {
					int d = 0;
					try {
						d = Integer.valueOf(s.replace("%", ""));
					} catch (Exception ex) {
						gp.sendMessage("&cInput a new time in seconds.");
						editSetting(1, gp, m);
						return;
					}

					if (d < 1) {
						gp.sendMessage("&cWrong input. Please input a new time greater than 1");
						editSetting(1, gp, m);
						return;
					}

					m.resetByTimeValue = d;
					m.saveData(Data.OPTIONS);
					gp.sendMessage("&fTime modified to &b" + d + " seconds.");

					MineResetMenu v = new MineResetMenu(gp.player, m);
					v.openInventory(gp.player);
				}
			}, new InputRunnable() {
				@Override
				public void run(String s) {
					MineResetMenu v = new MineResetMenu(gp.player, m);
					v.openInventory(gp.player);
				}
			});
		}
	}

	public Inventory getInventory() {
		return inv;
	}

	private void register() {
		inventories.put(this.uuid, this);
	}

	private void unregister() {
		inventories.remove(this.uuid);
	}
}
