package josegamerpt.realmines.gui;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mines.RMine;
import josegamerpt.realmines.mines.mine.BlockMine;
import josegamerpt.realmines.utils.Items;
import josegamerpt.realmines.utils.PlayerInput;
import josegamerpt.realmines.utils.Text;
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

import java.util.*;

public class MineResetMenu {

    private static final Map<UUID, MineResetMenu> inventories = new HashMap<>();
    private final Inventory inv;

    private final UUID uuid;
    private final RMine min;
    private final RealMines rm;

    public MineResetMenu(RealMines rm, Player as, RMine m) {
        this.rm = rm;
        this.uuid = as.getUniqueId();
        inv = Bukkit.getServer().createInventory(null, InventoryType.HOPPER, Text.color(Language.file().getString("GUI.Reset-Name").replaceAll("%mine%", m.getDisplayName())));
        this.min = m;

        load(m);

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
                        MineResetMenu current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);
                        Player gp = (Player) clicker;

                        switch (e.getRawSlot()) {
                            case 2:
                                gp.closeInventory();
                                current.rm.getGUIManager().openMine(current.min, gp);
                                break;
                            case 0:
                                switch (e.getClick()) {
                                    case LEFT:
                                        current.min.setResetStatus(BlockMine.Reset.PERCENTAGE, !current.min.isResetBy(BlockMine.Reset.PERCENTAGE));
                                        current.load(current.min);
                                        current.min.saveData(BlockMine.Data.OPTIONS);
                                        break;
                                    case RIGHT:
                                        current.editSetting(current.rm, 0, gp, current.min);
                                        break;
                                }
                                break;
                            case 4:
                                switch (e.getClick()) {
                                    case LEFT:
                                        current.min.setResetStatus(BlockMine.Reset.TIME, !current.min.isResetBy(BlockMine.Reset.TIME));
                                        current.load(current.min);
                                        current.min.saveData(BlockMine.Data.OPTIONS);
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

    public void load(RMine m) {
        inv.clear();
        List<String> percentageOnDesc = new ArrayList<>();
        List<String> percentageOffDesc = new ArrayList<>();
        List<String> timeOnDesc = new ArrayList<>();
        List<String> timeOffDesc = new ArrayList<>();
        for (String s : Language.file().getStringList("GUI.Resets.Percentage-On.Description")) {
            percentageOnDesc.add(s.replaceAll("%value%", "" + m.getResetValue(BlockMine.Reset.PERCENTAGE)));
        }
        for (String s : Language.file().getStringList("GUI.Resets.Percentage-Off.Description")) {
            percentageOffDesc.add(s.replaceAll("%value%", "" + m.getResetValue(BlockMine.Reset.PERCENTAGE)));
        }
        for (String s : Language.file().getStringList("GUI.Resets.Time-On.Description")) {
            timeOnDesc.add(s.replaceAll("%value%", "" + m.getResetValue(BlockMine.Reset.TIME)));
        }
        for (String s : Language.file().getStringList("GUI.Resets.Time-Off.Description")) {
            timeOffDesc.add(s.replaceAll("%value%", "" + m.getResetValue(BlockMine.Reset.TIME)));
        }

        if (m.isResetBy(BlockMine.Reset.PERCENTAGE)) {
            this.inv.setItem(0, Items.createItemLoreEnchanted(Material.BOOK, 1, Language.file().getString("GUI.Resets.Percentage-On.Name"), percentageOnDesc));
        } else {
            this.inv.setItem(0, Items.createItemLore(Material.BOOK, 1, Language.file().getString("GUI.Resets.Percentage-Off.Name"), percentageOffDesc));
        }

        if (m.isResetBy(BlockMine.Reset.TIME)) {
            this.inv.setItem(4, Items.createItemLoreEnchanted(Material.CLOCK, 1, Language.file().getString("GUI.Resets.Time-On.Name"), timeOnDesc));
        } else {
            this.inv.setItem(4, Items.createItemLore(Material.CLOCK, 1, Language.file().getString("GUI.Resets.Time-Off.Name"), timeOffDesc));

        }

        this.inv.setItem(2,
                Items.createItemLore(Material.ACACIA_DOOR, 1, Language.file().getString("GUI.Items.Go-Back.Name"), Language.file().getStringList("GUI.Items.Go-Back.Description")));
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

    protected void editSetting(RealMines rm, int i, Player gp, RMine m) {
        switch (i) {
            case 0:
                new PlayerInput(gp, s -> {
                    int d;
                    try {
                        d = Integer.parseInt(s.replace("%", ""));
                    } catch (Exception ex) {
                        gp.sendMessage(Text.color(Language.file().getString("System.Input-Parse")));
                        editSetting(rm, 0, gp, m);
                        return;
                    }

                    if (d <= 1 || d >= 100) {
                        gp.sendMessage(Text.color(Language.file().getString("System.Input-Limit-Error")));
                        editSetting(rm, 0, gp, m);
                        return;
                    }

                    m.setResetValue(BlockMine.Reset.PERCENTAGE, d);
                    m.saveData(BlockMine.Data.OPTIONS);
                    gp.sendMessage(Text.color(Language.file().getString("System.Percentage-Modified").replaceAll("%value%", "" + d)));

                    MineResetMenu v = new MineResetMenu(rm, gp, m);
                    v.openInventory(gp);
                }, s -> {
                    MineResetMenu v = new MineResetMenu(rm, gp, m);
                    v.openInventory(gp);
                });
                break;
            case 1:
                new PlayerInput(gp, s -> {
                    int d;
                    try {
                        d = Integer.parseInt(s.replace("%", ""));
                    } catch (Exception ex) {
                        gp.sendMessage(Text.color(Language.file().getString("System.Input-Seconds")));
                        editSetting(rm, 1, gp, m);
                        return;
                    }

                    if (d < 1) {
                        gp.sendMessage(Text.color(Language.file().getString("System.Input-Limit-Error-Greater")));
                        editSetting(rm, 1, gp, m);
                        return;
                    }

                    m.setResetValue(BlockMine.Reset.TIME, d);
                    m.saveData(BlockMine.Data.OPTIONS);
                    gp.sendMessage(Text.color(Language.file().getString("System.Time-Modified").replaceAll("%value%", "" + d)));


                    MineResetMenu v = new MineResetMenu(rm, gp, m);
                    v.openInventory(gp);
                }, s -> {
                    MineResetMenu v = new MineResetMenu(rm, gp, m);
                    v.openInventory(gp);
                });
                break;
            default:
                break;
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
