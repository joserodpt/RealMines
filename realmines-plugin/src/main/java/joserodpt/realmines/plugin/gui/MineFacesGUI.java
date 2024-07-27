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
import joserodpt.realmines.api.mine.components.MineCuboid;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.PickType;
import joserodpt.realmines.plugin.RealMines;
import org.bukkit.Bukkit;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MineFacesGUI {

    private static final Map<UUID, MineFacesGUI> inventories = new HashMap<>();
    static final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, TranslatableLine.GUI_CLOSE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Close.Description"));
    private final Inventory inv;
    private final UUID uuid;
    private final RMine m;

    private final RealMines rm;

    public MineFacesGUI(final RealMines rm, final Player as, final RMine m) {
        this.rm = rm;
        this.m = m;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, TranslatableLine.GUI_FACES_NAME.get());

        this.inv.setItem(13, this.getIcon(m, MineCuboid.CuboidDirection.Up));
        this.inv.setItem(22, this.getIcon(m, MineCuboid.CuboidDirection.Down));
        this.inv.setItem(21, this.getIcon(m, MineCuboid.CuboidDirection.East));
        this.inv.setItem(23, this.getIcon(m, MineCuboid.CuboidDirection.West));
        this.inv.setItem(31, this.getIcon(m, MineCuboid.CuboidDirection.North));
        this.inv.setItem(40, this.getIcon(m, MineCuboid.CuboidDirection.South));

        this.inv.setItem(53, close);
        this.register();
    }

    private static MineCuboid.CuboidDirection getDirection(final int rawSlot) {
        switch (rawSlot) {
            case 13:
                return MineCuboid.CuboidDirection.Up;
            case 22:
                return MineCuboid.CuboidDirection.Down;
            case 21:
                return MineCuboid.CuboidDirection.East;
            case 23:
                return MineCuboid.CuboidDirection.West;
            case 31:
                return MineCuboid.CuboidDirection.North;
            case 40:
                return MineCuboid.CuboidDirection.South;
            default:
                return MineCuboid.CuboidDirection.Unknown;
        }
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
                        final MineFacesGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);
                        final Player p = (Player) clicker;

                        switch (e.getRawSlot()) {
                            case 53:
                                p.closeInventory();
                                current.rm.getGUIManager().openMine(current.m, p);
                                break;
                            case 13:
                            case 22:
                            case 21:
                            case 23:
                            case 31:
                            case 40:
                                if (e.getClick() == ClickType.DROP) {
                                    current.m.removeFaceblock(getDirection(e.getRawSlot()));
                                    p.closeInventory();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm.getPlugin(), () -> {
                                        final MineFacesGUI mp = new MineFacesGUI(current.rm, p, current.m);
                                        mp.openInventory(p);
                                    }, 1);
                                } else {
                                    p.closeInventory();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm.getPlugin(), () -> {
                                        final BlockPickerGUI mp = new BlockPickerGUI(current.rm, current.m, p, PickType.FACE_MATERIAL, getDirection(e.getRawSlot()).name());
                                        mp.openInventory(p);
                                    }, 1);
                                }
                        }

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

    private ItemStack getIcon(final RMine m, final MineCuboid.CuboidDirection sel) {
        final List<String> faceSelectedDesc = new ArrayList<>();
        if (!faceSelectedDesc.isEmpty()) faceSelectedDesc.clear();
        if (m.hasFaceBlock(sel)) {
            for (final String s : RMLanguageConfig.file().getStringList("GUI.Faces.Selected-Description")) {
                faceSelectedDesc.add(s.replaceAll("%material%", m.getFaceBlock(sel).name()));
            }
            return Items.createItem(m.getFaceBlock(sel), 1, TranslatableLine.GUI_FACES_ITEM_NAME.setV1(TranslatableLine.ReplacableVar.FACE.eq(sel.name())).get(), faceSelectedDesc);
        } else {
            return Items.createItem(Material.BOOK, 1, TranslatableLine.GUI_FACES_ITEM_NAME.setV1(TranslatableLine.ReplacableVar.FACE.eq(sel.name())).get(), faceSelectedDesc);
        }
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
