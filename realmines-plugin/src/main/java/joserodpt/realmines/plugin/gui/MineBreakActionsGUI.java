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
import joserodpt.realmines.api.mine.components.actions.MineAction;
import joserodpt.realmines.api.mine.components.actions.MineActionCommand;
import joserodpt.realmines.api.mine.components.actions.MineActionDropItem;
import joserodpt.realmines.api.mine.components.actions.MineActionDummy;
import joserodpt.realmines.api.mine.components.actions.MineActionGiveItem;
import joserodpt.realmines.api.mine.components.actions.MineActionMoney;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Pagination;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MineBreakActionsGUI { //TODO TRANSLATE

    private static final Map<UUID, MineBreakActionsGUI> inventories = new HashMap<>();
    static final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static final ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, TranslatableLine.GUI_NEXT_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Next.Description"));
    static final ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, TranslatableLine.GUI_PREVIOUS_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Back.Description"));
    static final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, TranslatableLine.GUI_CLOSE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Close.Description"));
    final ItemStack add = Items.createItem(Material.OBSERVER, 1, "&b&LAdd a New Break Action",
            Collections.singletonList("&fClick here to add a new break action to this item."));
    private final Inventory inv;
    private final UUID uuid;
    private final HashMap<Integer, MineAction> display = new HashMap<>();
    private final RMine mine;
    private final MineItem mineItem;
    private int pageNumber = 0;
    private Pagination<MineAction> p;
    private final RealMines rm;
    private final String currentBlockSet;

    public MineBreakActionsGUI(final RealMines rm, final Player target, final RMine min, final MineItem mineItem, final String currentBlockSet) {
        this.rm = rm;
        this.uuid = target.getUniqueId();
        this.currentBlockSet = currentBlockSet;
        this.mine = min;
        this.mineItem = mineItem;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.beautifyMaterialName(mineItem.getMaterial()) + " break actions");

        this.load();

        this.register();
    }

    public void load() {
        List<MineAction> actions = this.mine.getMineItemsOfSet(currentBlockSet).get(this.mineItem.getMaterial()).getBreakActions();
        if (actions.isEmpty()) {
            this.p = new Pagination<>(28, Collections.singletonList(new MineActionDummy()));
        } else {
            this.p = new Pagination<>(28, actions.stream().sorted(Comparator.comparingDouble(MineAction::getChance).reversed()).collect(Collectors.toList()));
        }

        this.fillChest(this.p.getPage(this.pageNumber));
    }

    public void fillChest(final List<MineAction> items) {
        this.inv.clear();
        this.display.clear();

        for (int i = 0; i < 9; ++i) {
            this.inv.setItem(i, placeholder);
        }
        this.inv.setItem(4, add);

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
                final MineAction s = items.get(0);
                this.inv.setItem(slot, s.getItem());
                this.display.put(slot, s);
                items.remove(0);
            }
            ++slot;
        }

        this.inv.setItem(49, close);
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

                        final MineBreakActionsGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        switch (e.getRawSlot()) {
                            case 49:
                                p.closeInventory();
                                final MineItemsGUI v = new MineItemsGUI(current.rm, p, current.mine);
                                v.openInventory(p);
                                break;
                            case 4:
                                p.closeInventory();
                                current.rm.getGUIManager().openBreakActionChooser(p, current.mine, current.mineItem, current.currentBlockSet);
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
                            final MineAction a = current.display.get(e.getRawSlot());

                            if (a.isInteractable()) {
                                switch (e.getClick()) {
                                    case DROP:
                                        current.mineItem.getBreakActions().remove(a);
                                        current.mine.saveData(RMine.MineData.BLOCKS);
                                        current.load();
                                        break;
                                    case RIGHT:
                                        switch (a.getType()) {
                                            case DROP_ITEM:
                                            case GIVE_ITEM:
                                                if (p.getInventory().getItemInMainHand().getType() == Material.AIR) {
                                                    return;
                                                }

                                                switch (a.getType()) {
                                                    case GIVE_ITEM:
                                                        ((MineActionGiveItem) a).setItem(p.getInventory().getItemInMainHand());
                                                        break;
                                                    case DROP_ITEM:
                                                        ((MineActionDropItem) a).setItem(p.getInventory().getItemInMainHand());
                                                        break;
                                                }

                                                current.mine.saveData(RMine.MineData.BLOCKS);
                                                break;
                                            case EXECUTE_COMMAND:
                                                p.closeInventory();

                                                TranslatableLine.MINE_BREAK_ACTION_INPUT_COMMAND.send(p);
                                                new PlayerInput(false, p, s -> {
                                                    ((MineActionCommand) a).setCommand(s);
                                                    current.mine.saveData(RMine.MineData.BLOCKS);

                                                    final MineBreakActionsGUI v = new MineBreakActionsGUI(current.rm, p, current.mine, current.mineItem, current.currentBlockSet);
                                                    v.openInventory(p);
                                                }, s -> {
                                                    final MineBreakActionsGUI v = new MineBreakActionsGUI(current.rm, p, current.mine, current.mineItem, current.currentBlockSet);
                                                    v.openInventory(p);
                                                });
                                                break;
                                            case GIVE_MONEY:
                                                p.closeInventory();

                                                TranslatableLine.MINE_BREAK_ACTION_INPUT_AMOUNT.send(p);
                                                new PlayerInput(true, p, s -> {
                                                    final double d;
                                                    try {
                                                        d = Double.parseDouble(s);
                                                    } catch (final Exception ex) {
                                                        TranslatableLine.MINE_BREAK_ACTION_INPUT_AMOUNT_ERROR.send(p);
                                                        return;
                                                    }

                                                    ((MineActionMoney) a).setAmount(d);
                                                    current.mine.saveData(RMine.MineData.BLOCKS);

                                                    final MineBreakActionsGUI v = new MineBreakActionsGUI(current.rm, p, current.mine, current.mineItem, current.currentBlockSet);
                                                    v.openInventory(p);
                                                }, s -> {
                                                    final MineBreakActionsGUI v = new MineBreakActionsGUI(current.rm, p, current.mine, current.mineItem, current.currentBlockSet);
                                                    v.openInventory(p);
                                                });
                                                break;
                                        }
                                        current.load();
                                        break;

                                    default:
                                        //chance chance
                                        TranslatableLine.MINE_BREAK_ACTION_INPUT_CHANCE.send(p);
                                        new PlayerInput(true, p, s -> {
                                            final double d;
                                            try {
                                                d = Double.parseDouble(s);
                                            } catch (final Exception ex) {
                                                TranslatableLine.MINE_BREAK_ACTION_INPUT_AMOUNT_ERROR.send(p);
                                                return;
                                            }

                                            a.setChance(d);
                                            current.mine.saveData(RMine.MineData.BLOCKS);

                                            final MineBreakActionsGUI v = new MineBreakActionsGUI(current.rm, p, current.mine, current.mineItem, current.currentBlockSet);
                                            v.openInventory(p);
                                        }, s -> {
                                            final MineBreakActionsGUI v = new MineBreakActionsGUI(current.rm, p, current.mine, current.mineItem, current.currentBlockSet);
                                            v.openInventory(p);
                                        });
                                        break;
                                }
                            }
                        }
                    }
                }
            }

            private void backPage(final MineBreakActionsGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    --asd.pageNumber;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(final MineBreakActionsGUI asd) {
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
