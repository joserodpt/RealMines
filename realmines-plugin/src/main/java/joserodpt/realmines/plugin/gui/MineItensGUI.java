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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.RMMinesConfig;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.items.farm.MineFarmItem;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.mine.types.BlockMine;
import joserodpt.realmines.api.mine.components.items.MineBlockItem;
import joserodpt.realmines.api.mine.types.farm.FarmItem;
import joserodpt.realmines.api.mine.types.farm.FarmMine;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Pagination;
import joserodpt.realmines.api.utils.PercentageInput;
import joserodpt.realmines.api.utils.PickType;
import joserodpt.realmines.api.utils.PlayerInput;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MineItensGUI {

    private static final Map<UUID, MineItensGUI> inventories = new HashMap<>();
    static ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static ItemStack next = Items.createItemLore(Material.GREEN_STAINED_GLASS, 1, RMLanguageConfig.file().getString("GUI.Items.Next.Name"),
            RMLanguageConfig.file().getStringList("GUI.Items.Next.Description"));
    static ItemStack back = Items.createItemLore(Material.YELLOW_STAINED_GLASS, 1, RMLanguageConfig.file().getString("GUI.Items.Back.Name"),
            RMLanguageConfig.file().getStringList("GUI.Items.Back.Description"));
    static ItemStack close = Items.createItemLore(Material.ACACIA_DOOR, 1, RMLanguageConfig.file().getString("GUI.Items.Close.Name"),
            RMLanguageConfig.file().getStringList("GUI.Items.Close.Description"));
    static ItemStack add = Items.createItemLore(Material.HOPPER, 1, RMLanguageConfig.file().getString("GUI.Items.Add.Name"),
            RMLanguageConfig.file().getStringList("GUI.Items.Add.Description"));
    private final Inventory inv;
    private final UUID uuid;
    private final HashMap<Integer, MineItem> display = new HashMap<>();
    private final RMine m;
    int pageNumber = 0;
    Pagination<MineItem> p;
    private final RealMines rm;

    public MineItensGUI(final RealMines rm, final Player target, final RMine min) {
        this.rm = rm;
        this.uuid = target.getUniqueId();
        this.m = min;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(RMLanguageConfig.file().getString("GUI.Mine-Blocks-Name").replaceAll("%mine%", this.m.getDisplayName())));

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
                        final Player p = (Player) clicker;

                        e.setCancelled(true);

                        final MineItensGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {

                            if (Items.getValidBlocks(current.m.getBlockPickType()).contains(e.getCurrentItem().getType())) {

                                switch (current.m.getType()) {
                                    case BLOCKS:
                                        ((BlockMine) current.m).addItem(new MineBlockItem(e.getCurrentItem().getType()));
                                        break;
                                    case FARM:
                                        FarmItem fi = FarmItem.valueOf(e.getCurrentItem().getType());
                                        if (fi == null) {
                                            return;
                                        }
                                        ((FarmMine) current.m).addFarmItem(new MineFarmItem(fi));
                                        break;
                                }

                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 50, 50);
                                p.closeInventory();
                                Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm.getPlugin(), () -> {
                                    final MineItensGUI v = new MineItensGUI(current.rm, p, current.m);
                                    v.openInventory(p);
                                }, 1);
                            } else {
                                Text.send(clicker, RMLanguageConfig.file().getString("System.Cant-Add-Item"));
                            }

                        } else {
                            switch (e.getRawSlot()) {
                                case 49:
                                    p.closeInventory();
                                    current.rm.getGUIManager().openMine(current.m, p);
                                    break;
                                case 4:
                                    if (current.m.getType() == RMine.Type.SCHEMATIC) { return; }

                                    p.closeInventory();
                                    BlockPickerGUI mp = null;
                                    switch (current.m.getType()) {
                                        case BLOCKS:
                                            mp = new BlockPickerGUI(current.rm, current.m, p, PickType.BLOCK, "");
                                            break;
                                        case FARM:
                                            mp = new BlockPickerGUI(current.rm, current.m, p, PickType.FARM_ITEM, "");
                                            break;
                                    }
                                    if (mp != null)
                                        mp.openInventory(p);
                                    break;
                                case 0:
                                    current.m.setBreakingPermissionOn(!current.m.isBreakingPermissionOn());
                                    current.m.saveData(RMine.Data.SETTINGS);
                                    current.load();
                                    break;
                                case 8:
                                    RMMinesConfig.file().set(current.m.getName() + ".Settings.Discard-Break-Action-Messages", !RMMinesConfig.file().getBoolean(current.m.getName() + ".Settings.Discard-Break-Action-Messages"));
                                    RMMinesConfig.save();
                                    current.load();
                                    break;
                                case 26:
                                case 35:
                                    this.nextPage(current);
                                    p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                    break;
                                case 18:
                                case 27:
                                    this.backPage(current);
                                    p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                    break;
                            }

                            if (current.display.containsKey(e.getRawSlot())) {
                                final MineItem minItem = current.display.get(e.getRawSlot());

                                if (!minItem.isInteractable()) {
                                    return;
                                }

                                switch (e.getClick()) {
                                    case DROP:
                                        if (minItem.isSchematicBlock()) { return; }

                                        // eliminar
                                        switch (current.m.getType()) {
                                            case BLOCKS:
                                                ((BlockMine) current.m).removeMineBlockItem(minItem);
                                                break;
                                            case FARM:
                                                ((FarmMine) current.m).removeMineFarmItem(minItem);
                                                break;
                                        }

                                        Text.send(p, RMLanguageConfig.file().getString("System.Remove").replace("%object%", Text.beautifyMaterialName(minItem.getMaterial())));
                                        current.load();
                                        break;
                                    case SHIFT_RIGHT:
                                        if (minItem.isSchematicBlock()) { return; }

                                        if (minItem instanceof MineFarmItem) {
                                            ((MineFarmItem) minItem).addAge(-1);
                                            current.m.saveData(RMine.Data.BLOCKS);
                                            current.load();
                                        }
                                        break;
                                    case SHIFT_LEFT:
                                        if (minItem instanceof MineFarmItem) {
                                            ((MineFarmItem) minItem).addAge(1);
                                            current.m.saveData(RMine.Data.BLOCKS);
                                        } else {
                                            //disable block drop
                                            minItem.toggleVanillaBlockDrop();
                                            current.m.saveData(RMine.Data.BLOCKS);
                                        }
                                        current.load();
                                        break;

                                    case RIGHT:
                                        p.closeInventory();
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm.getPlugin(), () -> {
                                            final MineBreakActionsGUI v = new MineBreakActionsGUI(current.rm, p, current.m, minItem);
                                            v.openInventory(p);
                                        }, 2);
                                        break;
                                    default:
                                        if (minItem.isSchematicBlock()) { return; }

                                        // resto
                                        current.editPercentage(p, minItem, current);
                                        break;
                                }
                            }
                        }
                    }
                }
            }

            private void backPage(final MineItensGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    --asd.pageNumber;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(final MineItensGUI asd) {
                if (asd.p.exists(asd.pageNumber + 1)) {
                    ++asd.pageNumber;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
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
        switch (this.m.getType()) {
            case BLOCKS:
            case FARM:
            case SCHEMATIC:
                this.p = new Pagination<>(28, this.m.getBlockIcons().stream().sorted(Comparator.comparingDouble(MineItem::getPercentage).reversed()).collect(Collectors.toList()));
                break;
            default:
                rm.getPlugin().getLogger().warning("Unexpected value for mine items gui: " + this.m.getType().name());
                break;
        }

        this.fillChest(this.p.getPage(this.pageNumber));
    }

    public void fillChest(final List<MineItem> items) {
        this.inv.clear();
        this.display.clear();

        for (int i = 0; i < 8; ++i) {
            this.inv.setItem(i, placeholder);
        }

        this.inv.setItem(0, Items.createItemLore(Material.FILLED_MAP, 1, "&e&lToggle Break Permission", Arrays.asList("&fClick here to toggle the break permission:", "&f" + this.m.getBreakPermission(), "&7State: " + (this.m.isBreakingPermissionOn() ? "&a&lON" : "&c&lOFF"))));
        this.inv.setItem(8, Items.createItemLore(Material.COMPARATOR, 1, "&e&lDiscard Break Action Messages", Arrays.asList("&fClick here to toggle the messages.", "&7State: " + (RMMinesConfig.file().getBoolean(this.m.getName() + ".Settings.Discard-Break-Action-Messages") ? "&a&lON" : "&c&lOFF"))));

        this.inv.setItem(4, this.m.getType() != RMine.Type.SCHEMATIC ? add : placeholder);

        for (int slot : new int[]{45, 46, 47, 48, 49, 50, 51, 52, 53, 36, 44, 9, 17}) {
            this.inv.setItem(slot, placeholder);
        }

        this.inv.setItem(18, back);
        this.inv.setItem(27, back);
        this.inv.setItem(26, next);
        this.inv.setItem(35, next);

        int slot = 0;
        for (final ItemStack i : this.inv.getContents()) {
            if (i == null && !items.isEmpty()) {
                final MineItem s = items.get(0);
                this.inv.setItem(slot, s.getItem());
                this.display.put(slot, s);
                items.remove(0);
            }
            ++slot;
        }

        this.inv.setItem(49, close);
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

    protected void editPercentage(final Player p, final MineItem a, final MineItensGUI current) {
        p.closeInventory();

        if (RMConfig.file().getBoolean("RealMines.useButtonGUIForPercentages")) {
            PercentageInput pi = new PercentageInput(p, rm.getPlugin(), (int) (a.getPercentage() * 100), percentage -> {
                a.setPercentage((double) percentage / 100);
                current.m.saveData(BlockMine.Data.BLOCKS);

                Text.send(p, RMLanguageConfig.file().getString("System.Percentage-Modified").replaceAll("%value%", String.valueOf(percentage)));

                final MineItensGUI v = new MineItensGUI(current.rm, p, current.m);
                v.openInventory(p);
            });
            pi.openInventory(p);
        } else {
            new PlayerInput(p, s -> {
                double d = 0D;
                try {
                    d = Double.parseDouble(s.replace("%", ""));
                } catch (final Exception ex) {
                    Text.send(p, RMLanguageConfig.file().getString("System.Input-Percentage-Error"));
                    this.editPercentage(p, a, current);
                }

                if (d < 1D) {
                    Text.send(p, RMLanguageConfig.file().getString("System.Input-Percentage-Error-Greater"));
                    this.editPercentage(p, a, current);
                    return;
                }

                if (d > 100D) {
                    Text.send(p, RMLanguageConfig.file().getString("System.Input-Percentage-Error-Lower"));
                    this.editPercentage(p, a, current);
                    return;
                }

                d /= 100;

                a.setPercentage(d);
                current.m.saveData(BlockMine.Data.BLOCKS);

                Text.send(p, RMLanguageConfig.file().getString("System.Percentage-Modified").replaceAll("%value%", String.valueOf(d * 100)));
                final MineItensGUI v = new MineItensGUI(current.rm, p, current.m);
                v.openInventory(p);
            }, s -> {
                final MineItensGUI v = new MineItensGUI(current.rm, p, current.m);
                v.openInventory(p);
            });
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
