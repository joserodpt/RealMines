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
 * @author José Rodrigues © 2019-2026
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.managers.PrivateMineTemplate;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.PrivateMineData;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Pagination;
import joserodpt.realmines.api.utils.PlayerHeads;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
import joserodpt.realmines.plugin.command.PrivateMineCMD;
import joserodpt.realmines.plugin.managers.PrivateMinesManager;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The private mines catalogue: one icon per template, showing what a copy costs and whether the viewer
 * already has one. Admins can flip it over to a list of every claimed mine on the server.
 */
public class PrivateMinesGUI {

    /**
     * What the grid is showing: the templates a player can claim, the viewer's own mines, or every
     * claimed mine on the server (admins only).
     */
    public enum View {TEMPLATES, OWNED, CLAIMED}

    private static final Map<UUID, PrivateMinesGUI> inventories = new HashMap<>();

    static final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static final ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext page");
    static final ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Previous page");
    static final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, "&cClose");

    private final Inventory inv;
    private final UUID uuid;
    private final RealMines rm;

    /**
     * Slot to template, for the TEMPLATES view.
     */
    private final Map<Integer, PrivateMineTemplate> templateSlots = new HashMap<>();
    /**
     * Slot to claimed mine, for the CLAIMED view.
     */
    private final Map<Integer, RMine> mineSlots = new HashMap<>();

    private Pagination<PrivateMineTemplate> templatePages;
    private Pagination<RMine> minePages;
    private int pageNumber = 0;
    private View view = View.TEMPLATES;

    public PrivateMinesGUI(final RealMines rm, final Player as) {
        this(rm, as, View.TEMPLATES);
    }

    public PrivateMinesGUI(final RealMines rm, final Player as, final View view) {
        this.rm = rm;
        this.uuid = as.getUniqueId();
        this.view = view;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&f&lPrivate &9&lMines"));

        this.load();
        this.register();
    }

    public void load() {
        if (this.view == View.OWNED) {
            this.minePages = new Pagination<>(28, this.rm.getPrivateMinesManager().getMinesOf(this.uuid));
            this.fillClaimed();
        } else if (this.view == View.CLAIMED) {
            this.minePages = new Pagination<>(28, this.rm.getPrivateMinesManager().getPrivateMines());
            this.fillClaimed();
        } else {
            this.templatePages = new Pagination<>(28, new ArrayList<>(this.rm.getPrivateMinesManager().getTemplates()));
            this.fillTemplates();
        }
    }

    private void clampPage(final Pagination<?> pages) {
        if (pages == null || pages.isEmpty()) {
            this.pageNumber = 0;
            return;
        }
        if (this.pageNumber >= pages.totalPages()) {
            this.pageNumber = pages.totalPages() - 1;
        }
        if (this.pageNumber < 0) {
            this.pageNumber = 0;
        }
    }

    private void frame(final boolean toggle) {
        this.inv.clear();
        this.templateSlots.clear();
        this.mineSlots.clear();

        for (int i = 0; i < 9; ++i) {
            this.inv.setItem(i, placeholder);
        }
        for (final int slot : new int[]{45, 46, 47, 48, 49, 50, 51, 52, 53, 36, 44, 9, 17}) {
            this.inv.setItem(slot, placeholder);
        }

        this.inv.setItem(18, back);
        this.inv.setItem(27, back);
        this.inv.setItem(26, next);
        this.inv.setItem(35, next);
        this.inv.setItem(49, close);

        if (toggle) {
            this.inv.setItem(4, Items.createItem(Material.COMPARATOR, 1,
                    "&fClick to view: &b" + (this.view == View.TEMPLATES ? "Claimed mines" : "Templates")));
        }
    }

    private void fillTemplates() {
        final Player viewer = Bukkit.getPlayer(this.uuid);
        this.frame(viewer != null && viewer.hasPermission(PrivateMinesManager.ADMIN_PERMISSION));

        //releasing the last entry on a page can leave us past the end, and getPage throws on that
        this.clampPage(this.templatePages);

        if (this.templatePages.isEmpty()) {
            this.inv.setItem(22, Items.createItem(Material.BARRIER, 1, "&cNo templates",
                    Arrays.asList("&fAn administrator has to create one with", "&b/pmine template create <id> <mine>")));
            return;
        }

        final List<PrivateMineTemplate> page = new ArrayList<>(this.templatePages.getPage(this.pageNumber));

        int slot = 0;
        for (final ItemStack item : this.inv.getContents()) {
            if (item == null && !page.isEmpty()) {
                final PrivateMineTemplate template = page.remove(0);
                this.inv.setItem(slot, this.templateIcon(template, viewer));
                this.templateSlots.put(slot, template);
            }
            ++slot;
        }
    }

    private ItemStack templateIcon(final PrivateMineTemplate template, final Player viewer) {
        final RMine owned = viewer == null ? null
                : this.rm.getPrivateMinesManager().getMineOf(viewer.getUniqueId(), template.getID());

        final List<String> lore = new ArrayList<>(template.getDescription());
        if (!lore.isEmpty()) {
            lore.add("");
        }

        lore.add("&7Resets every: &f" + PrivateMineCMD.formatTime(template.getResetTime()));
        lore.add("&7Lifetime: &f" + describeLifecycle(template));
        lore.add("&7Cost: &f" + (template.getCost() <= 0 ? "Free" : String.valueOf(template.getCost())));
        lore.add("");

        if (owned != null) {
            lore.add("&aYou own this mine.");
            lore.add("&fClick &7to teleport to it.");
            lore.add("&fShift-click &7to manage it.");
        } else if (template.hasPermission() && viewer != null && !viewer.hasPermission(template.getPermission())
                && !viewer.hasPermission(PrivateMinesManager.ADMIN_PERMISSION)) {
            lore.add("&cYou don't have access to this one.");
        } else {
            lore.add("&fClick &7to claim it.");
        }

        return owned != null
                ? Items.createItemLoreEnchanted(template.getIcon(), 1, "&b" + template.getID(), lore)
                : Items.createItem(template.getIcon(), 1, "&b" + template.getID(), lore);
    }

    private static String describeLifecycle(final PrivateMineTemplate template) {
        switch (template.getLifecycle()) {
            case TIME_LIMITED:
                return PrivateMineCMD.formatTime(template.getDuration());
            case SESSION:
                return "Until you log out";
            default:
                return "Permanent";
        }
    }

    /**
     * The mine grid, used by both the viewer's own mines and the server wide list. What a click does is
     * the difference: an owner manages their mine, an admin looking at everybody's teleports to it.
     */
    private void fillClaimed() {
        final boolean own = this.view == View.OWNED;
        this.frame(true);

        this.clampPage(this.minePages);

        if (this.minePages.isEmpty()) {
            this.inv.setItem(22, Items.createItem(Material.BARRIER, 1,
                    own ? "&cYou have no private mines" : "&cNo private mines claimed"));
            return;
        }

        final List<RMine> page = new ArrayList<>(this.minePages.getPage(this.pageNumber));

        int slot = 0;
        for (final ItemStack item : this.inv.getContents()) {
            if (item == null && !page.isEmpty()) {
                final RMine mine = page.remove(0);
                final PrivateMineData data = mine.getPrivateData();

                final List<String> lore = new ArrayList<>();
                lore.add("&7Template: &f" + data.getTemplate());
                if (!own) {
                    lore.add("&7Owner: &f" + data.getOwnerName());
                }
                lore.add("&7Blocks left: &f" + mine.getRemainingBlocks() + "&7/&f" + mine.getBlockCount());
                lore.add("&7Trusted: &f" + data.getTrustedCount());
                lore.add("&7Expires in: &f" + PrivateMineCMD.formatTime(data.getSecondsLeft()));
                lore.add("");
                if (own) {
                    lore.add("&fClick &7to manage it.");
                    lore.add("&fShift-click &7to teleport there.");
                } else {
                    lore.add("&7Slot: &f" + data.getSlot());
                    lore.add("&fClick &7to teleport there.");
                    lore.add("&fDrop (Q) &7to delete it.");
                }

                this.inv.setItem(slot, PlayerHeads.getHead(data.getOwner(),
                        own ? mine.getDisplayName() : "&b" + data.getOwnerName() + "&7's " + data.getTemplate(), lore));
                this.mineSlots.put(slot, mine);
            }
            ++slot;
        }
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(final InventoryClickEvent e) {
                final HumanEntity clicker = e.getWhoClicked();
                if (!(clicker instanceof Player) || e.getCurrentItem() == null) {
                    return;
                }

                final UUID uuid = clicker.getUniqueId();
                final PrivateMinesGUI current = inventories.get(uuid);
                if (current == null || e.getInventory().getHolder() != current.getInventory().getHolder()) {
                    return;
                }

                e.setCancelled(true);
                final Player p = (Player) clicker;

                switch (e.getRawSlot()) {
                    case 49:
                        p.closeInventory();
                        return;
                    case 4:
                        //only the server wide list is an admin's to see; going back to the catalogue is
                        //something any owner looking at their own mines may do
                        if (current.view == View.TEMPLATES) {
                            if (!p.hasPermission(PrivateMinesManager.ADMIN_PERMISSION)) {
                                return;
                            }
                            current.view = View.CLAIMED;
                        } else {
                            current.view = View.TEMPLATES;
                        }
                        current.pageNumber = 0;
                        current.load();
                        return;
                    case 26:
                    case 35:
                        this.turnPage(current, 1);
                        p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                        return;
                    case 18:
                    case 27:
                        this.turnPage(current, -1);
                        p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                        return;
                    default:
                        break;
                }

                if (current.view != View.TEMPLATES) {
                    final RMine mine = current.mineSlots.get(e.getRawSlot());
                    if (mine == null) {
                        return;
                    }

                    if (current.view == View.OWNED) {
                        p.closeInventory();
                        if (e.isShiftClick()) {
                            current.rm.getMineManager().teleport(p, mine, false, true);
                        } else {
                            new PrivateMineManageGUI(current.rm, p, mine).openInventory(p);
                        }
                        return;
                    }

                    if (e.getClick() == ClickType.DROP) {
                        current.rm.getPrivateMinesManager().release(mine);
                        current.load();
                    } else {
                        p.closeInventory();
                        current.rm.getMineManager().teleport(p, mine, true, false);
                    }
                    return;
                }

                final PrivateMineTemplate template = current.templateSlots.get(e.getRawSlot());
                if (template == null) {
                    return;
                }

                final RMine owned = current.rm.getPrivateMinesManager().getMineOf(p.getUniqueId(), template.getID());
                if (owned == null) {
                    p.closeInventory();
                    PrivateMineCMD.tellClaimResult(p, current.rm, template,
                            current.rm.getPrivateMinesManager().claim(p, template));
                    return;
                }

                p.closeInventory();
                if (e.isShiftClick()) {
                    new PrivateMineManageGUI(current.rm, p, owned).openInventory(p);
                } else {
                    current.rm.getMineManager().teleport(p, owned, false, true);
                }
            }

            private void turnPage(final PrivateMinesGUI gui, final int delta) {
                final Pagination<?> pages = gui.view == View.TEMPLATES ? gui.templatePages : gui.minePages;
                if (pages != null && pages.exists(gui.pageNumber + delta)) {
                    gui.pageNumber += delta;
                    gui.load();
                }
            }

            @EventHandler
            public void onClose(final InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    final PrivateMinesGUI gui = inventories.get(e.getPlayer().getUniqueId());
                    if (gui != null) {
                        gui.unregister();
                    }
                }
            }
        };
    }

    public void openInventory(final Player target) {
        final Inventory inv = this.getInventory();
        final InventoryView openInv = target.getOpenInventory();
        if (openInv != null) {
            final Inventory openTop = openInv.getTopInventory();
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
