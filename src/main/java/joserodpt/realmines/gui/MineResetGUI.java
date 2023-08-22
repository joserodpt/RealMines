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
import joserodpt.realmines.config.Language;
import joserodpt.realmines.mine.RMine;
import joserodpt.realmines.util.Items;
import joserodpt.realmines.util.PlayerInput;
import joserodpt.realmines.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MineResetGUI {

    private static final Map<UUID, MineResetGUI> inventories = new HashMap<>();
    private final Inventory inv;

    private final UUID uuid;
    private final RMine min;
    private final RealMines rm;

    public MineResetGUI(final RealMines rm, final Player as, final RMine m) {
        this.rm = rm;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, InventoryType.HOPPER, Text.color(Language.file().getString("GUI.Reset-Name").replaceAll("%mine%", m.getDisplayName())));
        this.min = m;

        this.load(m);

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
                        final MineResetGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);
                        final Player gp = (Player) clicker;

                        switch (e.getRawSlot()) {
                            case 2:
                                gp.closeInventory();
                                current.rm.getGUIManager().openMine(current.min, gp);
                                break;
                            case 0:
                                switch (e.getClick()) {
                                    case LEFT:
                                        current.min.setResetStatus(RMine.Reset.PERCENTAGE, !current.min.isResetBy(RMine.Reset.PERCENTAGE));
                                        current.load(current.min);
                                        current.min.saveData(RMine.Data.OPTIONS);
                                        break;
                                    case RIGHT:
                                        current.editSetting(current.rm, 0, gp, current.min);
                                        break;
                                }
                                break;
                            case 4:
                                switch (e.getClick()) {
                                    case LEFT:
                                        current.min.setResetStatus(RMine.Reset.TIME, !current.min.isResetBy(RMine.Reset.TIME));
                                        current.load(current.min);
                                        current.min.saveData(RMine.Data.OPTIONS);
                                        break;
                                    case RIGHT:
                                        current.editSetting(current.rm, 1, gp, current.min);
                                        break;
                                    default:
                                        break;
                                }
                                break;
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

    public void load(final RMine m) {
        this.inv.clear();
        final List<String> percentageOnDesc = new ArrayList<>();
        final List<String> percentageOffDesc = new ArrayList<>();
        final List<String> timeOnDesc = new ArrayList<>();
        final List<String> timeOffDesc = new ArrayList<>();

        for (final String s : Language.file().getStringList("GUI.Resets.Percentage-On.Description")) {
            percentageOnDesc.add(s.replaceAll("%value%", String.valueOf(m.getResetValue(RMine.Reset.PERCENTAGE))));
        }
        for (final String s : Language.file().getStringList("GUI.Resets.Percentage-Off.Description")) {
            percentageOffDesc.add(s.replaceAll("%value%", String.valueOf(m.getResetValue(RMine.Reset.PERCENTAGE))));
        }
        for (final String s : Language.file().getStringList("GUI.Resets.Time-On.Description")) {
            timeOnDesc.add(s.replaceAll("%value%", String.valueOf(m.getResetValue(RMine.Reset.TIME))));
        }
        for (final String s : Language.file().getStringList("GUI.Resets.Time-Off.Description")) {
            timeOffDesc.add(s.replaceAll("%value%", String.valueOf(m.getResetValue(RMine.Reset.TIME))));
        }

        this.inv.setItem(0, m.isResetBy(RMine.Reset.PERCENTAGE)
                ? Items.createItemLoreEnchanted(Material.BOOK, 1, Language.file().getString("GUI.Resets.Percentage-On.Name"), percentageOnDesc)
                : Items.createItemLore(Material.BOOK, 1, Language.file().getString("GUI.Resets.Percentage-Off.Name"), percentageOffDesc));

        this.inv.setItem(4, m.isResetBy(RMine.Reset.TIME)
                ? Items.createItemLoreEnchanted(Material.CLOCK, 1, Language.file().getString("GUI.Resets.Time-On.Name"), timeOnDesc)
                : Items.createItemLore(Material.CLOCK, 1, Language.file().getString("GUI.Resets.Time-Off.Name"), timeOffDesc));

        this.inv.setItem(2,
                Items.createItemLore(Material.ACACIA_DOOR, 1, Language.file().getString("GUI.Items.Go-Back.Name"), Language.file().getStringList("GUI.Items.Go-Back.Description")));
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

    protected void editSetting(final RealMines rm, final int i, final Player p, final RMine m) {
        switch (i) {
            case 0:
                new PlayerInput(p, s -> {
                    final int d;
                    try {
                        d = Integer.parseInt(s.replace("%", ""));
                    } catch (final Exception ex) {
                        Text.send(p, Language.file().getString("System.Input-Parse"));
                        this.editSetting(rm, 0, p, m);
                        return;
                    }

                    if (d < 1 || d > 100) {
                        Text.send(p, Language.file().getString("System.Input-Limit-Error"));
                        this.editSetting(rm, 0, p, m);
                        return;
                    }

                    m.setResetValue(RMine.Reset.PERCENTAGE, d);
                    m.saveData(RMine.Data.OPTIONS);
                    Text.send(p, Language.file().getString("System.Percentage-Modified").replaceAll("%value%", String.valueOf(d)));

                    final MineResetGUI v = new MineResetGUI(rm, p, m);
                    v.openInventory(p);
                }, s -> {
                    final MineResetGUI v = new MineResetGUI(rm, p, m);
                    v.openInventory(p);
                });
                break;
            case 1:
                new PlayerInput(p, s -> {
                    final int d;
                    try {
                        d = Integer.parseInt(s.replace("%", ""));
                    } catch (final Exception ex) {
                        Text.send(p, Language.file().getString("System.Input-Seconds"));
                        this.editSetting(rm, 1, p, m);
                        return;
                    }

                    if (d < 1) {
                        Text.send(p, Language.file().getString("System.Input-Limit-Error-Greater"));
                        this.editSetting(rm, 1, p, m);
                        return;
                    }

                    m.setResetValue(RMine.Reset.TIME, d);
                    m.saveData(RMine.Data.OPTIONS);
                    Text.send(p, Language.file().getString("System.Time-Modified").replaceAll("%value%", String.valueOf(d)));

                    final MineResetGUI v = new MineResetGUI(rm, p, m);
                    v.openInventory(p);
                }, s -> {
                    final MineResetGUI v = new MineResetGUI(rm, p, m);
                    v.openInventory(p);
                });
                break;
            default:
                break;
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
