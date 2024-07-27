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
import joserodpt.realmines.api.mine.components.MineCuboid;
import joserodpt.realmines.api.mine.components.items.MineBlockItem;
import joserodpt.realmines.api.mine.components.items.farm.MineFarmItem;
import joserodpt.realmines.api.mine.types.BlockMine;
import joserodpt.realmines.api.mine.types.farm.FarmItem;
import joserodpt.realmines.api.mine.types.farm.FarmMine;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Pagination;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class BlockPickerGUI {

    private static final Map<UUID, BlockPickerGUI> inventories = new HashMap<>();
    static final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static final ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, TranslatableLine.GUI_NEXT_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Next.Description"));
    static final ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, TranslatableLine.GUI_PREVIOUS_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Back.Description"));
    static final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, TranslatableLine.GUI_CLOSE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Close.Description"));
    static ItemStack search = Items.createItem(Material.OAK_SIGN, 1, TranslatableLine.GUI_SEARCH_ITEM_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Close.Description"));
    private final RealMines rm;
    private final UUID uuid;
    private final List<Material> items;
    private final HashMap<Integer, Material> display = new HashMap<>();
    private final RMine min;
    private final PickType pt;
    int pageNumber = 0;
    private final Pagination<Material> p;
    private final Inventory inv;
    private final String add;

    public BlockPickerGUI(final RealMines rm, final RMine m, final Player pl, final PickType pickType, final String additional) {
        this.add = additional;
        this.rm = rm;
        this.uuid = pl.getUniqueId();
        this.min = m;
        this.pt = pickType;

        this.inv = Bukkit.getServer().createInventory(null, 54, Objects.requireNonNull(pickType) == PickType.ICON ? TranslatableLine.GUI_SELECT_ICON_NAME.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName())).get() : TranslatableLine.GUI_PICK_NEW_BLOCK_NAME.get());

        this.items = Items.getValidBlocks(pt);

        this.p = new Pagination<>(28, this.items);
        this.fillChest(this.p.getPage(this.pageNumber));

        this.register();
    }

    public BlockPickerGUI(final RealMines rm, final RMine m, final Player pl, final PickType pickType, final String search, final String additional) {
        this.add = additional;
        this.rm = rm;

        this.uuid = pl.getUniqueId();
        this.min = m;
        this.pt = pickType;

        this.inv = Bukkit.getServer().createInventory(null, 54, Objects.requireNonNull(pickType) == PickType.ICON ? TranslatableLine.GUI_SELECT_ICON_NAME.setV1(TranslatableLine.ReplacableVar.MINE.eq(m.getDisplayName())).get() : TranslatableLine.GUI_PICK_NEW_BLOCK_NAME.get());

        this.items = this.searchMaterial(search, pickType);

        this.p = new Pagination<>(28, this.items);
        this.fillChest(this.p.getPage(this.pageNumber));

        this.register();
    }

    private List<Material> searchMaterial(final String s, final PickType pt) {
        return Items.getValidBlocks(pt).stream()
                .filter(m -> m.name().toLowerCase().contains(s.toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));
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
                        final BlockPickerGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        final Player p = (Player) clicker;

                        switch (e.getRawSlot()) {
                            case 4:
                                new PlayerInput(p, input -> {
                                    if (current.searchMaterial(input, current.pt).isEmpty()) {
                                        TranslatableLine.SYSTEM_NOTHING_FOUND.send(p);
                                        current.exit(current.rm, p);
                                        return;
                                    }
                                    final BlockPickerGUI df = new BlockPickerGUI(current.rm, current.min, p, current.pt, input, current.add);
                                    df.openInventory(p);
                                }, input -> {
                                    p.closeInventory();
                                    final MineItensGUI v = new MineItensGUI(current.rm, p, current.min);
                                    v.openInventory(p);
                                });
                                break;
                            case 49:
                                current.exit(current.rm, p);
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
                            final Material a = current.display.get(e.getRawSlot());

                            switch (current.pt) {
                                case ICON:
                                    current.min.setIcon(a);
                                    current.min.saveData(BlockMine.Data.ICON);
                                    current.exit(current.rm, p);
                                    break;
                                case FARM_ITEM:
                                case BLOCK:
                                    switch (current.min.getType()) {
                                        case BLOCKS:
                                            ((BlockMine) current.min).addItem(new MineBlockItem(a));
                                            break;
                                        case FARM:
                                            ((FarmMine) current.min).addFarmItem(new MineFarmItem(FarmItem.valueOf(a)));
                                            break;
                                    }
                                    current.exit(current.rm, p);
                                    break;
                                case FACE_MATERIAL:
                                    final MineCuboid.CuboidDirection cd = MineCuboid.CuboidDirection.valueOf(current.add);
                                    current.min.setFaceBlock(cd, a);

                                    current.exit(current.rm, p);
                                    break;
                            }
                        }

                        e.setCancelled(true);
                    }
                }
            }

            private void backPage(final BlockPickerGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    --asd.pageNumber;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(final BlockPickerGUI asd) {
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

    public void fillChest(final List<Material> items) {
        this.inv.clear();
        this.display.clear();

        for (int i = 0; i < 9; ++i) {
            this.inv.setItem(i, placeholder);
        }

        this.inv.setItem(4, search);

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
                final Material s = items.get(0);
                this.inv.setItem(slot,
                        Items.createItem(s, 1, TranslatableLine.GUI_PICK_NAME.setV1(TranslatableLine.ReplacableVar.MATERIAL.eq(Text.beautifyMaterialName(s))).get(), RMLanguageConfig.file().getStringList("GUI.Items.Pick.Description")));
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

    protected void exit(final RealMines rm, final Player p) {
        unregister();
        p.closeInventory();

        switch (this.pt) {
            case ICON:
                rm.getGUIManager().openMine(this.min, p);
                break;
            case FARM_ITEM:
            case BLOCK:
                Bukkit.getScheduler().scheduleSyncDelayedTask(rm.getPlugin(), () -> {
                    final MineItensGUI v = new MineItensGUI(rm, p, this.min);
                    v.openInventory(p);
                }, 1);

                break;
            case FACE_MATERIAL:
                Bukkit.getScheduler().scheduleSyncDelayedTask(rm.getPlugin(), () -> {
                    final MineFacesGUI va = new MineFacesGUI(rm, p, min);
                    va.openInventory(p);
                }, 1);
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
