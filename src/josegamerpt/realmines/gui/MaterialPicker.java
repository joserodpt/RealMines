package josegamerpt.realmines.gui;

import java.util.ArrayList;
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

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.classes.Mine;
import josegamerpt.realmines.classes.MineBlock;
import josegamerpt.realmines.classes.MinePlayer;
import josegamerpt.realmines.classes.Enum.Data;
import josegamerpt.realmines.classes.Enum.PickType;
import josegamerpt.realmines.managers.PlayerManager;
import josegamerpt.realmines.utils.Itens;
import josegamerpt.realmines.utils.Pagination;
import josegamerpt.realmines.utils.PlayerInput;
import josegamerpt.realmines.utils.PlayerInput.InputRunnable;
import josegamerpt.realmines.utils.Text;

public class MaterialPicker {

	private static Map<UUID, MaterialPicker> inventories = new HashMap<>();
	private Inventory inv;

	static ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
	static ItemStack next = Itens.createItemLore(Material.GREEN_STAINED_GLASS, 1, "&aNext",
			Arrays.asList("&fClick here to go to the next page."));
	static ItemStack back = Itens.createItemLore(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
			Arrays.asList("&fClick here to go back to the next page."));
	static ItemStack close = Itens.createItemLore(Material.ACACIA_DOOR, 1, "&cGo Back",
			Arrays.asList("&fClick here to go back."));
	static ItemStack search = Itens.createItemLore(Material.OAK_SIGN, 1, "&9Search",
			Arrays.asList("&fClick here to search for a block."));

	private UUID uuid;
	private ArrayList<Material> items;
	private HashMap<Integer, Material> display = new HashMap<Integer, Material>();

	int pageNumber = 0;
	Pagination<Material> p;
	private Mine min;
	private PickType pt;

	public MaterialPicker(Mine m, Player pl, PickType block) {
		this.uuid = pl.getUniqueId();
		min = m;
		this.pt = block;
		if (block.equals(PickType.BLOCK)) {
			inv = Bukkit.getServer().createInventory(null, 54, Text.addColor("Pick a new block"));
		}
		if (block.equals(PickType.ICON)) {
			inv = Bukkit.getServer().createInventory(null, 54, Text.addColor("Select icon for " + m.name));
		}

		items = getIcons();

		p = new Pagination<Material>(28, items);
		fillChest(p.getPage(pageNumber));

		this.register();
	}

	public MaterialPicker(Mine m, Player pl, PickType block, String search) {
		this.uuid = pl.getUniqueId();
		min = m;
		this.pt = block;
		if (block.equals(PickType.BLOCK)) {
			inv = Bukkit.getServer().createInventory(null, 54, Text.addColor("Pick a new block"));
		}
		if (block.equals(PickType.ICON)) {
			inv = Bukkit.getServer().createInventory(null, 54, Text.addColor("Select icon for " + m.name));
		}

		items = searchMaterial(search);

		p = new Pagination<Material>(28, items);
		fillChest(p.getPage(pageNumber));

		this.register();
	}

	private ArrayList<Material> getIcons() {
		ArrayList<Material> ms = new ArrayList<Material>();
		if (pt.equals(PickType.ICON)) {
			for (Material m : Material.values()) {
				if (!m.equals(Material.AIR) && m.isSolid() && m.isBlock() && m.isItem()) {
					ms.add(m);
				}
			}
		} else {
			for (Material m : Material.values()) {
				if (!m.equals(Material.AIR) && m.isSolid() && m.isBlock() && m.isItem()) {
					ms.add(m);
				}
			}
		}
		return ms;
	}

	private ArrayList<Material> searchMaterial(String s) {
		ArrayList<Material> ms = new ArrayList<Material>();
		for (Material m : getIcons()) {
			if (m.name().toLowerCase().contains(s.toLowerCase())) {
				ms.add(m);
			}
		}
		return ms;
	}

	public void fillChest(List<Material> items) {

		inv.clear();
		display.clear();

		for (int i = 0; i < 9; i++) {
			inv.setItem(i, placeholder);
		}

		inv.setItem(4, search);

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
					Material s = items.get(0);
					inv.setItem(slot,
							Itens.createItemLore(s, 1, "§9" + s.name(), Arrays.asList("&fClick to pick this.")));
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
						MaterialPicker current = inventories.get(uuid);
						if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
							return;
						}

						MinePlayer gp = PlayerManager.searchPlayer((Player) clicker);

						if (e.getRawSlot() == 4) {
							new PlayerInput(gp, new InputRunnable() {
								@Override
								public void run(String input) {
									if (current.searchMaterial(input).size() == 0) {
										gp.sendMessage("&fNothing found for your results.");

										current.exit(gp);
										return;
									}
									MaterialPicker df = new MaterialPicker(current.min, gp.player, current.pt, input);
									df.openInventory(gp.player);
								}
							}, new InputRunnable() {
								@Override
								public void run(String input) {
									gp.player.closeInventory();
									MineBlocksViewer v = new MineBlocksViewer(gp.player, current.min);
									v.openInventory(gp.player);
								}
							});
						}

						if (e.getRawSlot() == 49) {
							current.exit(gp);
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
							Material a = current.display.get(e.getRawSlot());
							if (current.pt.equals(PickType.ICON)) {
								current.min.icon = a;
								current.min.saveData(Data.ICON);
								gp.player.closeInventory();
								GUIManager.openMine(current.min, gp.player);
							}
							if (current.pt.equals(PickType.BLOCK)) {
								current.min.addBlock(new MineBlock(a, 10D));
								gp.player.closeInventory();
								Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.pl, new Runnable() {
									@Override
									public void run() {
										MineBlocksViewer v = new MineBlocksViewer(gp.player, current.min);
										v.openInventory(gp.player);
									}
								},3 );
							}
						}

						e.setCancelled(true);
					}
				}
			}

			private void backPage(MaterialPicker asd) {
				if (asd.p.exists(asd.pageNumber - 1)) {
					asd.pageNumber--;
				}

				asd.fillChest(asd.p.getPage(asd.pageNumber));
			}

			private void nextPage(MaterialPicker asd) {
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

	protected void exit(MinePlayer gp) {
		if (this.pt.equals(PickType.ICON)) {
			gp.player.closeInventory();
			GUIManager.openMine(this.min, gp.player);
		}
		if (this.pt.equals(PickType.BLOCK)) {
			MineBlocksViewer v = new MineBlocksViewer(gp.player, this.min);
			v.openInventory(gp.player);
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
