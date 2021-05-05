package josegamerpt.realmines.gui;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.mines.Mine;
import josegamerpt.realmines.mines.MineBlock;
import josegamerpt.realmines.mines.MineBlockIcon;
import josegamerpt.realmines.config.Language;
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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MineBlocksViewer {

    private RealMines rm;

    private static final Map<UUID, MineBlocksViewer> inventories = new HashMap<>();
    static ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static ItemStack next = Itens.createItemLore(Material.GREEN_STAINED_GLASS, 1, Language.file().getString("GUI.Items.Next.Name"),
            Language.file().getStringList("GUI.Items.Next.Description"));
    static ItemStack back = Itens.createItemLore(Material.YELLOW_STAINED_GLASS, 1, Language.file().getString("GUI.Items.Back.Name"),
            Language.file().getStringList("GUI.Items.Back.Description"));
    static ItemStack close = Itens.createItemLore(Material.ACACIA_DOOR, 1, Language.file().getString("GUI.Items.Close.Name"),
            Language.file().getStringList("GUI.Items.Close.Description"));

    static ItemStack add = Itens.createItemLore(Material.HOPPER, 1, Language.file().getString("GUI.Items.Add.Name"),
            Language.file().getStringList("GUI.Items.Add.Description"));
    private final Inventory inv;
    private final UUID uuid;
    private final HashMap<Integer, MineBlockIcon> display = new HashMap<>();
    private final Mine m;

    int pageNumber = 0;
    Pagination<MineBlockIcon> p;

    public MineBlocksViewer(RealMines rm, Player target, Mine min) {
        this.rm = rm;
        this.uuid = target.getUniqueId();
        this.m = min;
        inv = Bukkit.getServer().createInventory(null, 54, Text.color(Language.file().getString("GUI.Mine-Blocks-Name").replaceAll("%mine%",m.getDisplayName())));

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
                        Player p = (Player) clicker;

                        MineBlocksViewer current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        if (e.getClickedInventory().getType() == InventoryType.PLAYER)
                        {

                            if (Itens.getValidBlocks().contains(e.getCurrentItem().getType()))
                            {
                                current.m.addBlock(new MineBlock(e.getCurrentItem().getType(), 10D));
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 50, 50);
                                p.closeInventory();
                                Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm, () -> {
                                    MineBlocksViewer v = new MineBlocksViewer(current.rm, p, current.m);
                                    v.openInventory(p);
                                }, 3);
                            } else {
                                Text.send(clicker, Language.file().getString("System.Cant-Add-Item"));
                            }

                            e.setCancelled(true);
                        } else {

                            e.setCancelled(true);

                            switch (e.getRawSlot()) {
                                case 49:
                                    p.closeInventory();
                                    current.rm.getGUIManager().openMine(current.m, p);
                                    break;
                                case 4:
                                    p.closeInventory();
                                    MaterialPicker mp = new MaterialPicker(current.rm, current.m, p, MaterialPicker.PickType.BLOCK, "");
                                    mp.openInventory(p);
                                    break;
                                case 26:
                                case 35:
                                    nextPage(current);
                                    p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                    break;
                                case 18:
                                case 27:
                                    backPage(current);
                                    p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                    break;
                            }

                            if (current.display.containsKey(e.getRawSlot())) {
                                MineBlockIcon a = current.display.get(e.getRawSlot());

                                if (a.isPlaceholder()) {
                                    return;
                                }

                                if (e.getClick() == ClickType.DROP) {
                                    // eliminar
                                    current.m.removeBlock(a.getMineBlock());
                                    Text.send(p, Language.file().getString("System.Remove").replace("%object%", a.getMineBlock().getMaterial().name()));
                                    p.closeInventory();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm, () -> {
                                        MineBlocksViewer v = new MineBlocksViewer(current.rm, p, current.m);
                                        v.openInventory(p);
                                    }, 3);
                                } else {
                                    // resto
                                    current.editPercentage(p, a, current);
                                }

                                p.closeInventory();
                            }
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
        p = new Pagination<>(28, m.getBlocks());
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
                MineBlockIcon s = items.get(0);
                inv.setItem(slot, s.getItemStack());
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

    protected void editPercentage(Player gp, MineBlockIcon a, MineBlocksViewer current) {
        new PlayerInput(gp, s -> {
            double d;
            try {
                d = Double.parseDouble(s.replace("%", ""));
            } catch (Exception ex) {
                gp.sendMessage(Text.color("&cInput a percentage from 0 to 100."));
                editPercentage(gp, a, current);
                return;
            }

            if (d < 1) {
                gp.sendMessage(Text.color("&cWrong input. Please input a percentage greater than 1"));
                editPercentage(gp, a, current);
                return;
            }

            a.getMineBlock().setPercentage(d);
            current.m.saveData(Mine.Data.BLOCKS);
            gp.sendMessage(Text.color("&fPercentage modified to &b" + d + "%"));
            MineBlocksViewer v = new MineBlocksViewer(current.rm, gp, current.m);
            v.openInventory(gp);
        }, s -> {
            MineBlocksViewer v = new MineBlocksViewer(current.rm, gp, current.m);
            v.openInventory(gp);
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
