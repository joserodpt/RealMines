package josegamerpt.realmines.gui;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mine.BlockMine;
import josegamerpt.realmines.mine.component.MineBlock;
import josegamerpt.realmines.mine.gui.MineBlockIcon;
import josegamerpt.realmines.util.Items;
import josegamerpt.realmines.util.Pagination;
import josegamerpt.realmines.util.PlayerInput;
import josegamerpt.realmines.util.Text;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MineBlocksViewer {

    private static final Map<UUID, MineBlocksViewer> inventories = new HashMap<>();
    static ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static ItemStack next = Items.createItemLore(Material.GREEN_STAINED_GLASS, 1, Language.file().getString("GUI.Items.Next.Name"),
            Language.file().getStringList("GUI.Items.Next.Description"));
    static ItemStack back = Items.createItemLore(Material.YELLOW_STAINED_GLASS, 1, Language.file().getString("GUI.Items.Back.Name"),
            Language.file().getStringList("GUI.Items.Back.Description"));
    static ItemStack close = Items.createItemLore(Material.ACACIA_DOOR, 1, Language.file().getString("GUI.Items.Close.Name"),
            Language.file().getStringList("GUI.Items.Close.Description"));
    static ItemStack add = Items.createItemLore(Material.HOPPER, 1, Language.file().getString("GUI.Items.Add.Name"),
            Language.file().getStringList("GUI.Items.Add.Description"));
    private final Inventory inv;
    private final UUID uuid;
    private final HashMap<Integer, MineBlockIcon> display = new HashMap<>();
    private final BlockMine m;
    int pageNumber = 0;
    Pagination<MineBlockIcon> p;
    private final RealMines rm;

    public MineBlocksViewer(final RealMines rm, final Player target, final BlockMine min) {
        this.rm = rm;
        this.uuid = target.getUniqueId();
        this.m = min;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(Language.file().getString("GUI.Mine-Blocks-Name").replaceAll("%mine%", this.m.getDisplayName())));

        this.load();

        this.register();
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(final InventoryClickEvent e) {
                final HumanEntity clicker = e.getWhoClicked();
                if (clicker instanceof Player) {
                    if (e.getCurrentItem() == null) {
                        return;
                    }

                    final UUID uuid = clicker.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        final Player p = (Player) clicker;

                        final MineBlocksViewer current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {

                            if (Items.getValidBlocks().contains(e.getCurrentItem().getType())) {
                                ((BlockMine) current.m).addBlock(new MineBlock(e.getCurrentItem().getType(), 0.1D));
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 50, 50);
                                p.closeInventory();
                                Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm, () -> {
                                    final MineBlocksViewer v = new MineBlocksViewer(current.rm, p, current.m);
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
                                    final MaterialPicker mp = new MaterialPicker(current.rm, current.m, p, MaterialPicker.PickType.BLOCK, "");
                                    mp.openInventory(p);
                                    break;
                                case 26:
                                case 35:
                                    this.nextPage(current);
                                    p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                    break;
                                case 18:
                                case 27:
                                    this.backPage(current);
                                    p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                    break;
                            }

                            if (current.display.containsKey(e.getRawSlot())) {
                                final MineBlockIcon a = current.display.get(e.getRawSlot());

                                if (a.isPlaceholder()) {
                                    return;
                                }

                                if (e.getClick() == ClickType.DROP) {
                                    // eliminar
                                    ((BlockMine) current.m).removeBlock(a.getMineBlock());
                                    Text.send(p, Language.file().getString("System.Remove").replace("%object%", a.getMineBlock().getMaterial().name()));
                                    p.closeInventory();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm, () -> {
                                        final MineBlocksViewer v = new MineBlocksViewer(current.rm, p, current.m);
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

            private void backPage(final MineBlocksViewer asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    asd.pageNumber--;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(final MineBlocksViewer asd) {
                if (asd.p.exists(asd.pageNumber + 1)) {
                    asd.pageNumber++;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            @EventHandler
            public void onClose(final InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    if (e.getInventory() == null) {
                        return;
                    }
                    final Player p = (Player) e.getPlayer();
                    final UUID uuid = p.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        inventories.get(uuid).unregister();
                    }
                }
            }
        };
    }

    public void load() {
        this.p = new Pagination<>(28, this.m.getBlockIcons());
        this.fillChest(this.p.getPage(this.pageNumber));
    }

    public void fillChest(final List<MineBlockIcon> items) {
        this.inv.clear();
        this.display.clear();

        for (int i = 0; i < 9; ++i) {
            this.inv.setItem(i, placeholder);
        }
        this.inv.setItem(4, add);

        this.inv.setItem(45, placeholder);
        this.inv.setItem(46, placeholder);
        this.inv.setItem(47, placeholder);
        this.inv.setItem(48, placeholder);
        this.inv.setItem(49, placeholder);
        this.inv.setItem(50, placeholder);
        this.inv.setItem(51, placeholder);
        this.inv.setItem(52, placeholder);
        this.inv.setItem(53, placeholder);
        this.inv.setItem(36, placeholder);
        this.inv.setItem(44, placeholder);
        this.inv.setItem(9, placeholder);
        this.inv.setItem(17, placeholder);

        this.inv.setItem(18, back);
        this.inv.setItem(27, back);
        this.inv.setItem(26, next);
        this.inv.setItem(35, next);

        int slot = 0;
        for (final ItemStack i : this.inv.getContents()) {
            if (i == null && !items.isEmpty()) {
                final MineBlockIcon s = items.get(0);
                this.inv.setItem(slot, s.getItemStack());
                this.display.put(slot, s);
                items.remove(0);
            }
            slot++;
        }

        this.inv.setItem(49, close);
    }

    public void openInventory(final Player target) {
        final Inventory inv = this.getInventory();
        final InventoryView openInv = target.getOpenInventory();
        if (openInv != null) {
            final Inventory openTop = target.getOpenInventory().getTopInventory();
            if (openTop != null && openTop.getType().name().equalsIgnoreCase(inv.getType().name())) {
                openTop.setContents(inv.getContents());
            } else {
                target.openInventory(inv);
            }
        }
    }

    protected void editPercentage(final Player gp, final MineBlockIcon a, final MineBlocksViewer current) {
        new PlayerInput(gp, s -> {
            double d = 0;
            try {
                d = Double.parseDouble(s.replace("%", ""));
            } catch (final Exception ex) {
                gp.sendMessage(Text.color(Language.file().getString("System.Input-Percentage-Error")));
                this.editPercentage(gp, a, current);
            }

            if (d <= 0) {
                gp.sendMessage(Text.color(Language.file().getString("System.Input-Percentage-Error-Greater")));
                this.editPercentage(gp, a, current);
                return;
            }

            if (d > 100) {
                gp.sendMessage(Text.color(Language.file().getString("System.Input-Percentage-Error-Lower")));
                this.editPercentage(gp, a, current);
                return;
            }

            d /= 100;

            a.getMineBlock().setPercentage(d);
            current.m.saveData(BlockMine.Data.BLOCKS);
            gp.sendMessage(Text.color(Language.file().getString("System.Percentage-Modified").replaceAll("%value%", String.valueOf(d * 100))));
            final MineBlocksViewer v = new MineBlocksViewer(current.rm, gp, current.m);
            v.openInventory(gp);
        }, s -> {
            final MineBlocksViewer v = new MineBlocksViewer(current.rm, gp, current.m);
            v.openInventory(gp);
        });
    }

    public Inventory getInventory() {
        return this.inv;
    }

    private void register() {
        inventories.put(this.uuid, this);
    }

    private void unregister() {
        inventories.remove(this.uuid);
    }
}
