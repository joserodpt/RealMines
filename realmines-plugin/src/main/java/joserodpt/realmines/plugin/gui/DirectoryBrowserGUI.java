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

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.mine.components.RMFailedToLoadException;
import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.Pagination;
import joserodpt.realmines.api.utils.Text;
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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DirectoryBrowserGUI {

    private static final Map<UUID, DirectoryBrowserGUI> inventories = new HashMap<>();
    static final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static final ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, TranslatableLine.GUI_NEXT_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Next.Description"));
    static final ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, TranslatableLine.GUI_PREVIOUS_PAGE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Back.Description"));
    static final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, TranslatableLine.GUI_CLOSE_NAME.get(),
            RMLanguageConfig.file().getStringList("GUI.Items.Close.Description"));
    private final Inventory inv;
    private final UUID uuid;
    private final HashMap<Integer, File> display = new HashMap<>();
    private final Collection<String> allowedExtensions;
    private final FileRunnable onFileChoosen;
    int pageNumber = 0;
    Pagination<File> p;
    private File upDir;
    private File currentDir;

    public DirectoryBrowserGUI(final Player p, final File startDir, String title, Collection<String> allowedExtensions, FileRunnable onFileChoosen) {
        this.onFileChoosen = onFileChoosen;
        this.allowedExtensions = allowedExtensions;
        this.uuid = p.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, title);

        loadDirectory(startDir);

        this.register();
    }

    private void loadDirectory(final File dir) {
        if (!dir.exists() || !dir.isDirectory() || !dir.canRead()) {
            return;
        }

        this.currentDir = dir;
        try {
            this.upDir = dir.getParentFile();
        } catch (Exception e) {
            this.upDir = null;
        }
        this.load();
    }

    public void load() {
        List<File> files = Arrays.stream(Objects.requireNonNull(this.currentDir.listFiles())).sorted(Comparator.comparingLong(File::lastModified)).filter(f -> {
            if (f.isDirectory()) {
                return true;
            }
            for (String ext : this.allowedExtensions) {
                if (f.getName().endsWith(ext)) {
                    return true;
                }
            }
            return false;
        }).toList();

        if (files.isEmpty()) {
            this.fillChest(Collections.emptyList());
        } else {
            this.p = new Pagination<>(28, files);
            this.fillChest(this.p.getPage(this.pageNumber));
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
                        final DirectoryBrowserGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);
                        final Player p = (Player) clicker;

                        switch (e.getRawSlot()) {
                            case 4:
                                //go up directory
                                if (current.currentDir.getParentFile() != null) {
                                    current.loadDirectory(current.currentDir.getParentFile());
                                }
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

                        if (current.display.containsKey(e.getRawSlot())) {
                            File f = current.display.get(e.getRawSlot());
                            if (f.isDirectory()) {
                                current.loadDirectory(f);
                            } else {
                                p.closeInventory();
                                Bukkit.getScheduler().scheduleSyncDelayedTask(RealMinesAPI.getInstance().getPlugin(), () -> {
                                    try {
                                        current.onFileChoosen.fileRunnable(f);
                                    } catch (RMFailedToLoadException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }, 3);
                            }
                        }
                    }
                }
            }

            private void backPage(final DirectoryBrowserGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    --asd.pageNumber;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(final DirectoryBrowserGUI asd) {
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

    public void fillChest(final List<File> items) {
        this.inv.clear();
        this.display.clear();

        for (int i = 0; i < 9; ++i) {
            this.inv.setItem(i, placeholder);
        }

        if (this.upDir != null)
            this.inv.setItem(4, Items.createItem(Material.COMPASS, 1, "&fClick to go up to: " + this.upDir, Collections.singletonList("&7Currently on: &7" + this.currentDir.getName())));

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
                final File s = items.get(0);
                this.inv.setItem(slot, Items.createItem(s.isDirectory() ? Material.CHEST : (s.getName().endsWith(".schem") || s.getName().endsWith(".schematic")) ? Material.FILLED_MAP : Material.MAP, 1, (s.isDirectory() ? "&f&l" : "&f") + s.getName(), Arrays.asList("&7Last modified: " + Text.formatEpoch(s.lastModified()), s.isDirectory() ? "&7Click to open." : "&7Click to select.")));
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

    public Inventory getInventory() {
        return this.inv;
    }

    private void register() {
        inventories.put(this.uuid, this);
    }

    private void unregister() {
        inventories.remove(this.uuid);
    }

    @FunctionalInterface
    public interface FileRunnable {
        void fileRunnable(File file) throws RMFailedToLoadException;
    }
}
