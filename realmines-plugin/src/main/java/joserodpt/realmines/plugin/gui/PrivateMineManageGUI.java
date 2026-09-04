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
 * @author José Rodrigues © 2019-2026
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.config.TranslatableLine.ReplacableVar;
import joserodpt.realmines.api.managers.PrivateMineTemplate;
import joserodpt.realmines.api.managers.PrivateMinesManagerAPI.ClaimResult;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.PrivateMineData;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.PlayerHeads;
import joserodpt.realmines.api.utils.PlayerInput;
import joserodpt.realmines.api.utils.Text;
import joserodpt.realmines.plugin.RealMines;
import joserodpt.realmines.plugin.command.PrivateMineCMD;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Everything the owner of one private mine can do with it: go there, reset it, extend it, manage who
 * else may use it, and give it up.
 */
public class PrivateMineManageGUI {

    private static final Map<UUID, PrivateMineManageGUI> inventories = new HashMap<>();

    private static final int SLOT_TELEPORT = 10;
    private static final int SLOT_RESET = 12;
    private static final int SLOT_EXTEND = 14;
    private static final int SLOT_INFO = 16;
    private static final int SLOT_ADD_TRUSTED = 30;
    private static final int SLOT_RELEASE = 32;
    private static final int SLOT_BACK = 40;

    /**
     * The trusted heads shown in the bottom row, by slot.
     */
    private static final int[] TRUSTED_SLOTS = {19, 20, 21, 22, 23, 24, 25};

    static {
        //the template's trusted-limit is capped to this, so every trusted player stays removable here
        if (TRUSTED_SLOTS.length != PrivateMineTemplate.MAX_TRUSTED) {
            throw new IllegalStateException("TRUSTED_SLOTS must match PrivateMineTemplate.MAX_TRUSTED");
        }
    }

    static final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");

    /**
     * Last manual reset per mine, so the button can't be held down to refill a region every tick.
     */
    private static final Map<String, Long> RESET_COOLDOWN = new HashMap<>();
    private static final long RESET_COOLDOWN_MS = 5000L;

    private final Inventory inv;
    private final UUID uuid;
    private final RealMines rm;
    private final RMine mine;
    private final Map<Integer, UUID> trustedSlots = new HashMap<>();

    public PrivateMineManageGUI(final RealMines rm, final Player as, final RMine mine) {
        this.rm = rm;
        this.uuid = as.getUniqueId();
        this.mine = mine;
        this.inv = Bukkit.getServer().createInventory(null, 45, Text.color("&f&lPrivate &9&lMine"));

        this.load();
        this.register();
    }

