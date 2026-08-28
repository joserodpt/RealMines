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
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.database.RMPlayerBlockStat;
import joserodpt.realmines.api.database.RMPlayerData;
import joserodpt.realmines.api.managers.DatabaseManagerAPI;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Pagination;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.api.utils.skulls.SkullCreator;
import joserodpt.realmines.plugin.RealMines;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Ranks players by how much they have mined, either overall or for one block.
 */
public class LeaderboardGUI {

    private static final Map<UUID, LeaderboardGUI> inventories = new HashMap<>();
    static final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static final ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, TranslatableLine.GUI_NEXT_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Next.Description"));
    static final ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, TranslatableLine.GUI_PREVIOUS_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Back.Description"));
    static final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, TranslatableLine.GUI_CLOSE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Close.Description"));

    private final Inventory inv;
    private final UUID uuid;
    private final RealMines rm;

    /**
     * The block being ranked, or null to rank every block together.
     */
    private Material material;

    int pageNumber = 0;
    Pagination<Entry> p;
    private boolean empty;

    public LeaderboardGUI(final RealMines rm, final Player viewer) {
        this(rm, viewer, null);
    }

    public LeaderboardGUI(final RealMines rm, final Player viewer, final Material material) {
        this.rm = rm;
        this.uuid = viewer.getUniqueId();
        this.material = material;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&8Top Miners"));

        this.load();
        this.register();
    }

    public void load() {
        this.pageNumber = 0;
        final List<Entry> entries = collect();
        this.empty = entries.isEmpty();
        this.p = new Pagination<>(28, entries);
        this.fillChest(getPage(this.pageNumber));
    }

    private List<Entry> collect() {
        final DatabaseManagerAPI db = this.rm.getDatabaseManager();
        final List<Entry> entries = new ArrayList<>();
        if (db == null) {
            return entries;
        }

        final int size = Math.max(1, RMConfig.file().getInt("RealMines.Stats.Leaderboard-Size", 28));

        if (this.material == null) {
            int rank = 1;
            for (final RMPlayerData data : db.getTopTotalBlocksMined(size)) {
                entries.add(new Entry(rank++, data.getUUID(), data.getName(), data.getTotalBlocksMined()));
            }
        } else {
            int rank = 1;
            for (final RMPlayerBlockStat stat : db.getTopBlocksMined(this.material, size)) {
                entries.add(new Entry(rank++, stat.getPlayerUUID(), stat.getPlayerName(), stat.getAmount()));
            }
        }
        return entries;
    }

    /**
     * Pagination throws when asked for a page it doesn't have, which is what an empty board is.
     */
    private List<Entry> getPage(final int page) {
        return this.p.exists(page) ? this.p.getPage(page) : new ArrayList<>();
    }

    public void fillChest(final List<Entry> items) {
        this.inv.clear();

        for (int i = 0; i < 9; ++i) {
            this.inv.setItem(i, placeholder);
        }
        for (final int slot : new int[]{45, 46, 47, 48, 49, 50, 51, 52, 53, 36, 44, 9, 17}) {
            this.inv.setItem(slot, placeholder);
        }

        this.inv.setItem(4, getModeItem());
        this.inv.setItem(18, back);
        this.inv.setItem(27, back);
        this.inv.setItem(26, next);
        this.inv.setItem(35, next);

        int slot = 0;
        for (final ItemStack i : this.inv.getContents()) {
            if (i == null && !items.isEmpty()) {
                this.inv.setItem(slot, items.get(0).getItem());
                items.remove(0);
            }
            ++slot;
        }

        if (this.empty) {
            this.inv.setItem(22, Items.createItem(Material.BARRIER, 1,
                    RMLanguageConfig.file().getString("GUI.Items.Leaderboard-Empty.Name"),
                    RMLanguageConfig.file().getStringList("GUI.Items.Leaderboard-Empty.Description")));
        }

        this.inv.setItem(45, Items.createItem(Material.EXPERIENCE_BOTTLE, 1,
                RMLanguageConfig.file().getString("GUI.Items.Achievements.Name"),
                RMLanguageConfig.file().getStringList("GUI.Items.Achievements.Description")));
        this.inv.setItem(49, close);
    }

