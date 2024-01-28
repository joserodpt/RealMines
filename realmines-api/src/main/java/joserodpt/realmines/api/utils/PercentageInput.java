package joserodpt.realmines.api.utils;

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

import joserodpt.realmines.api.RealMinesAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PercentageInput {

    private static Map<UUID, PercentageInput> inventories = new HashMap<>();
    private Inventory inv;
    private final UUID uuid;
    private int percentage = 0;
    private PercentageInput.InputRunnable acceptTask;
    private JavaPlugin rm;

    public PercentageInput(Player as, JavaPlugin rm, int initialPercentage, final PercentageInput.InputRunnable acceptTask) {
        this.percentage = initialPercentage;
        this.acceptTask = acceptTask;
        this.rm = rm;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, InventoryType.DROPPER, Text.color("&8Percentage Selector | &l" + percentage + "%"));

        List<String> desc = Arrays.asList("&aClick &fto add.","&cQ (Drop) &fto remove.");
        this.inv.setItem(0, Items.createItemLore(Material.EMERALD_ORE, 1, "&f&l1",desc));
        this.inv.setItem(1, Items.createItemLore(Material.EMERALD_ORE, 1, "&f&l2",desc));
        this.inv.setItem(2, Items.createItemLore(Material.EMERALD_ORE, 1, "&f&l5",desc));

        this.inv.setItem(3, Items.createItemLore(Material.EMERALD, 1, "&f&l10",desc));
        this.inv.setItem(4, Items.createItemLore(Material.EMERALD, 1, "&f&l20",desc));
        this.inv.setItem(5, Items.createItemLore(Material.EMERALD, 1, "&f&l30",desc));

        this.inv.setItem(6, Items.createItemLore(Material.EMERALD_BLOCK, 1, "&f&l50",desc));
        this.inv.setItem(7, Items.createItemLore(Material.EMERALD_BLOCK, 1, "&f&l100",desc));

        this.inv.setItem(8, Items.createItem(Material.CHEST_MINECART, 1, "&a&lConfirm"));

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
                        PercentageInput current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);

                        if (e.getRawSlot() == 8) {
                            //rodar ação de sucesso
                            p.closeInventory();
                            Bukkit.getScheduler().scheduleSyncDelayedTask(RealMinesAPI.getInstance().getPlugin(), () -> current.acceptTask.run(current.percentage), 3);
                            return;
                        }

                        if (e.getClick() == ClickType.DROP) {
                            switch (e.getRawSlot()) {
                                case 0:
                                    --current.percentage;
                                    break;
                                case 1:
                                    current.percentage -= 2;
                                    break;
                                case 2:
                                    current.percentage -= 5;
                                    break;
                                case 3:
                                    current.percentage -= 10;
                                    break;
                                case 4:
                                    current.percentage -= 20;
                                    break;
                                case 5:
                                    current.percentage -= 30;
                                    break;
                                case 6:
                                    current.percentage -= 50;
                                    break;
                                case 7:
                                    current.percentage -= 100;
                                    break;
                            }
                            if (current.percentage < 0) {
                                current.percentage = 0;
                            }
                        } else {
                            switch (e.getRawSlot()) {
                                case 0:
                                    ++current.percentage;
                                    break;
                                case 1:
                                    current.percentage += 2;
                                    break;
                                case 2:
                                    current.percentage += 5;
                                    break;
                                case 3:
                                    current.percentage += 10;
                                    break;
                                case 4:
                                    current.percentage += 20;
                                    break;
                                case 5:
                                    current.percentage += 30;
                                    break;
                                case 6:
                                    current.percentage += 50;
                                    break;
                                case 7:
                                    current.percentage += 100;
                                    break;
                            }
                            if (current.percentage > 100) {
                                current.percentage = 100;
                            }
                        }
                        InventoryTitleUpdate.updateInventory(p, Text.color("&8Percentage Selector | &l" + current.percentage + "%"));
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

    @FunctionalInterface
    public interface InputRunnable {
        void run(int percentage);
    }
}