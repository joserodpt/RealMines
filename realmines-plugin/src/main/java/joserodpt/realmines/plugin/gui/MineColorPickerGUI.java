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
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.MineColor;
import joserodpt.realmines.plugin.RealMines;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MineColorPickerGUI {

    private static final Map<UUID, MineColorPickerGUI> inventories = new HashMap<>();
    private final Inventory inv;
    private final UUID uuid;
    private final RMine mi;
    private final RealMines rm;
    private final List<String> colorsDescription = RMLanguageConfig.file().getStringList("GUI.Items.Colors.Description");

    public MineColorPickerGUI(final RealMines rm, final Player as, final RMine mi) {
        this.rm = rm;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, InventoryType.DROPPER, TranslatableLine.GUI_COLOR_PICKER_NAME.get());

        this.mi = mi;

        this.inv.setItem(0, MineColor.RED.getItem(TranslatableLine.GUI_COLORS_RED.get(), this.colorsDescription));
        this.inv.setItem(1, MineColor.GREEN.getItem(TranslatableLine.GUI_COLORS_GREEN.get(), this.colorsDescription));
        this.inv.setItem(2, MineColor.BLUE.getItem(TranslatableLine.GUI_COLORS_BLUE.get(), this.colorsDescription));
        this.inv.setItem(3, MineColor.BROWN.getItem(TranslatableLine.GUI_COLORS_BROWN.get(), this.colorsDescription));
        this.inv.setItem(4, MineColor.GRAY.getItem(TranslatableLine.GUI_COLORS_GRAY.get(), this.colorsDescription));
        this.inv.setItem(5, MineColor.WHITE.getItem(TranslatableLine.GUI_COLORS_WHITE.get(), this.colorsDescription));
        this.inv.setItem(6, MineColor.ORANGE.getItem(TranslatableLine.GUI_COLORS_ORANGE.get(), this.colorsDescription));
        this.inv.setItem(7, MineColor.YELLOW.getItem(TranslatableLine.GUI_COLORS_YELLOW.get(), this.colorsDescription));
        this.inv.setItem(8, MineColor.PURPLE.getItem(TranslatableLine.GUI_COLORS_PURPLE.get(), this.colorsDescription));

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
                        final MineColorPickerGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);
                        final Player gp = (Player) clicker;

                        switch (e.getRawSlot()) {
                            case 0:
                                current.mi.setMineColor(MineColor.RED);
                                break;
                            case 1:
                                current.mi.setMineColor(MineColor.GREEN);
                                break;
                            case 2:
                                current.mi.setMineColor(MineColor.BLUE);
                                break;
                            case 3:
                                current.mi.setMineColor(MineColor.BROWN);
                                break;
                            case 4:
                                current.mi.setMineColor(MineColor.GRAY);
                                break;
                            case 5:
                                current.mi.setMineColor(MineColor.WHITE);
                                break;
                            case 6:
                                current.mi.setMineColor(MineColor.ORANGE);
                                break;
                            case 7:
                                current.mi.setMineColor(MineColor.YELLOW);
                                break;
                            case 8:
                                current.mi.setMineColor(MineColor.PURPLE);
                                break;
                        }
                        gp.playSound(gp.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 50, 50);
                        gp.closeInventory();
                        Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm.getPlugin(), () -> current.rm.getGUIManager().openMine(current.mi, gp), 2);
                    }
                }
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
