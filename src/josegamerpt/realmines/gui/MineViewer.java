package josegamerpt.realmines.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import josegamerpt.realmines.classes.MineIcon;
import josegamerpt.realmines.classes.MinePlayer;
import josegamerpt.realmines.managers.MineManager;
import josegamerpt.realmines.managers.PlayerManager;
import josegamerpt.realmines.utils.Itens;
import josegamerpt.realmines.utils.Pagination;

public class MineViewer {

	private static Map<UUID, MineViewer> inventories = new HashMap<>();
	private Inventory inv;

	static ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
	static ItemStack next = Itens.createItemLore(Material.GREEN_STAINED_GLASS, 1, "&aNext",
			Arrays.asList("&fClick here to go to the next page."));
	static ItemStack back = Itens.createItemLore(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
			Arrays.asList("&fClick here to go back to the next page."));
	static ItemStack close = Itens.createItemLore(Material.ACACIA_DOOR, 1, "&cClose",
			Arrays.asList("&fClick here to close this menu."));

	private UUID uuid;
	private List<MineIcon> items;
	private HashMap<Integer, MineIcon> display = new HashMap<Integer, MineIcon>();

	int pageNumber = 0;
	Pagination<MineIcon> p;

	public MineViewer(Player as) {
		this.uuid = as.getUniqueId();
		inv = Bukkit.getServer().createInventory(null, 54, "§9Mines");

		load();

		this.register();
	}

	public void load() {
		items = MineManager.getMineList();

		p = new Pagination<MineIcon>(28, items);
		fillChest(p.getPage(pageNumber));
	}

	public void fillChest(List<MineIcon> items) {

		inv.clear();
		display.clear();

		for (int i = 0; i < 9; i++) {
			inv.setItem(i, placeholder);
		}

		inv.setItem(45, placeholder);
		inv.setItem(46, placeholder);
		inv.setItem(47, placeholder);
		inv.setItem(48, placeholder);
		inv.setItem(49, placeholder);
		inv.setItem(50, placeholder);
		inv.setItem(51, placeholder);
		inv.setItem(52, placeholder);
		inv.setItem(53, placeholder);
		inv.setItem(36, placeholder);
		inv.setItem(44, placeholder);
		inv.setItem(9, placeholder);
		inv.setItem(17, placeholder);

		inv.setItem(18, back);
		inv.setItem(27, back);
		inv.setItem(26, next);
		inv.setItem(35, next);

		int slot = 0;
		for (ItemStack i : inv.getContents()) {
			if (i == null) {
				if (items.size() != 0) {
					MineIcon s = items.get(0);
					inv.setItem(slot, s.i);
					display.put(slot, s);
					items.remove(0);
				}
			}
			slot++;
		}

		inv.setItem(49, close);
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
						MineViewer current = inventories.get(uuid);
						if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
							return;
						}

						e.setCancelled(true);
						MinePlayer gp = PlayerManager.searchPlayer((Player) clicker);

						if (e.getRawSlot() == 49) {
							gp.player.closeInventory();
						}

						if (e.getRawSlot() == 26 || e.getRawSlot() == 35) {
							nextPage(current);
							gp.player.playSound(gp.player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
						}
						if (e.getRawSlot() == 18 || e.getRawSlot() == 27) {
							backPage(current);
							gp.player.playSound(gp.player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
						}

						if (current.display.containsKey(e.getRawSlot())) {

							MineIcon a = current.display.get(e.getRawSlot());

							if (a.placeholder == true) {
								return;
							}

							gp.player.closeInventory();
							GUIManager.openMine(a.m, gp.player);
						}
					}
				}
			}

			private void backPage(MineViewer asd) {
				if (asd.p.exists(asd.pageNumber - 1)) {
					asd.pageNumber--;
				}

				asd.fillChest(asd.p.getPage(asd.pageNumber));
			}

			private void nextPage(MineViewer asd) {
				if (asd.p.exists(asd.pageNumber + 1)) {
					asd.pageNumber++;
				}

				asd.fillChest(asd.p.getPage(asd.pageNumber));
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
