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

import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.config.TranslatableLine.ReplacableVar;
import joserodpt.realmines.api.managers.PrivateMinePlatform;
import joserodpt.realmines.api.managers.PrivateMineTemplate;
import joserodpt.realmines.api.mine.components.PrivateMineData;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.PlayerInput;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
import joserodpt.realmines.plugin.command.PrivateMineCMD;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
 * Editor for one private mine template: everything the file holds except the snapshot itself, which only
 * {@code /pmine template update} can retake.
 * <p>
 * The top row describes how the template is handed out, the middle row where copies of it are built. Every
 * change is written to the template's file and reloaded straight away, so there is no {@code /rm reload}
 * step and no way for the menu and the file to disagree. Mines already claimed keep what they were built
 * with - an edit here only changes what the next claim gets.
 */
public class PrivateMineTemplateGUI {

    private static final Map<UUID, PrivateMineTemplateGUI> inventories = new HashMap<>();

    private static final int SLOT_INFO = 4;

    //how the template is handed out
    private static final int SLOT_DISPLAY_NAME = 9;
    private static final int SLOT_ICON = 10;
    private static final int SLOT_DESCRIPTION = 11;
    private static final int SLOT_PERMISSION = 12;
    private static final int SLOT_COST = 13;
    private static final int SLOT_RENEW_COST = 14;
    private static final int SLOT_LIFECYCLE = 15;
    private static final int SLOT_DURATION = 16;
    private static final int SLOT_TRUSTED_LIMIT = 17;

    //where copies of it go
    private static final int SLOT_ORIGIN = 27;
    private static final int SLOT_SPACING_X = 28;
    private static final int SLOT_SPACING_Z = 29;
    private static final int SLOT_PER_ROW = 30;
    private static final int SLOT_PLATFORM = 31;
    private static final int SLOT_SHELL = 32;

    private static final int SLOT_BACK = 36;
    private static final int SLOT_DELETE = 44;

    private static final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");

    /**
     * The setting each slot edits. Dropping (Q) on one takes its key out of the file, which is what puts
     * the template back on its own default - so reset needs nothing but this map.
     */
    private static final Map<Integer, String> KEYS = new HashMap<>();

    static {
        KEYS.put(SLOT_DISPLAY_NAME, "display-name");
        KEYS.put(SLOT_ICON, "icon");
        KEYS.put(SLOT_DESCRIPTION, "description");
        KEYS.put(SLOT_PERMISSION, "permission");
        KEYS.put(SLOT_COST, "cost");
        KEYS.put(SLOT_RENEW_COST, "renew-cost");
        KEYS.put(SLOT_LIFECYCLE, "lifecycle");
        KEYS.put(SLOT_DURATION, "duration");
        KEYS.put(SLOT_TRUSTED_LIMIT, "trusted-limit");
        KEYS.put(SLOT_ORIGIN, "placement.origin");
        KEYS.put(SLOT_SPACING_X, "placement.spacing-x");
        KEYS.put(SLOT_SPACING_Z, "placement.spacing-z");
        KEYS.put(SLOT_PER_ROW, "placement.per-row");
        KEYS.put(SLOT_PLATFORM, "placement.platform-width");
        KEYS.put(SLOT_SHELL, "placement.shell-schematic");
        //deliberately not SLOT_DELETE: Q there confirms the deletion
    }

    private final Inventory inv;
    private final UUID uuid;
    private final RealMines rm;

    /**
     * Held by id, not by object: every edit reloads the template, so the old instance is stale at once.
     */
    private final String templateID;

    public PrivateMineTemplateGUI(final RealMines rm, final Player as, final String templateID) {
        this.rm = rm;
        this.uuid = as.getUniqueId();
        this.templateID = templateID;
        this.inv = Bukkit.getServer().createInventory(null, 45, Text.color("&f&lTemplate &8| &9&l" + templateID));

        this.load();
        this.register();
    }

    private PrivateMineTemplate template() {
        return this.rm.getPrivateMinesManager().getTemplate(this.templateID);
    }

