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
import joserodpt.realmines.utils.Items;
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

import java.util.*;

public class RealMinesGUI {

    private static Map<UUID, RealMinesGUI> inventories = new HashMap<>();
    private Inventory inv;
    private final UUID uuid;
    private RealMines rm;

    public RealMinesGUI(Player as, RealMines rm) {
        this.rm = rm;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 27, Text.color("&f&lReal&9&lMines &8Version " + rm.getDescription().getVersion()));

        this.inv.clear();

        //12, 14

        this.inv.setItem(12, Items.createItemLore(Material.DIAMOND_PICKAXE, 1, "&9&lMines",
                Collections.singletonList("&fClick here to view the mines.")));

        this.inv.setItem(14, Items.createItemLore(Material.COMMAND_BLOCK, 1, "&f&lSettings",
                Collections.singletonList("&fClick here to open the plugin settings.")));

        this.inv.setItem(26, Items.createItemLore(Material.OAK_DOOR, 1, "&cClose",
                Collections.singletonList("&fClick here to close this menu.")));

        this.register();
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
                        RealMinesGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);

                        switch (e.getRawSlot()) {
                            case 26:
                                p.closeInventory();
                                break;
                            case 12:
                                p.closeInventory();
                                final MineListGUI v = new MineListGUI(current.rm, p);
                                v.openInventory(p);
                                break;
                            case 14:
                                p.closeInventory();
                                final SettingsGUI v2 = new SettingsGUI(p, current.rm);
                                v2.openInventory(p);
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