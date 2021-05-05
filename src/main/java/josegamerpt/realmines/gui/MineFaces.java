package josegamerpt.realmines.gui;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mines.Mine;
import josegamerpt.realmines.mines.MineCuboid;
import josegamerpt.realmines.utils.Itens;
import josegamerpt.realmines.utils.Text;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MineFaces {

    private static final Map<UUID, MineFaces> inventories = new HashMap<>();
    static ItemStack close = Itens.createItemLore(Material.ACACIA_DOOR, 1, Language.file().getString("GUI.Items.Close.Name"),
            Language.file().getStringList("GUI.Items.Close.Description"));
    private final Inventory inv;
    private final UUID uuid;
    private final Mine m;

    private RealMines rm;

    public MineFaces(RealMines rm, Player as, Mine m) {
        this.rm = rm;
        this.m = m;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(Language.file().getString("GUI.Faces-Name")));

        this.inv.setItem(13, getIcon(m, MineCuboid.CuboidDirection.Up));
        this.inv.setItem(22, getIcon(m, MineCuboid.CuboidDirection.Down));
        this.inv.setItem(21, getIcon(m, MineCuboid.CuboidDirection.East));
        this.inv.setItem(23, getIcon(m, MineCuboid.CuboidDirection.West));
        this.inv.setItem(31, getIcon(m, MineCuboid.CuboidDirection.North));
        this.inv.setItem(40, getIcon(m, MineCuboid.CuboidDirection.South));

        inv.setItem(53, close);
        this.register();
    }

    private static MineCuboid.CuboidDirection getDirection(int rawSlot) {
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
            public void onClick(InventoryClickEvent e) {
                HumanEntity clicker = e.getWhoClicked();
                if (clicker instanceof Player) {
                    if (e.getCurrentItem() == null) {
                        return;
                    }
                    UUID uuid = clicker.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        MineFaces current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);
                        Player p = (Player) clicker;

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
                                        MineFaces mp = new MineFaces(current.rm, p, current.m);
                                        mp.openInventory(p);
                                    }, 3);
                                } else {
                                    p.closeInventory();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm, () -> {
                                        MaterialPicker mp = new MaterialPicker(current.rm, current.m, p, MaterialPicker.PickType.FACE_MATERIAL, getDirection(e.getRawSlot()).name());
                                        mp.openInventory(p);
                                    }, 3);
                                }
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

    private ItemStack getIcon(Mine m, MineCuboid.CuboidDirection sel) {
        if (m.hasFaceBlock(sel)) {
            return Itens.createItemLore(m.getFaceBlock(sel), 1, "&3&L" + sel.name(), Arrays.asList("&7Selected Block: &f" + m.getFaceBlock(sel).name()));
        } else {
            return Itens.createItemLore(Material.BOOK, 1, "&3&L" + sel.name(), Arrays.asList("&7Selected Block: &fNone"));
        }
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