    public void load() {
        this.inv.clear();

        final PrivateMineTemplate template = this.template();
        if (template == null) {
            return;
        }

        for (int i = 0; i < this.inv.getSize(); ++i) {
            this.inv.setItem(i, placeholder);
        }

        final PrivateMineTemplate.Placement placement = template.getPlacement();
        final Player viewer = Bukkit.getPlayer(this.uuid);

        this.inv.setItem(SLOT_INFO, this.info(template));

        this.inv.setItem(SLOT_DISPLAY_NAME, Items.createItem(Material.NAME_TAG, 1, "&eDisplay name",
                resettable(Arrays.asList("&7Now: &r" + template.getRawDisplayName(),
                        "&7Shown as: &r" + template.getDisplayNameFor(viewer == null ? "Player" : viewer.getName()),
                        "", "&f%player% &7becomes the owner's name.",
                        "&fClick &7and type a new one in chat."), "&r" + PrivateMineTemplate.DEFAULT_DISPLAY_NAME)));

        this.inv.setItem(SLOT_ICON, Items.createItem(template.getIcon(), 1, "&eIcon",
                resettable(Arrays.asList("&7Now: &f" + template.getIcon().name(),
                        "", "&fClick &7to pick another."), PrivateMineTemplate.DEFAULT_ICON.name())));

        final List<String> description = new ArrayList<>();
        description.add("&7Shown under the icon in the menu:");
        if (template.getDescription().isEmpty()) {
            description.add(" &8(empty)");
        } else {
            template.getDescription().forEach(line -> description.add(" &r" + line));
        }
        description.add("");
        description.add("&fClick &7to add a line.");
        description.add("&fDrop (Q) &7to reset to &fno description");
        this.inv.setItem(SLOT_DESCRIPTION, Items.createItem(Material.WRITABLE_BOOK, 1, "&eDescription", description));

        this.inv.setItem(SLOT_PERMISSION, Items.createItem(Material.PAPER, 1, "&ePermission",
                resettable(Arrays.asList("&7Now: &f" + (template.hasPermission() ? template.getPermission() : "none, anybody may claim it"),
                        "", "&fClick &7and type one in chat."), "none, anybody may claim it")));

        this.inv.setItem(SLOT_COST, Items.createItem(Material.GOLD_INGOT, 1, "&eCost",
                resettable(Arrays.asList("&7Now: &f" + (template.getCost() <= 0 ? "free" : String.valueOf(template.getCost())),
                        "&7Charged once, when the mine is claimed.",
                        "", "&fClick &7and type an amount in chat."), "free")));

        this.inv.setItem(SLOT_RENEW_COST, Items.createItem(Material.GOLD_NUGGET, 1, "&eRenew cost",
                resettable(Arrays.asList("&7Now: &f" + (template.getRenewCost() <= 0 ? "free" : String.valueOf(template.getRenewCost())),
                        "&7Charged by &f/pmine extend&7, which only",
                        "&7time limited mines can use.",
                        "", "&fClick &7and type an amount in chat."), "free")));

        this.inv.setItem(SLOT_LIFECYCLE, Items.createItem(Material.CLOCK, 1, "&eLifetime",
                resettable(Arrays.asList("&7Now: &f" + template.getLifecycle().name(),
                        "&f" + PrivateMineData.Lifecycle.PERSISTENT.name() + " &7- until it is given up",
                        "&f" + PrivateMineData.Lifecycle.TIME_LIMITED.name() + " &7- until its duration runs out",
                        "&f" + PrivateMineData.Lifecycle.SESSION.name() + " &7- until the owner logs out",
                        "", "&fClick &7to switch to the next one."), PrivateMineData.Lifecycle.PERSISTENT.name())));

        this.inv.setItem(SLOT_DURATION, Items.createItem(Material.REPEATER, 1, "&eDuration",
                resettable(Arrays.asList("&7Now: &f" + PrivateMineCMD.formatTime(template.getDuration()),
                        template.getLifecycle() == PrivateMineData.Lifecycle.TIME_LIMITED
                                ? "&7How long a claimed mine lasts."
                                : "&8Only used by " + PrivateMineData.Lifecycle.TIME_LIMITED.name() + " templates.",
                        "", "&fClick &7and type the seconds in chat."),
                PrivateMineCMD.formatTime(PrivateMineTemplate.DEFAULT_DURATION))));

        this.inv.setItem(SLOT_TRUSTED_LIMIT, Items.createItem(Material.PLAYER_HEAD, 1, "&eTrusted limit",
                resettable(Arrays.asList("&7Now: &f" + template.getTrustedLimit() + "&7/&f" + PrivateMineTemplate.MAX_TRUSTED,
                        "&7How many others an owner may let in.",
                        "", "&fClick &7for one more, &fright-click &7for one less."),
                String.valueOf(PrivateMineTemplate.DEFAULT_TRUSTED_LIMIT))));

        this.inv.setItem(SLOT_ORIGIN, Items.createItem(Material.COMPASS, 1, "&bGrid origin",
                resettable(Arrays.asList("&7Now: &f" + placement.getOriginX() + ";" + placement.getOriginY() + ";" + placement.getOriginZ(),
                        "&7The lowest corner of the first copy.",
                        "", "&fClick &7to use where you are standing.",
                        "&fRight-click &7and type &fx;y;z &7in chat."), PrivateMineTemplate.DEFAULT_ORIGIN)));

        this.inv.setItem(SLOT_SPACING_X, Items.createItem(Material.RAIL, 1, "&bSpacing X",
                resettable(Arrays.asList("&7Now: &f" + placement.getSpacingX(),
                        "&7How far apart copies sit along X.",
                        "&7Smallest that fits: &f" + minimumSpacing(template, true),
                        "", "&fClick &7and type a number in chat.",
                        "&fRight-click &7to use the smallest that fits."),
                String.valueOf(PrivateMineTemplate.DEFAULT_SPACING))));

        this.inv.setItem(SLOT_SPACING_Z, Items.createItem(Material.POWERED_RAIL, 1, "&bSpacing Z",
                resettable(Arrays.asList("&7Now: &f" + placement.getSpacingZ(),
                        "&7How far apart copies sit along Z.",
                        "&7Smallest that fits: &f" + minimumSpacing(template, false),
                        "", "&fClick &7and type a number in chat.",
                        "&fRight-click &7to use the smallest that fits."),
                String.valueOf(PrivateMineTemplate.DEFAULT_SPACING))));

        this.inv.setItem(SLOT_PER_ROW, Items.createItem(Material.LADDER, 1, "&bCopies per row",
                resettable(Arrays.asList("&7Now: &f" + placement.getPerRow(),
                        "&7How many copies a row holds before",
                        "&7the grid wraps onto the next one.",
                        "", "&fClick &7and type a number in chat."),
                String.valueOf(PrivateMineTemplate.DEFAULT_PER_ROW))));

        this.inv.setItem(SLOT_PLATFORM, Items.createItem(PrivateMinePlatform.walkwayMaterial(), 1, "&bPlatform width",
                resettable(Arrays.asList("&7Now: &f" + (placement.hasPlatform() ? placement.getPlatformWidth() + " blocks" : "no platform"),
                        "&7The walkway around each copy, fenced",
                        "&7with barriers so nobody falls out.",
                        "&7Paved with &f" + PrivateMinePlatform.walkwayMaterial().name() + "&7, set in",
                        "&fprivate-mines/config.yml&7.",
                        "", "&fClick &7for one wider, &fright-click &7for one narrower."),
                PrivateMinePlatform.DEFAULT_WIDTH + " blocks")));

        this.inv.setItem(SLOT_SHELL, Items.createItem(Material.MAP, 1, "&bShell schematic",
                resettable(Arrays.asList("&7Now: &f" + (placement.hasShellSchematic() ? placement.getShellSchematic() : "none"),
                        "&7Pasted at every slot for walls and",
                        "&7decoration, on top of the platform.",
                        "&7Read from the &fschematics &7folder.",
                        "", "&fClick &7and type a file name in chat."), "nothing pasted")));

        this.inv.setItem(SLOT_BACK, Items.createItem(Material.ACACIA_DOOR, 1, "&cBack"));

        this.inv.setItem(SLOT_DELETE, Items.createItem(Material.BARRIER, 1, "&cDelete this template",
                Arrays.asList("&7Claimed mines are left alone, but",
                        "&7nobody can claim a new one after this.",
                        "&cThis cannot be undone.", "", "&fPress &cQ &fto confirm.")));
    }

