package josegamerpt.realmines.gui;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.classes.Mine;
import josegamerpt.realmines.classes.MineBlock;
import josegamerpt.realmines.classes.MinePlayer;
import josegamerpt.realmines.managers.PlayerManager;
import josegamerpt.realmines.utils.Itens;
import josegamerpt.realmines.utils.Pagination;
import josegamerpt.realmines.utils.PlayerInput;
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

public class MaterialPicker {

    public enum PickType {ICON, BLOCK}

    static ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static ItemStack next = Itens.createItemLore(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Arrays.asList("&fClick here to go to the next page."));
    static ItemStack back = Itens.createItemLore(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Arrays.asList("&fClick here to go back to the next page."));
    static ItemStack close = Itens.createItemLore(Material.ACACIA_DOOR, 1, "&cGo Back",
            Arrays.asList("&fClick here to go back."));
    static ItemStack search = Itens.createItemLore(Material.OAK_SIGN, 1, "&9Search",
            Arrays.asList("&fClick here to search for a block."));
    private static final Map<UUID, MaterialPicker> inventories = new HashMap<>();
    int pageNumber = 0;
    Pagination<Material> p;
    private Inventory inv;
    private final UUID uuid;
    private final ArrayList<Material> items;
    private final HashMap<Integer, Material> display = new HashMap<Integer, Material>();
    private final Mine min;
    private final PickType pt;

    public MaterialPicker(Mine m, Player pl, PickType block) {
        this.uuid = pl.getUniqueId();
        min = m;
        this.pt = block;

        switch (block) {
            case BLOCK:
                inv = Bukkit.getServer().createInventory(null, 54, Text.color("Pick a new block"));
                break;
            case ICON:
                inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + m.getDisplayName()));
                break;
        }
        items = getIcons();

        p = new Pagination<>(28, items);
        fillChest(p.getPage(pageNumber));

        this.register();
    }

    public MaterialPicker(Mine m, Player pl, PickType block, String search) {
        this.uuid = pl.getUniqueId();
        min = m;
        this.pt = block;

        switch (block) {
            case BLOCK:
                inv = Bukkit.getServer().createInventory(null, 54, Text.color("Pick a new block"));
                break;
            case ICON:
                inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + m.getDisplayName()));
                break;
        }

        items = searchMaterial(search);

        p = new Pagination<>(28, items);
        fillChest(p.getPage(pageNumber));

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
                        MaterialPicker current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        MinePlayer gp = PlayerManager.get((Player) clicker);

                        switch (e.getRawSlot()) {
                            case 4:
                                new PlayerInput(gp, input -> {
                                    if (current.searchMaterial(input).size() == 0) {
                                        gp.sendMessage("&fNothing found for your results.");
                                        current.exit(gp);
                                        return;
                                    }
                                    MaterialPicker df = new MaterialPicker(current.min, gp.getPlayer(), current.pt, input);
                                    df.openInventory(gp.getPlayer());
                                }, input -> {
                                    gp.getPlayer().closeInventory();
                                    MineBlocksViewer v = new MineBlocksViewer(gp.getPlayer(), current.min);
                                    v.openInventory(gp.getPlayer());
                                });
                                break;
                            case 49:
                                current.exit(gp);
                                break;
                            case 26:
                            case 35:
                                nextPage(current);
                                gp.getPlayer().playSound(gp.getPlayer().getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                break;
                            case 18:
                            case 27:
                                backPage(current);
                                gp.getPlayer().playSound(gp.getPlayer().getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                break;
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            Material a = current.display.get(e.getRawSlot());

                            switch (current.pt) {
                                case ICON:
                                    current.min.setIcon(a);
                                    current.min.saveData(Mine.Data.ICON);
                                    gp.getPlayer().closeInventory();
                                    GUIManager.openMine(current.min, gp.getPlayer());
                                    break;
                                case BLOCK:
                                    current.min.addBlock(new MineBlock(a, 10D));
                                    gp.getPlayer().closeInventory();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.pl, new Runnable() {
                                        @Override
                                        public void run() {
                                            MineBlocksViewer v = new MineBlocksViewer(gp.getPlayer(), current.min);
                                            v.openInventory(gp.getPlayer());
                                        }
                                    }, 3);
                                    break;
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

    private ArrayList<Material> getIcons() {
        ArrayList<Material> ms = new ArrayList<Material>();
        switch (pt) {
            default:
                for (Material m : Material.values()) {
                    if (!m.equals(Material.AIR) && m.isSolid() && m.isBlock() && m.isItem()) {
                        ms.add(m);
                    }
                }
                break;
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
            if (i == null && items.size() != 0) {
                Material s = items.get(0);
                inv.setItem(slot,
                        Itens.createItemLore(s, 1, "§9" + s.name(), Arrays.asList("&fClick to pick this.")));
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

    protected void exit(MinePlayer gp) {
        switch (this.pt) {
            case ICON:
                gp.getPlayer().closeInventory();
                GUIManager.openMine(this.min, gp.getPlayer());
                break;
            case BLOCK:
                MineBlocksViewer v = new MineBlocksViewer(gp.getPlayer(), this.min);
                v.openInventory(gp.getPlayer());
                break;
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