    public void load() {
        this.inv.clear();
        this.trustedSlots.clear();

        for (int i = 0; i < this.inv.getSize(); ++i) {
            this.inv.setItem(i, placeholder);
        }

        final PrivateMineData data = this.mine.getPrivateData();
        final PrivateMineTemplate template = this.rm.getPrivateMinesManager().getTemplate(data.getTemplate());

        this.inv.setItem(SLOT_TELEPORT, Items.createItem(Material.ENDER_PEARL, 1, "&bTeleport",
                Collections.singletonList("&fGo to your private mine.")));

        this.inv.setItem(SLOT_RESET, Items.createItem(Material.CLOCK, 1, "&bReset now",
                Arrays.asList("&fRefill the mine straight away.",
                        "&7Resets on its own every &f" + PrivateMineCMD.formatTime(this.mine.getResetValue(RMine.Reset.TIME)))));

        if (data.getLifecycle() == PrivateMineData.Lifecycle.TIME_LIMITED) {
            this.inv.setItem(SLOT_EXTEND, Items.createItem(Material.EMERALD, 1, "&bExtend",
                    Arrays.asList("&7Expires in: &f" + PrivateMineCMD.formatTime(data.getSecondsLeft()),
                            "&7Cost: &f" + (template == null ? 0D : template.getRenewCost()),
                            "", "&fClick to extend it.")));
        }

        this.inv.setItem(SLOT_INFO, Items.createItem(this.mine.getIcon(), 1, this.mine.getDisplayName(),
                Arrays.asList("&7Template: &f" + data.getTemplate(),
                        "&7Blocks left: &f" + this.mine.getRemainingBlocks() + "&7/&f" + this.mine.getBlockCount(),
                        "&7Expires in: &f" + PrivateMineCMD.formatTime(data.getSecondsLeft()))));

        final int limit = template == null ? 5 : template.getTrustedLimit();
        this.inv.setItem(SLOT_ADD_TRUSTED, Items.createItem(Material.PLAYER_HEAD, 1, "&aTrust a player",
                Arrays.asList("&fLet someone else mine here.",
                        "&7Trusted: &f" + data.getTrustedCount() + "&7/&f" + limit,
                        "", "&fClick and type their name in chat.")));

        this.inv.setItem(SLOT_RELEASE, Items.createItem(Material.BARRIER, 1, "&cRelease this mine",
                Arrays.asList("&fGives the mine up for good.",
                        "&cThis cannot be undone.", "", "&fPress &cQ &fto confirm.")));

        this.inv.setItem(SLOT_BACK, Items.createItem(Material.ACACIA_DOOR, 1, "&cBack"));

        final List<UUID> trusted = data.getTrustedList();
        for (int i = 0; i < TRUSTED_SLOTS.length && i < trusted.size(); i++) {
            final UUID t = trusted.get(i);
            final String name = Bukkit.getOfflinePlayer(t).getName();
            this.inv.setItem(TRUSTED_SLOTS[i], PlayerHeads.getHead(t, "&b" + (name == null ? t.toString() : name),
                    Arrays.asList("&7Can mine here.", "", "&fClick to remove.")));
            this.trustedSlots.put(TRUSTED_SLOTS[i], t);
        }

        if (trusted.isEmpty()) {
            this.inv.setItem(TRUSTED_SLOTS[0], Items.createItem(Material.GRAY_DYE, 1, "&7Nobody is trusted",
                    Collections.singletonList("&fOnly you can mine here.")));
        }
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(final InventoryClickEvent e) {
                final HumanEntity clicker = e.getWhoClicked();
                if (!(clicker instanceof Player) || e.getCurrentItem() == null) {
                    return;
                }

                final PrivateMineManageGUI current = inventories.get(clicker.getUniqueId());
                if (current == null || e.getInventory().getHolder() != current.getInventory().getHolder()) {
                    return;
                }

                e.setCancelled(true);
                final Player p = (Player) clicker;

                //the mine can be released or expire while this menu sits open
                if (current.rm.getMineManager().getMine(current.mine.getName()) == null) {
                    p.closeInventory();
                    TranslatableLine.PRIVATE_MINE_NO_MINE.send(p);
                    return;
                }

                final PrivateMineData data = current.mine.getPrivateData();

                switch (e.getRawSlot()) {
                    case SLOT_BACK:
                        p.closeInventory();
                        //back to the player's own mines, which is where this menu is reached from
                        new PrivateMinesGUI(current.rm, p, PrivateMinesGUI.View.OWNED).openInventory(p);
                        return;
                    case SLOT_TELEPORT:
                        p.closeInventory();
                        current.rm.getMineManager().teleport(p, current.mine, false, true);
                        return;
                    case SLOT_RESET: {
                        //a reset is a full region rewrite on the main thread, so it can't be spammable
                        final long now = System.currentTimeMillis();
                        final Long last = RESET_COOLDOWN.get(current.mine.getName());
                        if (last != null && now - last < RESET_COOLDOWN_MS) {
                            return;
                        }
                        //drop expired entries so this never grows with the number of mines ever opened,
                        //and so a re-claimed mine of the same name doesn't inherit an old cooldown
                        RESET_COOLDOWN.values().removeIf(stamp -> now - stamp >= RESET_COOLDOWN_MS);
                        RESET_COOLDOWN.put(current.mine.getName(), now);
                        current.mine.reset(RMine.ResetCause.COMMAND);
                        current.load();
                        return;
                    }
                    case SLOT_EXTEND: {
                        if (data.getLifecycle() != PrivateMineData.Lifecycle.TIME_LIMITED) {
                            return;
                        }
                        final ClaimResult result = current.rm.getPrivateMinesManager().extend(p, current.mine);
                        if (result == ClaimResult.OK) {
                            TranslatableLine.PRIVATE_MINE_EXTENDED
                                    .setV1(ReplacableVar.TIME.eq(PrivateMineCMD.formatTime(data.getSecondsLeft()))).send(p);
                        } else if (result == ClaimResult.INSUFFICIENT_FUNDS) {
                            final PrivateMineTemplate template = current.rm.getPrivateMinesManager().getTemplate(data.getTemplate());
                            TranslatableLine.PRIVATE_MINE_INSUFFICIENT_FUNDS
                                    .setV1(ReplacableVar.VALUE.eq(String.valueOf(template == null ? 0D : template.getRenewCost()))).send(p);
                        } else if (result == ClaimResult.NO_ECONOMY) {
                            TranslatableLine.PRIVATE_MINE_NO_ECONOMY.send(p);
                        }
                        current.load();
                        return;
                    }
                    case SLOT_ADD_TRUSTED:
                        addTrusted(current, p);
                        return;
                    case SLOT_RELEASE:
                        //a misclick here would destroy the mine, so make it a deliberate one
                        if (e.getClick() != ClickType.DROP) {
                            return;
                        }
                        p.closeInventory();
                        final String display = current.mine.getDisplayName();
                        current.rm.getPrivateMinesManager().release(current.mine);
                        TranslatableLine.PRIVATE_MINE_RELEASED.setV1(ReplacableVar.MINE.eq(display)).send(p);
                        return;
                    default:
                        break;
                }

                final UUID trusted = current.trustedSlots.get(e.getRawSlot());
                if (trusted != null) {
                    final String name = Bukkit.getOfflinePlayer(trusted).getName();
                    data.removeTrusted(trusted);
                    current.mine.savePrivateData();
                    TranslatableLine.PRIVATE_MINE_TRUSTED_REMOVED
                            .setV1(ReplacableVar.PLAYER.eq(name == null ? trusted.toString() : name)).send(p);
                    current.load();
                }
            }

            private void addTrusted(final PrivateMineManageGUI current, final Player p) {
                final PrivateMineData data = current.mine.getPrivateData();
                final PrivateMineTemplate template = current.rm.getPrivateMinesManager().getTemplate(data.getTemplate());
                final int limit = template == null ? 5 : template.getTrustedLimit();

                if (data.getTrustedCount() >= limit) {
                    TranslatableLine.PRIVATE_MINE_TRUSTED_LIMIT.setV1(ReplacableVar.COUNT.eq(String.valueOf(limit))).send(p);
                    return;
                }

                new PlayerInput(true, p, input -> {
                    //the mine can be released or expire while the owner is typing the name
                    if (current.rm.getMineManager().getMine(current.mine.getName()) == null) {
                        TranslatableLine.PRIVATE_MINE_NO_MINE.send(p);
                        return;
                    }

                    final OfflinePlayer target = PrivateMineCMD.findPlayer(input);

                    if (target == null) {
                        TranslatableLine.PRIVATE_MINE_PLAYER_NOT_FOUND.setV1(ReplacableVar.PLAYER.eq(input)).send(p);
                    } else if (!data.addTrusted(target.getUniqueId())) {
                        TranslatableLine.PRIVATE_MINE_TRUSTED_ALREADY.setV1(ReplacableVar.PLAYER.eq(input)).send(p);
                    } else {
                        current.mine.savePrivateData();
                        TranslatableLine.PRIVATE_MINE_TRUSTED_ADDED.setV1(ReplacableVar.PLAYER.eq(input)).send(p);
                    }

                    reopen(current, p);
                }, input -> reopen(current, p));
            }

            private void reopen(final PrivateMineManageGUI current, final Player p) {
                final PrivateMineManageGUI gui = new PrivateMineManageGUI(current.rm, p, current.mine);
                Bukkit.getScheduler().runTask(current.rm.getPlugin(), () -> gui.openInventory(p));
            }

            @EventHandler
            public void onClose(final InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    final PrivateMineManageGUI gui = inventories.get(e.getPlayer().getUniqueId());
                    if (gui != null) {
                        gui.unregister();
                    }
                }
            }
        };
    }

    public void openInventory(final Player target) {
        final Inventory inv = this.getInventory();
        final InventoryView openInv = target.getOpenInventory();
        if (openInv != null) {
            final Inventory openTop = openInv.getTopInventory();
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