    /**
     * The lore of an editable setting, with the line that says what dropping it puts back.
     */
    private static List<String> resettable(final List<String> lore, final String def) {
        final List<String> out = new ArrayList<>(lore);
        out.add("&fDrop (Q) &7to reset to &f" + def);
        return out;
    }

    private ItemStack info(final PrivateMineTemplate template) {
        final List<String> lore = new ArrayList<>();
        lore.add("&7Taken from: &f" + (template.getSourceMine().isEmpty() ? "unknown" : template.getSourceMine()));
        lore.add("&7Type: &f" + template.getMineType().name());
        lore.add("&7Resets every: &f" + PrivateMineCMD.formatTime(template.getResetTime()));

        final int[] bounds = template.getSnapshotBounds();
        lore.add("&7Size: &f" + (bounds == null ? "unknown"
                : (bounds[3] - bounds[0] + 1) + "x" + (bounds[4] - bounds[1] + 1) + "x" + (bounds[5] - bounds[2] + 1)));
        lore.add("&7Claimed: &f" + this.rm.getPrivateMinesManager().getPrivateMines().stream()
                .filter(mine -> mine.getPrivateData().getTemplate().equalsIgnoreCase(template.getID())).count());

        //the same check the console prints at load and every claim refuses on, so it is fixable from here
        final List<String> problems = template.validate();
        lore.add("");
        if (problems.isEmpty()) {
            lore.add("&aReady to hand out.");
        } else {
            lore.add("&cNobody can claim this yet:");
            problems.forEach(problem -> lore.add(" &c- &f" + problem));
        }

        lore.add("");
        lore.add("&8Retake the blocks with /pmine template update");

        return Items.createItem(template.getIcon(), 1, "&b" + template.getID(), lore);
    }

