package josegamerpt.realmines.gui;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.mines.MineIcon;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.utils.Itens;
import josegamerpt.realmines.utils.Pagination;
import josegamerpt.realmines.utils.Text;
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

import java.util.*;

public class MineViewer {

    static ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static ItemStack next = Itens.createItemLore(Material.GREEN_STAINED_GLASS, 1, Language.file().getString("GUI.Items.Next.Name"),
            Language.file().getStringList("GUI.Items.Next.Description"));
    static ItemStack back = Itens.createItemLore(Material.YELLOW_STAINED_GLASS, 1, Language.file().getString("GUI.Items.Back.Name"),
            Language.file().getStringList("GUI.Items.Back.Description"));
    static ItemStack close = Itens.createItemLore(Material.ACACIA_DOOR, 1, Language.file().getString("GUI.Items.Close.Name"),
            Language.file().getStringList("GUI.Items.Close.Description"));

    private static final Map<UUID, MineViewer> inventories = new HashMap<>();
    int pageNumber = 0;
    Pagination<MineIcon> p;
    private final Inventory inv;
    private final UUID uuid;
    private final HashMap<Integer, MineIcon> display = new HashMap<>();

    private RealMines rm;

    public MineViewer(RealMines rm, Player as) {
        this.rm = rm;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(Language.file().getString("GUI.Panel-Name")));

        this.load();

        this.register();
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
                        Player gp = (Player) clicker;

                        switch (e.getRawSlot()) {
                            case 49:
                                gp.closeInventory();
                                break;
                            case 26:
                            case 35:
                                nextPage(current);
                                gp.playSound(gp.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                break;
                            case 18:
                            case 27:
                                backPage(current);
                                gp.playSound(gp.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                break;
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            MineIcon a = current.display.get(e.getRawSlot());

                            if (a.isPlaceholder()) {
                                return;
                            }

                            gp.closeInventory();
                            current.rm.getGUIManager().openMine(a.getMine(), gp);
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

    public void load() {
        List<MineIcon> items = rm.getMineManager().getMineList();

        p = new Pagination<>(28, items);
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
            if (i == null && items.size() != 0) {
                MineIcon s = items.get(0);
                inv.setItem(slot, s.getIcon());
                display.put(slot, s);
                items.remove(0);
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
