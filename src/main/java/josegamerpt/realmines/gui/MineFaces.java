package josegamerpt.realmines.gui;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mine.RMine;
import josegamerpt.realmines.mine.component.MineCuboid;
import josegamerpt.realmines.util.Items;
import josegamerpt.realmines.util.Text;
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

public class MineFaces {

    private static final Map<UUID, MineFaces> inventories = new HashMap<>();
    static ItemStack close = Items.createItemLore(Material.ACACIA_DOOR, 1, Language.file().getString("GUI.Items.Close.Name"),
            Language.file().getStringList("GUI.Items.Close.Description"));
    private final Inventory inv;
    private final UUID uuid;
    private final RMine m;

    private final RealMines rm;

    public MineFaces(final RealMines rm, final Player as, final RMine m) {
        this.rm = rm;
        this.m = m;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(Language.file().getString("GUI.Faces-Name")));

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
                        final MineFaces current = inventories.get(uuid);
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
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm, () -> {
                                        final MineFaces mp = new MineFaces(current.rm, p, current.m);
                                        mp.openInventory(p);
                                    }, 3);
                                } else {
                                    p.closeInventory();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm, () -> {
                                        final MaterialPicker mp = new MaterialPicker(current.rm, current.m, p, MaterialPicker.PickType.FACE_MATERIAL, getDirection(e.getRawSlot()).name());
                                        mp.openInventory(p);
                                    }, 3);
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
            for (final String s : Language.file().getStringList("GUI.Faces.Selected-Description")) {
                faceSelectedDesc.add(s.replaceAll("%material%", m.getFaceBlock(sel).name()));
            }
            return Items.createItemLore(m.getFaceBlock(sel), 1, Language.file().getString("GUI.Faces.Name").replaceAll("%face%", sel.name()), faceSelectedDesc);
        } else {
            return Items.createItemLore(Material.BOOK, 1, Language.file().getString("GUI.Faces.Name").replaceAll("%face%", sel.name()), faceSelectedDesc);
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
