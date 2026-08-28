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

import joserodpt.realmines.api.achievements.RMAchievement;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.database.RMPlayerStats;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Pagination;
import joserodpt.realmines.api.utils.PlayerHeads;
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
import java.util.UUID;

/**
 * Shows a player's mining achievements, what they have unlocked and how far they are from the rest.
 */
public class AchievementBoardGUI {

    private static final Map<UUID, AchievementBoardGUI> inventories = new HashMap<>();
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
    private final String targetName;

    /**
     * The stats being shown. Live for an online player, a snapshot read off the database for
     * anybody else.
     */
    private final RMPlayerStats stats;
    private final HashMap<Integer, RMAchievement> display = new HashMap<>();

    int pageNumber = 0;
    Pagination<RMAchievement> p;

    public AchievementBoardGUI(final RealMines rm, final Player viewer, final String targetName, final RMPlayerStats stats) {
        this.rm = rm;
        this.uuid = viewer.getUniqueId();
        this.targetName = targetName;
        this.stats = stats;
        this.inv = Bukkit.getServer().createInventory(null, 54,
                Text.color("&8Achievements &7- &f" + targetName));

        this.load();
        this.register();
    }

    public void load() {
        this.p = new Pagination<>(28, new ArrayList<>(this.rm.getAchievementsManager().getAchievements()));
        this.fillChest(getPage(this.pageNumber));

        //the board can be opened for an offline player, whose skin the server won't know
        if (this.stats != null) {
            PlayerHeads.preload(java.util.Collections.singletonList(this.stats.getUUID()), this::repaint);
        }
    }

    /**
     * Redraws the current page in place. Skipped if the player has since closed or replaced this GUI.
     */
    private void repaint() {
        if (inventories.get(this.uuid) == this) {
            this.fillChest(getPage(this.pageNumber));
        }
    }

    /**
     * Pagination throws when asked for a page it doesn't have, which is what an empty board is.
     */
    private List<RMAchievement> getPage(final int page) {
        return this.p.exists(page) ? this.p.getPage(page) : new ArrayList<>();
    }

    public void fillChest(final List<RMAchievement> items) {
        this.inv.clear();
        this.display.clear();

        for (int i = 0; i < 9; ++i) {
            this.inv.setItem(i, placeholder);
        }
        for (final int slot : new int[]{45, 46, 47, 48, 49, 50, 51, 52, 53, 36, 44, 9, 17}) {
            this.inv.setItem(slot, placeholder);
        }

        this.inv.setItem(4, getSummaryItem());
        this.inv.setItem(18, back);
        this.inv.setItem(27, back);
        this.inv.setItem(26, next);
        this.inv.setItem(35, next);

        int slot = 0;
        for (final ItemStack i : this.inv.getContents()) {
            if (i == null && !items.isEmpty()) {
                final RMAchievement achievement = items.get(0);
                this.inv.setItem(slot, achievement.getItem(this.stats));
                this.display.put(slot, achievement);
                items.remove(0);
            }
            ++slot;
        }

        this.inv.setItem(45, Items.createItem(Material.GOLD_INGOT, 1,
                RMLanguageConfig.file().getString("GUI.Items.Leaderboard.Name"),
                RMLanguageConfig.file().getStringList("GUI.Items.Leaderboard.Description")));
        this.inv.setItem(49, close);
    }

    private ItemStack getSummaryItem() {
        final int total = this.rm.getAchievementsManager().getAchievements().size();
        final int unlocked = this.rm.getAchievementsManager().getUnlockedCount(this.stats);
        final long mined = this.stats == null ? 0L : this.stats.getTotalBlocksMined();

        final List<String> lore = new ArrayList<>();
        lore.add(Text.color("&fBlocks mined: &b" + Text.formatNumber(mined)));
        lore.add(Text.color("&fAchievements: &b" + unlocked + "&7/&b" + total));
        if (this.stats != null && this.stats.getFirstJoin() > 0) {
            lore.add(Text.color("&8First seen: " + Text.formatEpoch(this.stats.getFirstJoin())));
        }

        if (this.stats == null) {
            return Items.createItem(Material.PLAYER_HEAD, 1, Text.color("&9" + this.targetName), lore);
        }
        return PlayerHeads.getHead(this.stats.getUUID(), "&9" + this.targetName, lore);
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
                        final AchievementBoardGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);
                        final Player p = (Player) clicker;

                        switch (e.getRawSlot()) {
                            case 45:
                                p.closeInventory();
                                //opened a tick later, once the close has actually gone through
                                Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm.getPlugin(),
                                        () -> new LeaderboardGUI(current.rm, p).openInventory(p), 1);
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

            private void backPage(final AchievementBoardGUI g) {
                if (g.p.exists(g.pageNumber - 1)) {
                    --g.pageNumber;
                }
                g.fillChest(g.getPage(g.pageNumber));
            }

            private void nextPage(final AchievementBoardGUI g) {
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
                    final AchievementBoardGUI current = inventories.get(uuid);
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
}