    /**
     * The smallest spacing {@link PrivateMineTemplate#validate()} accepts: the mine, the platform on both
     * sides of it, and one block so two copies never touch.
     */
    private static int minimumSpacing(final PrivateMineTemplate template, final boolean alongX) {
        final int[] bounds = template.getSnapshotBounds();
        if (bounds == null) {
            return 0;
        }
        final int size = alongX ? bounds[3] - bounds[0] + 1 : bounds[5] - bounds[2] + 1;
        return size + template.getPlacement().getPlatformMargin() * 2 + 1;
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(final InventoryClickEvent e) {
                final HumanEntity clicker = e.getWhoClicked();
                if (!(clicker instanceof Player) || e.getCurrentItem() == null) {
                    return;
                }

                final PrivateMineTemplateGUI current = inventories.get(clicker.getUniqueId());
                if (current == null || e.getInventory().getHolder() != current.getInventory().getHolder()) {
                    return;
                }

                e.setCancelled(true);
                final Player p = (Player) clicker;

                //the template can be deleted, by another admin or by hand, while this menu sits open
                final PrivateMineTemplate template = current.template();
                if (template == null) {
                    p.closeInventory();
                    TranslatableLine.PRIVATE_MINE_TEMPLATE_NOT_FOUND
                            .setV1(ReplacableVar.TEMPLATE.eq(current.templateID)).send(p);
                    return;
                }

                final PrivateMineTemplate.Placement placement = template.getPlacement();

                //Q on any setting puts it back to the default, which is just its key leaving the file.
                //Checked before the switch so every setting resets the same way, and so nothing has to
                //remember what its own default is
                if (e.getClick() == ClickType.DROP) {
                    final String reset = KEYS.get(e.getRawSlot());
                    if (reset != null) {
                        current.set(reset, null);
                        return;
                    }
                }

                switch (e.getRawSlot()) {
                    case SLOT_BACK:
                        p.closeInventory();
                        new PrivateMineTemplatesGUI(current.rm, p).openInventory(p);
                        return;

                    case SLOT_DELETE:
                        //a misclick here would take the template away, so make it a deliberate one
                        if (e.getClick() != ClickType.DROP) {
                            return;
                        }
                        p.closeInventory();
                        if (current.rm.getPrivateMinesManager().deleteTemplate(current.templateID)) {
                            TranslatableLine.PRIVATE_MINE_TEMPLATE_DELETED
                                    .setV1(ReplacableVar.TEMPLATE.eq(current.templateID)).send(p);
                        } else {
                            TranslatableLine.PRIVATE_MINE_TEMPLATE_NOT_FOUND
                                    .setV1(ReplacableVar.TEMPLATE.eq(current.templateID)).send(p);
                        }
                        return;

                    case SLOT_DISPLAY_NAME:
                        ask(current, p, "display-name", input -> input);
                        return;

                    case SLOT_ICON:
                        //closed first, because openInventory writes into a chest window that is already
                        //open and the picker is a row taller than this menu, which that refuses. The
                        //picker is then built inside the task, not before it: closing this menu fires a
                        //close event that every menu listens to, and it would drop a picker that already
                        //existed, leaving it open but unregistered - which is a menu you can take from.
                        p.closeInventory();
                        Bukkit.getScheduler().runTask(current.rm.getPlugin(), () ->
                                new MaterialPickerGUI(p, Text.color("&9Template icon"),
                                        MaterialPickerGUI.MaterialLists.ALL_MATERIALS, material -> {
                                    if (material != null) {
                                        current.set("icon", material.name());
                                    }
                                    reopen(current, p);
                                }).openInventory(p));
                        return;

                    case SLOT_DESCRIPTION:
                        ask(current, p, "description", input -> {
                            //read again: the admin may have been typing for a while
                            final PrivateMineTemplate fresh = current.template();
                            if (fresh == null) {
                                return null;
                            }
                            final List<String> lines = new ArrayList<>(fresh.getDescription());
                            lines.add(input);
                            return lines;
                        });
                        return;

                    case SLOT_PERMISSION:
                        ask(current, p, "permission", input -> input);
                        return;

                    case SLOT_COST:
                        askNumber(current, p, "cost", false);
                        return;

                    case SLOT_RENEW_COST:
                        askNumber(current, p, "renew-cost", false);
                        return;

                    case SLOT_LIFECYCLE: {
                        final PrivateMineData.Lifecycle[] all = PrivateMineData.Lifecycle.values();
                        current.set("lifecycle", all[(template.getLifecycle().ordinal() + 1) % all.length].name());
                        return;
                    }

                    case SLOT_DURATION:
                        askNumber(current, p, "duration", true);
                        return;

                    case SLOT_TRUSTED_LIMIT:
                        //clamped the same way the template reads it, so the menu can't show a value it
                        //would then ignore
                        current.set("trusted-limit", Math.max(0, Math.min(PrivateMineTemplate.MAX_TRUSTED,
                                template.getTrustedLimit() + (e.isRightClick() ? -1 : 1))));
                        return;

                    case SLOT_ORIGIN:
                        if (e.isRightClick()) {
                            ask(current, p, "placement.origin", input -> {
                                if (PrivateMineTemplate.parsePos(input) == null) {
                                    Text.send(p, "&cThat isn't a position. Type it as &fx;y;z&c.");
                                    return null;
                                }
                                return input;
                            });
                            return;
                        }
                        final Location at = p.getLocation();
                        current.set("placement.origin", at.getBlockX() + ";" + at.getBlockY() + ";" + at.getBlockZ());
                        return;

                    case SLOT_SPACING_X:
                        if (e.isRightClick()) {
                            current.set("placement.spacing-x", minimumSpacing(template, true));
                            return;
                        }
                        askNumber(current, p, "placement.spacing-x", true);
                        return;

                    case SLOT_SPACING_Z:
                        if (e.isRightClick()) {
                            current.set("placement.spacing-z", minimumSpacing(template, false));
                            return;
                        }
                        askNumber(current, p, "placement.spacing-z", true);
                        return;

                    case SLOT_PER_ROW:
                        askNumber(current, p, "placement.per-row", true);
                        return;

                    case SLOT_PLATFORM:
                        current.set("placement.platform-width", Math.max(0, Math.min(PrivateMinePlatform.MAX_WIDTH,
                                placement.getPlatformWidth() + (e.isRightClick() ? -1 : 1))));
                        return;

                    case SLOT_SHELL:
                        ask(current, p, "placement.shell-schematic", input -> input);
                        return;

                    default:
                        break;
                }
            }

            /**
             * Asks for a line of chat and writes whatever {@code value} makes of it. Returning null from
             * it leaves the template alone, which is how a rejected input backs out.
             */
            private void ask(final PrivateMineTemplateGUI current, final Player p, final String key,
                             final Editor value) {
                new PlayerInput(true, p, input -> {
                    final Object written = value.of(input);
                    if (written != null) {
                        current.rm.getPrivateMinesManager().editTemplate(current.template(), key, written);
                    }
                    reopen(current, p);
                }, input -> reopen(current, p));
            }

            private void askNumber(final PrivateMineTemplateGUI current, final Player p, final String key,
                                   final boolean whole) {
                ask(current, p, key, input -> {
                    try {
                        final double parsed = Double.parseDouble(input.trim());
                        if (parsed < 0D) {
                            Text.send(p, "&cThat has to be zero or more.");
                            return null;
                        }
                        return whole ? (Object) (long) parsed : (Object) parsed;
                    } catch (final NumberFormatException ex) {
                        Text.send(p, "&f" + input + " &cisn't a number.");
                        return null;
                    }
                });
            }

            private void reopen(final PrivateMineTemplateGUI current, final Player p) {
                //closed first because openInventory reuses a chest window that is already open, and the
                //material picker's is a row taller than this one
                p.closeInventory();
                final PrivateMineTemplateGUI gui = new PrivateMineTemplateGUI(current.rm, p, current.templateID);
                Bukkit.getScheduler().runTask(current.rm.getPlugin(), () -> gui.openInventory(p));
            }

            @EventHandler
            public void onClose(final InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    final PrivateMineTemplateGUI gui = inventories.get(e.getPlayer().getUniqueId());
                    if (gui != null) {
                        gui.unregister();
                    }
                }
            }
        };
    }

    /**
     * Turns a line of chat into the value to write, or null to write nothing.
     */
    private interface Editor {
        Object of(String input);
    }

    /**
     * Writes one setting and redraws, so the menu always shows what the file now says.
     */
    private void set(final String key, final Object value) {
        this.rm.getPrivateMinesManager().editTemplate(this.template(), key, value);
        this.load();
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
