package josegamerpt.realmines.gui;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.mines.RMine;
import josegamerpt.realmines.mines.mine.BlockMine;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.utils.Itens;
import josegamerpt.realmines.utils.Text;
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

import java.util.*;

public class MineColorPicker {

    private static final Map<UUID, MineColorPicker> inventories = new HashMap<>();
    private final Inventory inv;
    private final UUID uuid;
    private final RMine mi;
    private final RealMines rm;

    public MineColorPicker(RealMines rm, Player as, RMine mi) {
        this.rm = rm;
        this.uuid = as.getUniqueId();
        inv = Bukkit.getServer().createInventory(null, InventoryType.DROPPER, Text.color(Language.file().getString("GUI.Color-Picker-Name")));

        this.mi = mi;
        load();

        this.register();
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
                        MineColorPicker current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);
                        Player gp = (Player) clicker;

                        switch (e.getRawSlot()) {
                            case 0:
                                current.mi.setColor(BlockMine.Color.RED);
                                break;
                            case 1:
                                current.mi.setColor(BlockMine.Color.GREEN);
                                break;
                            case 2:
                                current.mi.setColor(BlockMine.Color.BLUE);
                                break;
                            case 3:
                                current.mi.setColor(BlockMine.Color.BROWN);
                                break;
                            case 4:
                                current.mi.setColor(BlockMine.Color.GRAY);
                                break;
                            case 5:
                                current.mi.setColor(BlockMine.Color.WHITE);
                                break;
                            case 6:
                                current.mi.setColor(BlockMine.Color.ORANGE);
                                break;
                            case 7:
                                current.mi.setColor(BlockMine.Color.YELLOW);
                                break;
                            case 8:
                                current.mi.setColor(BlockMine.Color.PURPLE);
                                break;
                        }
                        gp.playSound(gp.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 50, 50);
                        gp.closeInventory();
                        current.rm.getMineManager().saveMine(current.mi, BlockMine.Data.COLOR);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm, () -> current.rm.getGUIManager().openMine(current.mi, gp), 2);
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

    public void load() {
        inv.setItem(0, Itens.getMineColor(BlockMine.Color.RED, "&c&lRED", Collections.singletonList("&fClick to set this mine color.")));
        inv.setItem(1, Itens.getMineColor(BlockMine.Color.GREEN, "&2&lGREEN", Collections.singletonList("&fClick to set this mine color.")));
        inv.setItem(2, Itens.getMineColor(BlockMine.Color.BLUE, "&9&lBLUE", Collections.singletonList("&fClick to set this mine color.")));
        inv.setItem(3, Itens.getMineColor(BlockMine.Color.BROWN, "&4&lBROWN", Collections.singletonList("&fClick to set this mine color.")));
        inv.setItem(4, Itens.getMineColor(BlockMine.Color.GRAY, "&7&lGRAY", Collections.singletonList("&fClick to set this mine color.")));
        inv.setItem(5, Itens.getMineColor(BlockMine.Color.WHITE, "&f&lWHITE", Collections.singletonList("&fClick to set this mine color.")));
        inv.setItem(6, Itens.getMineColor(BlockMine.Color.ORANGE, "&6&lORANGE", Collections.singletonList("&fClick to set this mine color.")));
        inv.setItem(7, Itens.getMineColor(BlockMine.Color.YELLOW, "&e&lYELLOW", Collections.singletonList("&fClick to set this mine color.")));
        inv.setItem(8, Itens.getMineColor(BlockMine.Color.PURPLE, "&d&lPURPLE", Collections.singletonList("&fClick to set this mine color.")));
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
