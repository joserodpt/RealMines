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
import joserodpt.realmines.api.mine.components.RMBlockSet;
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

public class MineItemsGUI {

    private static final Map<UUID, MineItemsGUI> inventories = new HashMap<>();
    static final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static final ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, TranslatableLine.GUI_NEXT_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Next.Description"));
    static final ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, TranslatableLine.GUI_PREVIOUS_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Back.Description"));
    static final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, TranslatableLine.GUI_CLOSE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Close.Description"));
    static ItemStack add = Items.createItem(Material.HOPPER, 1, TranslatableLine.GUI_ADD_ITEMS_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Add.Description"));
    static ItemStack addSet = Items.createItem(Material.CAULDRON, 1, "&fAdd &anew &fblock set",
            List.of("&fClick here to add a new block set."));
    private final Inventory inv;
    private final UUID uuid;
    private final RMine mine;
    int pageNumber = 0;
    Pagination<MineItem> items;
    private final HashMap<Integer, Object> display = new HashMap<>();
    Pagination<RMBlockSet> blockSets;
    int pageNumberBlockSets = 0;
    String selectedBlockSet;
    private final RealMines rm;

    public MineItemsGUI(final RealMines rm, final Player target, final RMine mine) {
        this(rm, target, mine, mine.getCurrentBlockSet());
    }

    public MineItemsGUI(final RealMines rm, final Player target, final RMine mine, final String selectedBlockSet) {
        this.rm = rm;
        this.selectedBlockSet = selectedBlockSet;
        this.uuid = target.getUniqueId();
        this.mine = mine;
        this.inv = Bukkit.getServer().createInventory(null, mine.getType() == RMine.Type.SCHEMATIC ? 45 : 54, TranslatableLine.GUI_MINE_BLOCKS_NAME.setV1(TranslatableLine.ReplacableVar.MINE.eq(this.mine.getDisplayName())).get());

        this.load();

        this.register();
    }

    public void load() {
        switch (this.mine.getType()) {
            case BLOCKS:
            case FARM:
            case SCHEMATIC:
                this.items = new Pagination<>(21, this.mine.getBlockIcons(selectedBlockSet).stream().sorted(Comparator.comparingDouble(MineItem::getPercentage).reversed()).collect(Collectors.toList()));
                this.blockSets = new Pagination<>(7, this.mine.getBlockSets());
                break;
            default:
                rm.getPlugin().getLogger().warning("Unexpected value for mine items gui: " + this.mine.getType().name());
                break;
        }

        try {
            this.fillChest(this.items.getPage(this.pageNumber), this.blockSets.getPage(this.pageNumberBlockSets));
        } catch (final Exception ignored) {
            this.pageNumber = 0;
            this.pageNumberBlockSets = 0;
            this.selectedBlockSet = "default";
            this.fillChest(this.items.getPage(this.pageNumber), this.blockSets.getPage(this.pageNumberBlockSets));
        }
    }

    public void fillChest(final List<MineItem> items, final List<RMBlockSet> blockSets) {
        this.inv.clear();
        this.display.clear();

        for (int i = 0; i < 8; ++i) {
            this.inv.setItem(i, placeholder);
        }

        this.inv.setItem(0, Items.createItem(Material.FILLED_MAP, 1, "&e&lToggle Break Permission", Arrays.asList("&fClick here to toggle the break permission:", "&f" + this.mine.getBreakPermission(), "&7State: " + (this.mine.getSettingBool(RMineSettings.BREAK_PERMISSION) ? "&a&lON" : "&c&lOFF"))));
        this.inv.setItem(8, Items.createItem(Material.COMPARATOR, 1, "&e&lDiscard Break Action Messages", Arrays.asList("&fClick here to toggle the messages.", "&7State: " + (this.mine.getSettingBool(RMineSettings.DISCARD_BREAK_ACTION_MESSAGES) ? "&a&lON" : "&c&lOFF"))));

        this.inv.setItem(4, this.mine.getType() != RMine.Type.SCHEMATIC ? add : placeholder);

        for (int slot : new int[]{37, 38, 41, 42, 43, 36, 44, 9, 17}) {
            this.inv.setItem(slot, placeholder);
        }

        this.inv.setItem(18, back);
        this.inv.setItem(27, back);
        this.inv.setItem(26, next);
        this.inv.setItem(35, next);
        this.inv.setItem(39, mine.getType() == RMine.Type.SCHEMATIC ? placeholder : Items.createItem(Material.LEVER, 1, "&fCurrent block set mode: " + mine.getBlockSetMode().getDisplayName(), List.of("&7Next: " + mine.getBlockSetMode().next().getDisplayName(), "&fClick here to change the block set mode.")));
        this.inv.setItem(40, mine.getType() == RMine.Type.SCHEMATIC ? close : addSet);
        this.inv.setItem(41, mine.getType() == RMine.Type.SCHEMATIC ? placeholder : close);

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

        if (this.mine.getType() == RMine.Type.SCHEMATIC) {
            return;
        }


        this.inv.setItem(53, next);
        this.inv.setItem(45, back);

        for (int i : new int[]{46, 47, 48, 49, 50, 51, 52}) {
            if (this.inv.getItem(i) == null && !blockSets.isEmpty()) {
                final RMBlockSet s = blockSets.get(0);
                this.inv.setItem(i, s.getIcon(selectedBlockSet.equalsIgnoreCase(s.getKey())));
                this.display.put(i, s);
                blockSets.remove(0);
            }
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
                        final Player p = (Player) clicker;

                        e.setCancelled(true);

                        final MineItemsGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        switch (e.getRawSlot()) {
                            case 39:
                                if (current.mine.getType() == RMine.Type.SCHEMATIC) {
                                    return;
                                }
                                current.mine.setBlockSetMode(current.mine.getBlockSetMode().next());
                                current.load();
                                break;
                            case 40:
                                if (current.mine.getType() == RMine.Type.SCHEMATIC) {
                                    p.closeInventory();
                                    current.rm.getGUIManager().openMine(current.mine, p);
                                    return;
                                }
                                current.mine.addBlockSet(null);
                                current.load();
                                break;
                            case 41:
                                if (current.mine.getType() == RMine.Type.SCHEMATIC) {
                                    return;
                                }
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
                                            case BLOCKS ->
                                                    ((BlockMine) current.mine).addItem(current.selectedBlockSet, new MineBlockItem(mat));
                                            case FARM ->
                                                    ((FarmMine) current.mine).addFarmItem(current.selectedBlockSet, new MineFarmItem(mat));
                                        }
                                    }

                                    final MineItemsGUI v = new MineItemsGUI(current.rm, p, current.mine, current.selectedBlockSet);
                                    v.openInventory(p);
                                });
                                mpg.openInventory(p);
                                break;
                            case 0:
                                current.mine.setSettingBool(RMineSettings.BREAK_PERMISSION, !current.mine.getSettingBool(RMineSettings.BREAK_PERMISSION));
                                current.load();
                                break;
                            case 8:
                                current.mine.setSettingBool(RMineSettings.DISCARD_BREAK_ACTION_MESSAGES, !current.mine.getSettingBool(RMineSettings.DISCARD_BREAK_ACTION_MESSAGES));
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
                            case 45:
                                this.backPageBlockSets(current);
                                p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                break;
                            case 53:
                                this.nextPageBlockSets(current);
                                p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                break;
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            Object obj = current.display.get(e.getRawSlot());
                            if (obj instanceof MineItem minItem) {
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
                                                ((BlockMine) current.mine).removeMineBlockItem(current.selectedBlockSet, minItem);
                                                break;
                                            case FARM:
                                                ((FarmMine) current.mine).removeMineFarmItem(current.selectedBlockSet, minItem);
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
                                            final MineBreakActionsGUI v = new MineBreakActionsGUI(current.rm, p, current.mine, minItem, current.selectedBlockSet);
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
                            if (obj instanceof RMBlockSet blockSet) {
                                switch (e.getClick()) {
                                    case DROP:
                                        if (blockSet.isDefault()) {
                                            Text.send(p, "&cYou can't remove the default block set.");
                                            return;
                                        }

                                        current.mine.removeBlockSet(blockSet);

                                        if (blockSet.getKey().equalsIgnoreCase(current.selectedBlockSet)) {
                                            current.selectedBlockSet = "default";
                                        }

                                        current.load();
                                        break;
                                    case LEFT:
                                        current.selectedBlockSet = blockSet.getKey();
                                        current.load();
                                        break;
                                    case SHIFT_LEFT:
                                        p.closeInventory();
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm.getPlugin(), () -> {
                                            final MaterialPickerGUI mpg = new MaterialPickerGUI(p, TranslatableLine.GUI_SELECT_ICON_NAME.setV1(TranslatableLine.ReplacableVar.MINE.eq(blockSet.getKey())).get(), MaterialPickerGUI.MaterialLists.ALL_MATERIALS, mat -> {
                                                if (mat != null) {
                                                    blockSet.setIcon(mat);
                                                    current.mine.saveData(RMine.MineData.BLOCKS);
                                                }
                                                MineItemsGUI v = new MineItemsGUI(current.rm, p, current.mine, current.selectedBlockSet);
                                                v.openInventory(p);
                                            });
                                            mpg.openInventory(p);
                                        }, 2);
                                        break;
                                    case RIGHT:
                                        p.closeInventory();
                                        new PlayerInput(true, p, s -> {
                                            String oldKey = blockSet.getKey();
                                            current.mine.renameBlockSet(oldKey, s);
                                            MineItemsGUI v = new MineItemsGUI(current.rm, p, current.mine, s);
                                            v.openInventory(p);
                                        }, s -> {
                                            MineItemsGUI v = new MineItemsGUI(current.rm, p, current.mine, current.selectedBlockSet);
                                            v.openInventory(p);
                                        });
                                        break;
                                    case SHIFT_RIGHT:
                                        p.closeInventory();
                                        new PlayerInput(false, p, s -> {
                                            blockSet.setDescription(s);
                                            current.mine.saveData(RMine.MineData.BLOCKS);
                                            MineItemsGUI v = new MineItemsGUI(current.rm, p, current.mine, current.selectedBlockSet);
                                            v.openInventory(p);
                                        }, s -> {
                                            MineItemsGUI v = new MineItemsGUI(current.rm, p, current.mine, current.selectedBlockSet);
                                            v.openInventory(p);
                                        });
                                        break;
                                }
                            }
                        }
                    }
                }
            }

            private void nextPageBlockSets(MineItemsGUI current) {
                if (current.blockSets.exists(current.pageNumberBlockSets + 1)) {
                    ++current.pageNumberBlockSets;
                }

                current.fillChest(current.items.getPage(current.pageNumber), current.blockSets.getPage(current.pageNumberBlockSets));
            }

            private void backPageBlockSets(MineItemsGUI current) {
                if (current.blockSets.exists(current.pageNumberBlockSets - 1)) {
                    --current.pageNumberBlockSets;
                }

                current.fillChest(current.items.getPage(current.pageNumber), current.blockSets.getPage(current.pageNumberBlockSets));
            }

            private void backPage(final MineItemsGUI asd) {
                if (asd.items.exists(asd.pageNumber - 1)) {
                    --asd.pageNumber;
                }

                asd.fillChest(asd.items.getPage(asd.pageNumber), asd.blockSets.getPage(asd.pageNumberBlockSets));
            }

            private void nextPage(final MineItemsGUI asd) {
                if (asd.items.exists(asd.pageNumber + 1)) {
                    ++asd.pageNumber;
                }

                asd.fillChest(asd.items.getPage(asd.pageNumber), asd.blockSets.getPage(asd.pageNumberBlockSets));
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
        }

                ;
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

    protected void editPercentage(final Player p, final MineItem a, final MineItemsGUI current) {
        p.closeInventory();

        if (RMConfig.file().getBoolean("RealMines.useButtonGUIForPercentages")) {
            PercentageInput pi = new PercentageInput(p, rm.getPlugin(), (int) (a.getPercentage() * 100), percentage -> {
                a.setPercentage((double) percentage / 100);
                current.mine.saveData(RMine.MineData.BLOCKS);

                TranslatableLine.SYSTEM_PERCENTAGE_MODIFIED.setV1(TranslatableLine.ReplacableVar.VALUE.eq(percentage == 0 ? "0%" : Text.formatPercentages((double) percentage / 100) + "%")).send(p);

                final MineItemsGUI v = new MineItemsGUI(current.rm, p, current.mine, current.selectedBlockSet);
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

                TranslatableLine.SYSTEM_PERCENTAGE_MODIFIED.setV1(TranslatableLine.ReplacableVar.VALUE.eq(Text.formatPercentages((d)) + "%")).send(p);
                final MineItemsGUI v = new MineItemsGUI(current.rm, p, current.mine, current.selectedBlockSet);
                v.openInventory(p);
            }, s -> {
                final MineItemsGUI v = new MineItemsGUI(current.rm, p, current.mine, current.selectedBlockSet);
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
