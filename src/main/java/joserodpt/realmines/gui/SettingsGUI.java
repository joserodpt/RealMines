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
import joserodpt.realmines.config.Config;
import joserodpt.realmines.utils.Items;
import joserodpt.realmines.utils.PlayerInput;
import joserodpt.realmines.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.util.UUID;

public class SettingsGUI {

    private static Map<UUID, SettingsGUI> inventories = new HashMap<>();
    private Inventory inv;
    private ItemStack close = Items.createItemLore(Material.OAK_DOOR, 1, "&cClose",
            Collections.singletonList("&fClick here to close this menu."));
    private final UUID uuid;
    private RealMines rm;

    public enum Setting {REALM, PLAYERS}

    private Setting def = Setting.REALM;

    public SettingsGUI(Player as, RealMines rm) {
        this.rm = rm;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&f&lReal&9&lMines &8| Settings"));

        fillChest();
    }

    public void fillChest() {
        this.inv.clear();

        for (int number : new int[]{0, 1, 2, 9, 11, 18, 20, 27, 29, 36, 38, 45, 46, 47}) {
            this.inv.setItem(number, Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, ""));
        }

        //selection items
        this.inv.setItem(10, Items.createItem(Material.ENDER_CHEST, 1, Text.pluginPrefix));
        this.inv.setItem(19, Items.createItem(Material.PLAYER_HEAD, 1, "&b&lPlayers"));

        switch (def) {
            case REALM:
                this.inv.setItem(13, Items.createItemLore(Material.WRITABLE_BOOK, 1, "&ePlugin Prefix", Arrays.asList("&fCurrent: &r" + Config.file().getString("RealMines.Prefix"), "", "&fClick here to change the plugin's prefix.")));
                this.inv.setItem(14, Items.createItemLore(Material.GRASS_BLOCK, 1, "&ePlace Farm Land Below Crop " + (Config.file().getBoolean("RealMines.placeFarmLandBelowCrop") ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fClick here to toggle this setting.")));
                this.inv.setItem(15, Items.createItemLore(Material.OAK_SIGN, 1, "&eBroadcast Reset Message Only In World " + (Config.file().getBoolean("RealMines.broadcastResetMessageOnlyInWorld") ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fClick here to toggle this setting.")));
                break;
            case PLAYERS:
                this.inv.setItem(22, Items.createItemLore(Material.ENDER_PEARL, 1, "&eTeleport Players " + (Config.file().getBoolean("RealMines.teleportPlayers") ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fClick here to toggle player teleportation.")));
                this.inv.setItem(23, Items.createItemLore(Material.FILLED_MAP, 1, "&eTeleport Message " + (Config.file().getBoolean("RealMines.teleportMessage") ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fClick here to toggle the teleportation messages.")));
                this.inv.setItem(24, Items.createItemLore(Material.MAP, 1, "&eAction Bar Messages " + (Config.file().getBoolean("RealMines.actionbarMessages") ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fClick here to toggle action bar messages.")));
                this.inv.setItem(25, Items.createItemLore(Material.TNT, 1, "&eReset Mines with No Online Players " + (Config.file().getBoolean("RealMines.resetMinesWhenNoPlayers") ? "&a&lON" : "&c&lOFF"), Arrays.asList("", "&fClick here to toggle this setting.")));

                break;
        }

        this.inv.setItem(37, close);
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
                    if (e.getCurrentItem() == null) {
                        return;
                    }
                    UUID uuid = clicker.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        SettingsGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);

                        switch (e.getRawSlot()) {
                            case 16:
                                p.closeInventory();
                                break;
                            case 10:
                                current.def = Setting.REALM;
                                current.fillChest();
                                break;
                            case 19:
                                current.def = Setting.PLAYERS;
                                current.fillChest();
                                break;

                            case 13:
                                p.closeInventory();

                                new PlayerInput(p, input -> {
                                    Config.file().set("RealMines.Prefix", input);
                                    Config.save();
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

            private void toggle(String s, SettingsGUI sg) {
                Config.file().set("RealMines." + s, !Config.file().getBoolean("RealMines." + s));
                Config.save();
                sg.fillChest();
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