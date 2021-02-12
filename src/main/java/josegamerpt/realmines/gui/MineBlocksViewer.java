package josegamerpt.realmines.gui;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.classes.Mine;
import josegamerpt.realmines.classes.MineBlockIcon;
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

public class MineBlocksViewer {

    private static final Map<UUID, MineBlocksViewer> inventories = new HashMap<>();
    static ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static ItemStack next = Itens.createItemLore(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    static ItemStack back = Itens.createItemLore(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    static ItemStack close = Itens.createItemLore(Material.ACACIA_DOOR, 1, "&cBack",
            Collections.singletonList("&fClick here to go back."));
    static ItemStack add = Itens.createItemLore(Material.HOPPER, 1, "&bAdd Block",
            Collections.singletonList("&fClick here to add a new block."));
    private final Inventory inv;
    private final UUID uuid;
    private final HashMap<Integer, MineBlockIcon> display = new HashMap<Integer, MineBlockIcon>();
    private final Mine m;
    int pageNumber = 0;
    Pagination<MineBlockIcon> p;
    private List<MineBlockIcon> items;

    public MineBlocksViewer(Player target, Mine min) {
        this.uuid = target.getUniqueId();
        this.m = min;
        inv = Bukkit.getServer().createInventory(null, 54, Text.color(min.getDisplayName() + " &rblocks"));

        load();

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
                        MineBlocksViewer current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);
                        MinePlayer gp = PlayerManager.get((Player) clicker);

                        switch (e.getRawSlot()) {
                            case 49:
                                gp.getPlayer().closeInventory();
                                GUIManager.openMine(current.m, gp.getPlayer());
                                break;
                            case 4:
                                gp.getPlayer().closeInventory();
                                MaterialPicker mp = new MaterialPicker(current.m, gp.getPlayer(), MaterialPicker.PickType.BLOCK);
                                mp.openInventory(gp.getPlayer());
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
                            MineBlockIcon a = current.display.get(e.getRawSlot());

                            if (a.placeholder) {
                                return;
                            }

                            switch (e.getClick()) {
                                case DROP:
                                    // eliminar
                                    current.m.removeBlock(a.mb);
                                    gp.sendMessage("&fYou removed &b" + a.mb.getMaterial().name());
                                    gp.getPlayer().closeInventory();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.pl, new Runnable() {
                                        @Override
                                        public void run() {
                                            MineBlocksViewer v = new MineBlocksViewer(gp.getPlayer(), current.m);
                                            v.openInventory(gp.getPlayer());
                                        }
                                    }, 3);

                                    break;
                                default:
                                    // resto
                                    current.editPercentage(gp, a, current);
                            }

                            gp.getPlayer().closeInventory();

                        }
                    }
                }
            }

            private void backPage(MineBlocksViewer asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    asd.pageNumber--;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(MineBlocksViewer asd) {
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
        items = m.getBlocks();

        p = new Pagination<>(28, items);
        fillChest(p.getPage(pageNumber));
    }

    public void fillChest(List<MineBlockIcon> items) {

        inv.clear();
        display.clear();

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, placeholder);
        }
        inv.setItem(4, add);

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
                if (items.size() != 0) {
                    MineBlockIcon s = items.get(0);
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

    protected void editPercentage(MinePlayer gp, MineBlockIcon a, MineBlocksViewer current) {
        new PlayerInput(gp, s -> {
            double d;
            try {
                d = Double.valueOf(s.replace("%", ""));
            } catch (Exception ex) {
                gp.sendMessage("&cInput a percentage from 0 to 100.");
                editPercentage(gp, a, current);
                return;
            }

            if (d < 1) {
                gp.sendMessage("&cWrong input. Please input a percentage greater than 1");
                editPercentage(gp, a, current);
                return;
            }

            a.mb.setPercentage(d);
            current.m.saveData(Mine.Data.BLOCKS);
            gp.sendMessage("&fPercentage modified to &b" + d + "%");
            MineBlocksViewer v = new MineBlocksViewer(gp.getPlayer(), current.m);
            v.openInventory(gp.getPlayer());
        }, s -> {
            MineBlocksViewer v = new MineBlocksViewer(gp.getPlayer(), current.m);
            v.openInventory(gp.getPlayer());
        });
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