    private ItemStack getModeItem() {
        final String mode = this.material == null ? "All blocks" : Text.beautifyMaterialName(this.material);
        return Items.createItem(this.material == null ? Material.COMPARATOR : this.material, 1,
                RMLanguageConfig.file().getString("GUI.Items.Leaderboard-Mode.Name").replace("%value%", mode),
                RMLanguageConfig.file().getStringList("GUI.Items.Leaderboard-Mode.Description"));
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
                        final LeaderboardGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);
                        final Player p = (Player) clicker;

                        switch (e.getRawSlot()) {
                            case 4:
                                if (e.getClick() == ClickType.RIGHT) {
                                    current.material = null;
                                    current.load();
                                } else {
                                    openMaterialPicker(current, p);
                                }
                                break;
                            case 45:
                                p.closeInventory();
                                //opened a tick later, once the close has actually gone through.
                                //an online player's stats are already in memory, so this can't block
                                Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm.getPlugin(),
                                        () -> new AchievementBoardGUI(current.rm, p, p.getName(),
                                                current.rm.getDatabaseManager().getStats(p.getUniqueId())).openInventory(p), 1);
                                break;
                            case 49:
                                p.closeInventory();
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
                    }
                }
            }

            private void openMaterialPicker(final LeaderboardGUI current, final Player p) {
                final DatabaseManagerAPI db = current.rm.getDatabaseManager();
                //only offer blocks somebody has actually mined, instead of every block in the game
                final List<Material> tracked = db == null ? new ArrayList<>() : db.getTrackedMaterials();
                if (tracked.isEmpty()) {
                    return;
                }

                p.closeInventory();
                Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm.getPlugin(), () -> {
                    final MaterialPickerGUI mpg = new MaterialPickerGUI(p, TranslatableLine.GUI_PICK_NEW_BLOCK_NAME.get(),
                            tracked, mat -> {
                        final LeaderboardGUI lb = new LeaderboardGUI(current.rm, p, mat);
                        lb.openInventory(p);
                    }, "");
                    mpg.openInventory(p);
                }, 1);
            }

            private void backPage(final LeaderboardGUI g) {
                if (g.p.exists(g.pageNumber - 1)) {
                    --g.pageNumber;
                }
                g.fillChest(g.getPage(g.pageNumber));
            }

            private void nextPage(final LeaderboardGUI g) {
                if (g.p.exists(g.pageNumber + 1)) {
                    ++g.pageNumber;
                }
                g.fillChest(g.getPage(g.pageNumber));
            }

            @EventHandler
            public void onClose(final InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    if (e.getInventory() == null) {
                        return;
                    }
                    final UUID uuid = e.getPlayer().getUniqueId();
                    final LeaderboardGUI current = inventories.get(uuid);
                    //only when this GUI's own inventory is the one closing. Switching to another
                    //GUI registers the new one first, so unregistering on any close at all would
                    //kill it before the player ever gets to click it
                    if (current != null && e.getInventory().equals(current.getInventory())) {
                        current.unregister();
                    }
                }
            }
        };
    }

    public void openInventory(final Player target) {
        final InventoryView open = target.getOpenInventory();
        //fillChest writes straight into this inventory, so when it is already the one on screen the
        //player is looking at the current contents and there is nothing to reopen.
        //Matching on inventory type instead would treat any other 54 slot chest GUI as this one and
        //paint into it, leaving this GUI updating an inventory nobody is looking at.
        if (open != null && this.inv.equals(open.getTopInventory())) {
            return;
        }
        target.openInventory(this.inv);
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

    /**
     * One row of the leaderboard.
     */
    private static class Entry {

        private final int rank;
        private final UUID uuid;
        private final String name;
        private final long amount;

        Entry(final int rank, final UUID uuid, final String name, final long amount) {
            this.rank = rank;
            this.uuid = uuid;
            this.name = name == null ? uuid.toString().substring(0, 8) : name;
            this.amount = amount;
        }

        ItemStack getItem() {
            return Items.changeItemStack("&9#" + this.rank + " &f" + this.name,
                    Arrays.asList("&fBlocks mined: &b" + Text.formatNumber(this.amount)),
                    SkullCreator.itemFromUuid(this.uuid));
        }
    }
}
