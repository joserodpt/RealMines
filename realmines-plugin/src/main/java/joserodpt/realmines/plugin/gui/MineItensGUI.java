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

import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.RMineSettings;
import joserodpt.realmines.api.mine.components.items.MineBlockItem;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.mine.components.items.farm.MineFarmItem;
import joserodpt.realmines.api.mine.types.BlockMine;
import joserodpt.realmines.api.mine.types.farm.FarmMine;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Pagination;
import joserodpt.realmines.api.utils.PercentageInput;
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
    static final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static final ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, TranslatableLine.GUI_NEXT_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Next.Description"));
    static final ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, TranslatableLine.GUI_PREVIOUS_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Back.Description"));
    static final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, TranslatableLine.GUI_CLOSE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Close.Description"));
    static ItemStack add = Items.createItem(Material.HOPPER, 1, TranslatableLine.GUI_ADD_ITEMS_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Add.Description"));
    private final Inventory inv;
    private final UUID uuid;
    private final HashMap<Integer, MineItem> display = new HashMap<>();
    private final RMine mine;
    int pageNumber = 0;
    Pagination<MineItem> p;
    private final RealMines rm;

    public MineItensGUI(final RealMines rm, final Player target, final RMine mine) {
        this.rm = rm;
        this.uuid = target.getUniqueId();
        this.mine = mine;
        this.inv = Bukkit.getServer().createInventory(null, 54, TranslatableLine.GUI_MINE_BLOCKS_NAME.setV1(TranslatableLine.ReplacableVar.MINE.eq(this.mine.getDisplayName())).get());

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

                        switch (e.getRawSlot()) {
                            case 49:
                                p.closeInventory();
                                current.rm.getGUIManager().openMine(current.mine, p);
                                break;
                            case 4:
                                if (current.mine.getType() == RMine.Type.SCHEMATIC) {
                                    return;
                                }

                                p.closeInventory();

                                final MaterialPickerGUI mpg = new MaterialPickerGUI(p, TranslatableLine.GUI_PICK_NEW_BLOCK_NAME.get(), current.mine.getType() == RMine.Type.FARM ? MaterialPickerGUI.MaterialLists.ONLY_FARM_ICONS : MaterialPickerGUI.MaterialLists.ONLY_BLOCKS, mat -> {
                                    if (mat != null) {
                                        switch (current.mine.getType()) {
                                            case BLOCKS -> ((BlockMine) current.mine).addItem(new MineBlockItem(mat));
                                            case FARM -> ((FarmMine) current.mine).addFarmItem(new MineFarmItem(mat));
                                        }
                                    }

                                    final MineItensGUI v = new MineItensGUI(current.rm, p, current.mine);
                                    v.openInventory(p);
                                });
                                mpg.openInventory(p);
                                break;
                            case 0:
                                current.mine.setBooleanSetting(RMineSettings.BREAK_PERMISSION, !current.mine.getBooleanSetting(RMineSettings.BREAK_PERMISSION));
                                current.load();
                                break;
                            case 8:
                                current.mine.setBooleanSetting(RMineSettings.DISCARD_BREAK_ACTION_MESSAGES, !current.mine.getBooleanSetting(RMineSettings.DISCARD_BREAK_ACTION_MESSAGES));
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
                                    if (minItem.isSchematicBlock()) {
                                        return;
                                    }

                                    // eliminar
                                    switch (current.mine.getType()) {
                                        case BLOCKS:
                                            ((BlockMine) current.mine).removeMineBlockItem(minItem);
                                            break;
                                        case FARM:
                                            ((FarmMine) current.mine).removeMineFarmItem(minItem);
                                            break;
                                    }

                                    TranslatableLine.SYSTEM_REMOVE.setV1(TranslatableLine.ReplacableVar.OBJECT.eq(Text.beautifyMaterialName(minItem.getMaterial()))).send(p);
                                    current.load();
                                    break;
                                case SHIFT_RIGHT:
                                    if (minItem instanceof MineFarmItem) {
                                        ((MineFarmItem) minItem).addAge(-1);
                                        current.mine.saveData(RMine.MineData.BLOCKS);
                                        current.load();
                                    } else {
                                        //enable block drop
                                        minItem.toggleBlockMining();
                                        current.mine.saveData(RMine.MineData.BLOCKS);
                                        current.load();
                                    }
                                    break;
                                case SHIFT_LEFT:
                                    if (minItem instanceof MineFarmItem) {
                                        ((MineFarmItem) minItem).addAge(1);
                                        current.mine.saveData(RMine.MineData.BLOCKS);
                                    } else {
                                        //disable block drop
                                        minItem.toggleVanillaBlockDrop();
                                        current.mine.saveData(RMine.MineData.BLOCKS);
                                    }
                                    current.load();
                                    break;

                                case RIGHT:
                                    p.closeInventory();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm.getPlugin(), () -> {
                                        final MineBreakActionsGUI v = new MineBreakActionsGUI(current.rm, p, current.mine, minItem);
                                        v.openInventory(p);
                                    }, 2);
                                    break;
                                default:
                                    if (minItem.isSchematicBlock()) {
                                        return;
                                    }

                                    // resto
                                    current.editPercentage(p, minItem, current);
                                    break;
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
        switch (this.mine.getType()) {
            case BLOCKS:
            case FARM:
            case SCHEMATIC:
                this.p = new Pagination<>(28, this.mine.getBlockIcons().stream().sorted(Comparator.comparingDouble(MineItem::getPercentage).reversed()).collect(Collectors.toList()));
                break;
            default:
                rm.getPlugin().getLogger().warning("Unexpected value for mine items gui: " + this.mine.getType().name());
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

        this.inv.setItem(0, Items.createItem(Material.FILLED_MAP, 1, "&e&lToggle Break Permission", Arrays.asList("&fClick here to toggle the break permission:", "&f" + this.mine.getBreakPermission(), "&7State: " + (this.mine.getBooleanSetting(RMineSettings.BREAK_PERMISSION) ? "&a&lON" : "&c&lOFF"))));
        this.inv.setItem(8, Items.createItem(Material.COMPARATOR, 1, "&e&lDiscard Break Action Messages", Arrays.asList("&fClick here to toggle the messages.", "&7State: " + (this.mine.getBooleanSetting(RMineSettings.DISCARD_BREAK_ACTION_MESSAGES) ? "&a&lON" : "&c&lOFF"))));

        this.inv.setItem(4, this.mine.getType() != RMine.Type.SCHEMATIC ? add : placeholder);

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
                current.mine.saveData(RMine.MineData.BLOCKS);

                TranslatableLine.SYSTEM_PERCENTAGE_MODIFIED.setV1(TranslatableLine.ReplacableVar.VALUE.eq(String.valueOf(percentage))).send(p);

                final MineItensGUI v = new MineItensGUI(current.rm, p, current.mine);
                v.openInventory(p);
            });
            pi.openInventory(p);
        } else {
            new PlayerInput(true, p, s -> {
                double d = 0D;
                try {
                    d = Double.parseDouble(s.replace("%", ""));
                } catch (final Exception ex) {
                    TranslatableLine.SYSTEM_INPUT_PERCENTAGE_ERROR.send(p);
                    this.editPercentage(p, a, current);
                }

                if (d < 1D) {
                    TranslatableLine.SYSTEM_INPUT_PERCENTAGE_ERROR_GREATER.send(p);
                    this.editPercentage(p, a, current);
                    return;
                }

                if (d > 100D) {
                    TranslatableLine.SYSTEM_INPUT_PERCENTAGE_ERROR_LOWER.send(p);
                    this.editPercentage(p, a, current);
                    return;
                }

                d /= 100;

                a.setPercentage(d);
                current.mine.saveData(RMine.MineData.BLOCKS);

                TranslatableLine.SYSTEM_PERCENTAGE_MODIFIED.setV1(TranslatableLine.ReplacableVar.VALUE.eq(String.valueOf(d * 100))).send(p);
                final MineItensGUI v = new MineItensGUI(current.rm, p, current.mine);
                v.openInventory(p);
            }, s -> {
                final MineItensGUI v = new MineItensGUI(current.rm, p, current.mine);
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
