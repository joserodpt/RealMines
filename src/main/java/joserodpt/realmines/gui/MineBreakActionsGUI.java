package joserodpt.realmines.gui;

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

import joserodpt.realmines.RealMines;
import joserodpt.realmines.config.Language;
import joserodpt.realmines.mine.RMine;
import joserodpt.realmines.mine.components.actions.MineAction;
import joserodpt.realmines.mine.components.actions.MineActionDummy;
import joserodpt.realmines.util.Items;
import joserodpt.realmines.util.Pagination;
import org.apache.commons.lang.WordUtils;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MineBreakActionsGUI {

    private static final Map<UUID, MineBreakActionsGUI> inventories = new HashMap<>();
    static ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static ItemStack next = Items.createItemLore(Material.GREEN_STAINED_GLASS, 1, Language.file().getString("GUI.Items.Next.Name"),
            Language.file().getStringList("GUI.Items.Next.Description"));
    static ItemStack back = Items.createItemLore(Material.YELLOW_STAINED_GLASS, 1, Language.file().getString("GUI.Items.Back.Name"),
            Language.file().getStringList("GUI.Items.Back.Description"));
    static ItemStack close = Items.createItemLore(Material.ACACIA_DOOR, 1, Language.file().getString("GUI.Items.Close.Name"),
            Language.file().getStringList("GUI.Items.Close.Description"));
    static ItemStack add = Items.createItemLore(Material.OBSERVER, 1, "&b&LAdd a New Break Action",
            Collections.singletonList("&fClick here to add a new break action to this item."));
    private final Inventory inv;
    private final UUID uuid;
    private final HashMap<Integer, MineAction> display = new HashMap<>();
    private final RMine m;
    private final Material mat;
    private int pageNumber = 0;
    private Pagination<MineAction> p;
    private final RealMines rm;

    //TODO: add option to add new break actions

    public MineBreakActionsGUI(final RealMines rm, final Player target, final RMine min, final Material mat) {
        this.rm = rm;
        this.uuid = target.getUniqueId();
        this.m = min;
        this.mat = mat;
        this.inv = Bukkit.getServer().createInventory(null, 54, WordUtils.capitalizeFully(mat.name().replace("_", " ")) + " break actions");

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

                        e.setCancelled(true);

                        final MineBreakActionsGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        switch (e.getRawSlot()) {
                            case 49:
                                p.closeInventory();
                                final MineItensGUI v = new MineItensGUI(current.rm, p, current.m);
                                v.openInventory(p);
                                break;
                            case 4:
                                p.closeInventory();
                                BlockPickerGUI mp = null;
                                switch (current.m.getType()) {
                                    case BLOCKS:
                                        mp = new BlockPickerGUI(current.rm, current.m, p, BlockPickerGUI.PickType.BLOCK, "");
                                        break;
                                    case FARM:
                                        mp = new BlockPickerGUI(current.rm, current.m, p, BlockPickerGUI.PickType.FARM_ITEM, "");
                                        break;
                                }
                                if (mp != null)
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
                            final MineAction a = current.display.get(e.getRawSlot());

                            if (a.isInteractable()) {

                            }
                            //aaaa
                        }
                    }
                }
            }

            private void backPage(final MineBreakActionsGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    --asd.pageNumber;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(final MineBreakActionsGUI asd) {
                if (asd.p.exists(asd.pageNumber + 1)) {
                    ++asd.pageNumber;
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
        if (this.m.getMineItems().get(this.mat).getBreakActions().isEmpty()) {
            this.p = new Pagination<>(28, Collections.singletonList(new MineActionDummy()));
        } else {
            this.p = new Pagination<>(28, this.m.getMineItems().get(this.mat).getBreakActions().stream().sorted(Comparator.comparingDouble(MineAction::getChance).reversed()).collect(Collectors.toList()));
        }

        this.fillChest(this.p.getPage(this.pageNumber));
    }

    public void fillChest(final List<MineAction> items) {
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
                final MineAction s = items.get(0);
                this.inv.setItem(slot, s.getItem());
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
