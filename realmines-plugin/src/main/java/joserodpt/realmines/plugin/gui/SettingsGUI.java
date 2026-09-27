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

import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.utils.DialogForm;
import joserodpt.realmines.api.utils.DialogMenu;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.PlayerInput;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SettingsGUI {

    private static Map<UUID, SettingsGUI> inventories = new HashMap<>();
    private Inventory inv;
    final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, TranslatableLine.GUI_CLOSE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Close.Description"));
    private final UUID uuid;
    private RealMines rm;

    public enum Setting {REALM, PLAYERS}

    private Setting def = Setting.REALM;

    private static final String TITLE = "&f&lReal&9&lMines &8| &fSettings";

    /** One page of the settings dialog: the fields it shows, and how it writes them back. */
    private static final class Category {
        private final String name;
        private final String tooltip;
        private final Consumer<DialogForm> fields;
        private final BiConsumer<Player, DialogForm.Answers> save;

        private Category(final String name, final String tooltip, final Consumer<DialogForm> fields,
                         final BiConsumer<Player, DialogForm.Answers> save) {
            this.name = name;
            this.tooltip = tooltip;
            this.fields = fields;
            this.save = save;
        }
    }

    /** Every setting in config.yml, by what it is about. Keys are paths under {@code RealMines.}. */
    private static final List<Category> CATEGORIES = Arrays.asList(
            new Category("&eGeneral", "&7Prefix, messages and menus", form -> {
                form.text("Prefix", "&ePlugin prefix", RMConfig.file().getString("RealMines.Prefix"), 64);
                toggle(form, "actionbarMessages", "Action bar messages");
                toggle(form, "useButtonGUIForPercentages", "Use the button selector for percentages");
                toggle(form, "useDialogs", "Use dialogs &7(off: chat and inventory menus)");
            }, (p, answers) -> {
                RMConfig.file().set("RealMines.Prefix", answers.text("Prefix", RMConfig.file().getString("RealMines.Prefix")));
                saveToggles(answers, "actionbarMessages", "useButtonGUIForPercentages", "useDialogs");
            }),
            new Category("&bPlayers", "&7Teleporting, mined items and the default location", form -> {
                toggle(form, "teleportPlayers", "Teleport players out of a mine when it resets");
                toggle(form, "teleportMessage", "Tell players when they are teleported");
                toggle(form, "sendMinedItemsToInventory", "Send mined items straight to the inventory");
                form.text("Default-Location", "&eWhere players are sent when a mine goes &7(world;x;y;z;yaw;pitch, empty for spawn)",
                                RMConfig.file().getString(RMConfig.DEFAULT_LOCATION, ""), 256)
                        .toggle("Default-Location-Here", "&eSet the default location to where I am standing", false);
            }, (p, answers) -> {
                saveToggles(answers, "teleportPlayers", "teleportMessage", "sendMinedItemsToInventory");
                if (answers.toggle("Default-Location-Here", false)) {
                    RMConfig.file().set(RMConfig.DEFAULT_LOCATION, locationOf(p));
                } else {
                    RMConfig.file().set(RMConfig.DEFAULT_LOCATION,
                            answers.text("Default-Location", RMConfig.file().getString(RMConfig.DEFAULT_LOCATION, "")).trim());
                }
            }),
            new Category("&cResets", "&7When mines reset and who hears about it", form -> {
                form.text("announceTimes", "&eSeconds before a reset to announce it &7(comma separated)",
                        String.join(", ", RMConfig.file().getStringList("RealMines.announceTimes")), 256);
                toggle(form, "broadcastResetMessageOnlyInWorld", "Broadcast reset messages only in the mine's world");
                toggle(form, "resetMinesWhenNoPlayers", "Reset mines with no players online");
                toggle(form, "disableMineResetOnServerStart", "Don't reset mines when the server starts");
                toggle(form, "disableMineClearingWhenDeleting", "Don't clear a mine's blocks when deleting it");
            }, (p, answers) -> {
                //whatever in the list is a number, so a stray word does not throw the rest away
                final String announce = answers.text("announceTimes", null);
                if (announce != null) {
                    final List<Integer> times = new ArrayList<>();
                    for (final String part : announce.split("[,\\s]+")) {
                        try {
                            times.add(Integer.parseInt(part.trim()));
                        } catch (final NumberFormatException ignored) {
                            //not a number of seconds
                        }
                    }
                    RMConfig.file().set("RealMines.announceTimes", times);
                }
                saveToggles(answers, "broadcastResetMessageOnlyInWorld", "resetMinesWhenNoPlayers",
                        "disableMineResetOnServerStart", "disableMineClearingWhenDeleting");
            }),
            new Category("&6Block Placement", "&7WorldEdit, schematics and farms", form -> {
                toggle(form, "useWorldEditForBlockPlacement", "Use WorldEdit to place blocks");
                toggle(form, "ignoreAirBlocksSchematicPasting", "Skip air blocks when pasting schematics");
                toggle(form, "placeFarmLandBelowCrop", "Place farmland below crops");
            }, (p, answers) -> saveToggles(answers, "useWorldEditForBlockPlacement", "ignoreAirBlocksSchematicPasting",
                    "placeFarmLandBelowCrop")),
            new Category("&aStats", "&7Mining stats and the leaderboards", form -> {
                toggle(form, "Stats.Enabled", "Track mining stats");
                form.slider("Stats.Leaderboard-Size", "&eLeaderboard size", 1, 100, 1,
                                RMConfig.file().getInt("RealMines.Stats.Leaderboard-Size", 28))
                        .slider("Stats.Flush-Interval-Seconds", "&eSeconds between stats saves &7(after a restart)", 5, 600, 5,
                                RMConfig.file().getInt("RealMines.Stats.Flush-Interval-Seconds", 60));
            }, (p, answers) -> {
                saveToggles(answers, "Stats.Enabled");
                for (final String number : new String[]{"Stats.Leaderboard-Size", "Stats.Flush-Interval-Seconds"}) {
                    final String path = "RealMines." + number;
                    RMConfig.file().set(path, answers.number(number, RMConfig.file().getInt(path)));
                }
            }));

    private static void toggle(final DialogForm form, final String key, final String label) {
        form.toggle(key, "&e" + label, RMConfig.file().getBoolean("RealMines." + key));
    }

    private static void saveToggles(final DialogForm.Answers answers, final String... keys) {
        for (final String key : keys) {
            final String path = "RealMines." + key;
            RMConfig.file().set(path, answers.toggle(key, RMConfig.file().getBoolean(path)));
        }
    }

    /** The same format {@link RMConfig#setDefaultLocation} writes, set here so it is saved with the rest. */
    private static String locationOf(final Player p) {
        final Location loc = p.getLocation();
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";"
                + loc.getYaw() + ";" + loc.getPitch();
    }

    /**
     * Opens the settings: on servers that have dialogs, a menu of the categories above, each its own
     * dialog; the inventory editor everywhere else.
     */
    public static void open(final Player p, final RealMines rm) {
        final DialogMenu menu = new DialogMenu(TITLE, "")
                .icon(Material.COMMAND_BLOCK)
                .close("&cClose");
        for (final Category category : CATEGORIES) {
            menu.option(category.name, category.tooltip, () -> openCategory(p, rm, category));
        }

        if (!menu.open(p, () -> { }, () -> openInventoryEditor(p, rm))) {
            openInventoryEditor(p, rm);
        }
    }

    /** One category's dialog. Saving and going back both return to the menu. */
    private static void openCategory(final Player p, final RealMines rm, final Category category) {
        final DialogForm form = new DialogForm(TITLE + " &8> " + category.name,
                "&7Changes are written to config.yml when you save.");
        category.fields.accept(form);
        form.buttons("&aSave", "&7Back");

        final boolean shown = form.open(p, answers -> {
            category.save.accept(p, answers);
            RMConfig.save();
            TranslatableLine.SYSTEM_SETTINGS_SAVED.send(p);
            open(p, rm);
        }, () -> open(p, rm), () -> openInventoryEditor(p, rm));

        if (!shown) {
            openInventoryEditor(p, rm);
        }
    }

    private static void openInventoryEditor(final Player p, final RealMines rm) {
        new SettingsGUI(p, rm).openInventory(p);
    }

    public SettingsGUI(Player as, RealMines rm) {
        this.rm = rm;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&f&lReal&9&lMines &8| Settings"));

        fillGUI();
    }

    public void fillGUI() {
        this.inv.clear();

        for (int number : new int[]{0, 1, 2, 9, 11, 18, 20, 27, 29, 36, 38, 45, 46, 47}) {
            this.inv.setItem(number, Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, ""));
        }

        //selection items
        this.inv.setItem(10, Items.createItem(Material.ENDER_CHEST, 1, Text.pluginPrefix));
        this.inv.setItem(19, Items.createItem(Material.PLAYER_HEAD, 1, "&b&lPlayers"));

        switch (def) {
            case REALM:
                this.inv.setItem(13, Items.createItem(Material.WRITABLE_BOOK, 1, "&ePlugin Prefix", Arrays.asList("&fCurrent: &r" + Text.getPrefix(), "", "&fClick here to change the plugin's prefix.")));
                this.inv.setItem(14, Items.createItem(Material.GRASS_BLOCK, 1, "&ePlace Farm Land Below Crop " + (RMConfig.file().getBoolean("RealMines.placeFarmLandBelowCrop") ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fClick here to toggle this setting.")));
                this.inv.setItem(15, Items.createItem(Material.OAK_SIGN, 1, "&eBroadcast Reset Message Only In World " + (RMConfig.file().getBoolean("RealMines.broadcastResetMessageOnlyInWorld") ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fClick here to toggle this setting.")));
                //here too, since with dialogs off this is the only settings screen left to turn them back on
                this.inv.setItem(16, Items.createItem(Material.COMMAND_BLOCK, 1, "&eUse Dialogs " + (RMConfig.file().getBoolean("RealMines.useDialogs", true) ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fOn servers with dialogs (1.21.6+), ask for input,", "&fthe settings and percentages in dialogs.", "&fClick here to toggle this setting.")));
                break;
            case PLAYERS:
                this.inv.setItem(22, Items.createItem(Material.ENDER_PEARL, 1, "&eTeleport Players " + (RMConfig.file().getBoolean("RealMines.teleportPlayers") ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fClick here to toggle player teleportation.")));
                this.inv.setItem(23, Items.createItem(Material.FILLED_MAP, 1, "&eTeleport Message " + (RMConfig.file().getBoolean("RealMines.teleportMessage") ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fClick here to toggle the teleportation messages.")));
                this.inv.setItem(24, Items.createItem(Material.MAP, 1, "&eAction Bar Messages " + (RMConfig.file().getBoolean("RealMines.actionbarMessages") ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fClick here to toggle action bar messages.")));
                this.inv.setItem(25, Items.createItem(Material.TNT, 1, "&eReset Mines with No Online Players " + (RMConfig.file().getBoolean("RealMines.resetMinesWhenNoPlayers") ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fClick here to toggle this setting.")));
                this.inv.setItem(26, Items.createItem(Material.CHEST, 1, "&eSend Mined Items to Inventory " + (RMConfig.file().getBoolean("RealMines.sendMinedItemsToInventory") ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fClick here to toggle sending the mined", "&fitems directly to the player's inventory.")));

                break;
        }

        this.inv.setItem(37, close);
    }

    public void openInventory(Player target) {
        Inventory inv = getInventory();
        InventoryView openInv = target.getOpenInventory();
        if (openInv != null) {
            Inventory openTop = target.getOpenInventory().getTopInventory();
            if (!inv.equals(openTop)) {
                target.openInventory(inv);
            }

            register();
        }
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                HumanEntity clicker = e.getWhoClicked();
                if (clicker instanceof Player) {
                    Player p = (Player) clicker;
                    UUID uuid = clicker.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        SettingsGUI current = inventories.get(uuid);
                        if (!current.getInventory().equals(e.getInventory())) {
                            return;
                        }

                        e.setCancelled(true);
                        if (e.getCurrentItem() == null) {
                            return;
                        }

                        switch (e.getRawSlot()) {
                            case 16:
                                if (current.def == Setting.REALM) {
                                    toggle("useDialogs", current);
                                }
                                break;
                            case 10:
                                current.def = Setting.REALM;
                                current.fillGUI();
                                break;
                            case 19:
                                current.def = Setting.PLAYERS;
                                current.fillGUI();
                                break;

                            case 13:
                                p.closeInventory();

                                new PlayerInput(false, p, input -> {
                                    RMConfig.file().set("RealMines.Prefix", input);
                                    RMConfig.save();
                                    Text.send(p, "The plugin's prefix is now " + input);

                                    SettingsGUI wv = new SettingsGUI(p, current.rm);
                                    wv.openInventory(p);
                                }, input -> {
                                    SettingsGUI wv = new SettingsGUI(p, current.rm);
                                    wv.openInventory(p);
                                });
                                break;

                            case 14:
                                toggle("placeFarmLandBelowCrop", current);
                                break;

                            case 15:
                                toggle("broadcastResetMessageOnlyInWorld", current);
                                break;

                            case 22:
                                toggle("teleportPlayers", current);
                                break;
                            case 23:
                                toggle("teleportMessage", current);
                                break;
                            case 24:
                                toggle("actionbarMessages", current);
                                break;
                            case 25:
                                toggle("resetMinesWhenNoPlayers", current);
                                break;
                            case 26:
                                toggle("sendMinedItemsToInventory", current);
                                break;

                            case 37:
                                p.closeInventory();
                                RealMinesGUI rv = new RealMinesGUI(p, current.rm);
                                rv.openInventory(p);
                                break;
                        }
                    }
                }
            }

            @EventHandler
            public void onDrag(final InventoryDragEvent e) {
                final SettingsGUI current = inventories.get(e.getWhoClicked().getUniqueId());
                //dragging over this GUI's slots would drop the dragged items into it
                if (current != null && current.getInventory().equals(e.getInventory())) {
                    e.setCancelled(true);
                }
            }

            @EventHandler
            public void onClose(InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    if (e.getInventory() == null) {
                        return;
                    }
                    Player p = (Player) e.getPlayer();
                    UUID uuid = p.getUniqueId();
                    final SettingsGUI current = inventories.get(uuid);
                    if (current != null && e.getInventory().equals(current.getInventory())) {
                        current.unregister();
                    }
                }
            }

            private void toggle(String s, SettingsGUI sg) {
                RMConfig.file().set("RealMines." + s, !RMConfig.file().getBoolean("RealMines." + s));
                RMConfig.save();
                sg.fillGUI();
            }
        };
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