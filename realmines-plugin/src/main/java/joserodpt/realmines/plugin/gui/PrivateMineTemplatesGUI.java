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
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Pagination;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
import joserodpt.realmines.plugin.command.PrivateMineCMD;
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

/**
 * The admin's list of private mine templates. One icon per template, and clicking it opens that
 * template's editor.
 * <p>
 * Deliberately not the same menu as {@link PrivateMinesGUI}: that one is what players see to claim a copy
 * of a template, this one is what a template itself is worth knowing - what it costs to hand out, how many
 * copies are out there, and whether anything about it is stopping the next claim.
 */
public class PrivateMineTemplatesGUI {

    private static final Map<UUID, PrivateMineTemplatesGUI> inventories = new HashMap<>();

    private static final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private static final ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext page");
    private static final ItemStack previous = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Previous page");
    private static final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, "&cClose");

    private final Inventory inv;
    private final UUID uuid;
    private final RealMines rm;

    /**
     * Slot to template id. Held by id because editing one reloads it, so the object goes stale.
     */
    private final Map<Integer, String> templateSlots = new HashMap<>();

    private Pagination<PrivateMineTemplate> pages;
    private int pageNumber = 0;

    public PrivateMineTemplatesGUI(final RealMines rm, final Player as) {
        this.rm = rm;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&f&lPrivate &9&lMine Templates"));

        this.load();
        this.register();
    }

    public void load() {
        this.pages = new Pagination<>(28, new ArrayList<>(this.rm.getPrivateMinesManager().getTemplates()));

        this.inv.clear();
        this.templateSlots.clear();

        for (int i = 0; i < 9; ++i) {
            this.inv.setItem(i, placeholder);
        }
        for (final int slot : new int[]{45, 46, 47, 48, 49, 50, 51, 52, 53, 36, 44, 9, 17}) {
            this.inv.setItem(slot, placeholder);
        }

        this.inv.setItem(18, previous);
        this.inv.setItem(27, previous);
        this.inv.setItem(26, next);
        this.inv.setItem(35, next);
        this.inv.setItem(49, close);

        this.inv.setItem(4, Items.createItem(Material.WRITABLE_BOOK, 1, "&9Templates",
                Arrays.asList("&7A template is a frozen snapshot of a mine.",
                        "&7Players claim their own copies of it.",
                        "", "&7Take one with &f/pmine template create <id> <mine>",
                        "&7Retake its blocks with &f/pmine template update <id> <mine>")));

        //deleting the last template on a page can leave us past the end, and getPage throws on that
        if (this.pageNumber >= this.pages.totalPages()) {
            this.pageNumber = Math.max(0, this.pages.totalPages() - 1);
        }

        if (this.pages.isEmpty()) {
            this.inv.setItem(22, Items.createItem(Material.BARRIER, 1, "&cNo templates",
                    Arrays.asList("&fSnapshot a mine into one with",
                            "&b/pmine template create <id> <mine>")));
            return;
        }

        final List<PrivateMineTemplate> page = new ArrayList<>(this.pages.getPage(this.pageNumber));

        int slot = 0;
        for (final ItemStack item : this.inv.getContents()) {
            if (item == null && !page.isEmpty()) {
                final PrivateMineTemplate template = page.remove(0);
                this.inv.setItem(slot, this.icon(template));
                this.templateSlots.put(slot, template.getID());
            }
            ++slot;
        }
    }

    private ItemStack icon(final PrivateMineTemplate template) {
        final List<String> lore = new ArrayList<>();
        lore.add("&7Taken from: &f" + (template.getSourceMine().isEmpty() ? "unknown" : template.getSourceMine()));
        lore.add("&7Cost: &f" + (template.getCost() <= 0 ? "free" : String.valueOf(template.getCost())));
        lore.add("&7Lifetime: &f" + template.getLifecycle().name());
        lore.add("&7Resets every: &f" + PrivateMineCMD.formatTime(template.getResetTime()));
        lore.add("&7Claimed: &f" + this.rm.getPrivateMinesManager().getPrivateMines().stream()
                .filter(mine -> mine.getPrivateData().getTemplate().equalsIgnoreCase(template.getID())).count());
        lore.add("");

        //the same check every claim refuses on, so a broken template is obvious before a player finds it
        final int problems = template.validate().size();
        lore.add(problems == 0 ? "&aReady to hand out."
                : "&cNot claimable: &f" + problems + " &cproblem" + (problems == 1 ? "" : "s"));
        lore.add("");
        lore.add("&fClick &7to edit it.");

        return problems == 0
                ? Items.createItem(template.getIcon(), 1, "&b" + template.getID(), lore)
                : Items.createItem(Material.BARRIER, 1, "&b" + template.getID(), lore);
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(final InventoryClickEvent e) {
                final HumanEntity clicker = e.getWhoClicked();
                if (!(clicker instanceof Player) || e.getCurrentItem() == null) {
                    return;
                }

                final PrivateMineTemplatesGUI current = inventories.get(clicker.getUniqueId());
                if (current == null || e.getInventory().getHolder() != current.getInventory().getHolder()) {
                    return;
                }

                e.setCancelled(true);
                final Player p = (Player) clicker;

                switch (e.getRawSlot()) {
                    case 49:
                        p.closeInventory();
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

                final String templateID = current.templateSlots.get(e.getRawSlot());
                if (templateID == null) {
                    return;
                }

                p.closeInventory();
                new PrivateMineTemplateGUI(current.rm, p, templateID).openInventory(p);
            }

            private void turnPage(final PrivateMineTemplatesGUI gui, final int delta) {
                if (gui.pages != null && gui.pages.exists(gui.pageNumber + delta)) {
                    gui.pageNumber += delta;
                    gui.load();
                }
            }

            @EventHandler
            public void onClose(final InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    final PrivateMineTemplatesGUI gui = inventories.get(e.getPlayer().getUniqueId());
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
