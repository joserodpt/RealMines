package josegamerpt.realmines.gui;

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

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mine.RMine;
import josegamerpt.realmines.util.Items;
import josegamerpt.realmines.util.Text;
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

public class MineColorPicker {

    private static final Map<UUID, MineColorPicker> inventories = new HashMap<>();
    private final Inventory inv;
    private final UUID uuid;
    private final RMine mi;
    private final RealMines rm;
    private final List<String> colorsDescription = Language.file().getStringList("GUI.Items.Colors.Description");

    public MineColorPicker(final RealMines rm, final Player as, final RMine mi) {
        this.rm = rm;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, InventoryType.DROPPER, Text.color(Language.file().getString("GUI.Color-Picker-Name")));

        this.mi = mi;
        this.load();

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
                        final MineColorPicker current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);
                        final Player gp = (Player) clicker;

                        switch (e.getRawSlot()) {
                            case 0:
                                current.mi.setColor(RMine.Color.RED);
                                break;
                            case 1:
                                current.mi.setColor(RMine.Color.GREEN);
                                break;
                            case 2:
                                current.mi.setColor(RMine.Color.BLUE);
                                break;
                            case 3:
                                current.mi.setColor(RMine.Color.BROWN);
                                break;
                            case 4:
                                current.mi.setColor(RMine.Color.GRAY);
                                break;
                            case 5:
                                current.mi.setColor(RMine.Color.WHITE);
                                break;
                            case 6:
                                current.mi.setColor(RMine.Color.ORANGE);
                                break;
                            case 7:
                                current.mi.setColor(RMine.Color.YELLOW);
                                break;
                            case 8:
                                current.mi.setColor(RMine.Color.PURPLE);
                                break;
                        }
                        gp.playSound(gp.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 50, 50);
                        gp.closeInventory();
                        current.rm.getMineManager().saveMine(current.mi, RMine.Data.COLOR);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm, () -> current.rm.getGUIManager().openMine(current.mi, gp), 2);
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

    public void load() {
        this.inv.setItem(0, Items.getMineColor(RMine.Color.RED, Language.file().getString("GUI.Items.Colors.Red"), this.colorsDescription));
        this.inv.setItem(1, Items.getMineColor(RMine.Color.GREEN, Language.file().getString("GUI.Items.Colors.Green"), this.colorsDescription));
        this.inv.setItem(2, Items.getMineColor(RMine.Color.BLUE, Language.file().getString("GUI.Items.Colors.Blue"), this.colorsDescription));
        this.inv.setItem(3, Items.getMineColor(RMine.Color.BROWN, Language.file().getString("GUI.Items.Colors.Brown"), this.colorsDescription));
        this.inv.setItem(4, Items.getMineColor(RMine.Color.GRAY, Language.file().getString("GUI.Items.Colors.Gray"), this.colorsDescription));
        this.inv.setItem(5, Items.getMineColor(RMine.Color.WHITE, Language.file().getString("GUI.Items.Colors.White"), this.colorsDescription));
        this.inv.setItem(6, Items.getMineColor(RMine.Color.ORANGE, Language.file().getString("GUI.Items.Colors.Orange"), this.colorsDescription));
        this.inv.setItem(7, Items.getMineColor(RMine.Color.YELLOW, Language.file().getString("GUI.Items.Colors.Yellow"), this.colorsDescription));
        this.inv.setItem(8, Items.getMineColor(RMine.Color.PURPLE, Language.file().getString("GUI.Items.Colors.Purple"), this.colorsDescription));
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
