package joserodpt.realmines.plugin.gui;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Pagination;
import joserodpt.realmines.api.utils.PercentageInput;
import joserodpt.realmines.api.utils.PlayerInput;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MineDepthGUI {

    private static final Map<UUID, MineDepthGUI> inventories = new HashMap<>();
    private static final int[] ITEM_SLOTS = {9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
    static final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static final ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, TranslatableLine.GUI_NEXT_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Next.Description"));
    static final ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, TranslatableLine.GUI_PREVIOUS_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Back.Description"));
    static final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, TranslatableLine.GUI_CLOSE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Close.Description"));

    private final Inventory inv;
    private final UUID uuid;
    private final RMine mine;
    private final String selectedBlockSet;
    private final RealMines rm;
    private final Map<Integer, MineItem> display = new HashMap<>();
    private Pagination<MineItem> items;
    private int pageNumber = 0;

    public MineDepthGUI(final RealMines rm, final Player target, final RMine mine, final String selectedBlockSet) {
        this.rm = rm;
        this.mine = mine;
        this.selectedBlockSet = selectedBlockSet;
        this.uuid = target.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&8Depth: " + mine.getDisplayName()));

        this.load();

        this.register();
    }

    public void load() {
        this.items = new Pagination<>(ITEM_SLOTS.length, this.mine.getBlockIcons(this.selectedBlockSet));

        try {
            this.fillChest(this.items.getPage(this.pageNumber));
        } catch (final Exception ignored) {
            this.pageNumber = 0;
            this.fillChest(this.items.getPage(this.pageNumber));
        }
    }

    public void fillChest(final List<MineItem> items) {
        this.inv.clear();
        this.display.clear();

        for (int i = 0; i < 9; ++i) {
            this.inv.setItem(i, placeholder);
        }
        for (int i = 45; i < 54; ++i) {
            this.inv.setItem(i, placeholder);
        }

        this.inv.setItem(4, this.getDirectionIcon());

        this.inv.setItem(45, back);
        this.inv.setItem(49, close);
        this.inv.setItem(53, next);

        int index = 0;
        for (final MineItem mineItem : items) {
            if (index >= ITEM_SLOTS.length) {
                break;
            }

            final int slot = ITEM_SLOTS[index++];
            this.inv.setItem(slot, this.getItemIcon(mineItem));
            if (mineItem.isInteractable()) {
                this.display.put(slot, mineItem);
            }
        }
    }

    private ItemStack getDirectionIcon() {
        final List<String> lore = new ArrayList<>(Arrays.asList(
                "&fDepth &b0% &fis at the &e" + this.mine.getDepthDirection().name() + " &fface,",
                "&fdepth &b100% &fat the opposite one.",
                "&6",
                "&7Next: &f" + this.mine.nextDepthDirection().name(),
                "&a&nClick&r&f to change the face the depth is measured from."));
        return Items.createItem(Material.COMPASS, 1, "&e&lDepth Origin: &f" + this.mine.getDepthDirection().name(), lore);
    }

    private ItemStack getItemIcon(final MineItem mineItem) {
        if (!mineItem.isInteractable()) {
            return mineItem.getItem();
        }

        return Items.createItem(mineItem.getMaterial(), 1, "&3&l" + Text.beautifyMaterialName(mineItem.getMaterial()), Arrays.asList(
                "&fSpawns between &b" + Text.formatPercentages(mineItem.getDepthMin()) + "% &fand &b" + Text.formatPercentages(mineItem.getDepthMax()) + "% &fdepth.",
                "&6",
                "&a&nLeft-Click&r&f to edit the minimum depth.",
                "&e&nRight-Click&r&f to edit the maximum depth.",
                "&c&nQ (Drop)&r&f to reset this block's depth."));
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(final InventoryClickEvent e) {
                final HumanEntity clicker = e.getWhoClicked();
                if (!(clicker instanceof Player)) {
                    return;
                }

                if (e.getCurrentItem() == null) {
                    return;
                }

                final UUID uuid = clicker.getUniqueId();
                if (!inventories.containsKey(uuid)) {
                    return;
                }

                final MineDepthGUI current = inventories.get(uuid);
                if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                    return;
                }

                e.setCancelled(true);
                final Player p = (Player) clicker;

                switch (e.getRawSlot()) {
                    case 4:
                        current.mine.setDepthDirection(current.mine.nextDepthDirection());
                        current.load();
                        return;
                    case 45:
                        if (current.items.exists(current.pageNumber - 1)) {
                            --current.pageNumber;
                        }
                        current.fillChest(current.items.getPage(current.pageNumber));
                        p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                        return;
                    case 49:
                        p.closeInventory();
                        Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm.getPlugin(), () -> {
                            final MineItemsGUI v = new MineItemsGUI(current.rm, p, current.mine, current.selectedBlockSet);
                            v.openInventory(p);
                        }, 2);
                        return;
                    case 53:
                        if (current.items.exists(current.pageNumber + 1)) {
                            ++current.pageNumber;
                        }
                        current.fillChest(current.items.getPage(current.pageNumber));
                        p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                        return;
                }

                if (!current.display.containsKey(e.getRawSlot())) {
                    return;
                }

                final MineItem mineItem = current.display.get(e.getRawSlot());
                switch (e.getClick()) {
                    case DROP:
                        mineItem.setDepthRange(0D, 1D);
                        current.mine.saveData(RMine.MineData.BLOCKS);
                        current.load();
                        break;
                    case RIGHT:
                        current.editDepth(p, mineItem, false);
                        break;
                    default:
                        current.editDepth(p, mineItem, true);
                        break;
                }
            }

            @EventHandler
            public void onClose(final InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    final UUID uuid = e.getPlayer().getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        inventories.get(uuid).unregister();
                    }
                }
            }
        };
    }

    protected void editDepth(final Player p, final MineItem mineItem, final boolean minimum) {
        p.closeInventory();

        final int current = (int) Math.round((minimum ? mineItem.getDepthMin() : mineItem.getDepthMax()) * 100);

        if (RMConfig.file().getBoolean("RealMines.useButtonGUIForPercentages")) {
            final PercentageInput pi = new PercentageInput(p, this.rm.getPlugin(), current, percentage -> this.applyDepth(p, mineItem, minimum, percentage));
            pi.openInventory(p);
        } else {
            Text.send(p, minimum ? "&fInput in the chat the &aminimum &fdepth for this block (0-100%):" : "&fInput in the chat the &emaximum &fdepth for this block (0-100%):");
            new PlayerInput(true, p, s -> {
                final double d;
                try {
                    d = Double.parseDouble(s.replace("%", ""));
                } catch (final Exception ex) {
                    TranslatableLine.SYSTEM_INPUT_PERCENTAGE_ERROR.send(p);
                    this.editDepth(p, mineItem, minimum);
                    return;
                }

                if (d < 0D) {
                    TranslatableLine.SYSTEM_INPUT_PERCENTAGE_ERROR_GREATER.send(p);
                    this.editDepth(p, mineItem, minimum);
                    return;
                }

                if (d > 100D) {
                    TranslatableLine.SYSTEM_INPUT_PERCENTAGE_ERROR_LOWER.send(p);
                    this.editDepth(p, mineItem, minimum);
                    return;
                }

                this.applyDepth(p, mineItem, minimum, (int) d);
            }, s -> this.reopen(p));
        }
    }

    private void applyDepth(final Player p, final MineItem mineItem, final boolean minimum, final int percentage) {
        final double value = percentage / 100D;

        if (minimum) {
            mineItem.setDepthRange(value, Math.max(value, mineItem.getDepthMax()));
        } else {
            mineItem.setDepthRange(Math.min(value, mineItem.getDepthMin()), value);
        }

        this.mine.saveData(RMine.MineData.BLOCKS);

        Text.send(p, "&fDepth of &b" + Text.beautifyMaterialName(mineItem.getMaterial()) + " &fset to &b" + Text.formatPercentages(mineItem.getDepthMin()) + "% &f- &b" + Text.formatPercentages(mineItem.getDepthMax()) + "%&f.");

        this.reopen(p);
    }

    private void reopen(final Player p) {
        final MineDepthGUI v = new MineDepthGUI(this.rm, p, this.mine, this.selectedBlockSet);
        v.openInventory(p);
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
