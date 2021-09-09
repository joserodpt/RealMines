package josegamerpt.realmines.gui;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.mines.RMine;
import josegamerpt.realmines.mines.mine.BlockMine;
import josegamerpt.realmines.mines.components.MineBlock;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mines.components.MineCuboid;
import josegamerpt.realmines.utils.Itens;
import josegamerpt.realmines.utils.Pagination;
import josegamerpt.realmines.utils.PlayerInput;
import josegamerpt.realmines.utils.Text;
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

import java.util.*;

public class MaterialPicker {

    private final RealMines rm;

    public enum PickType {ICON, BLOCK, FACE_MATERIAL}

    static ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static ItemStack next = Itens.createItemLore(Material.GREEN_STAINED_GLASS, 1, Language.file().getString("GUI.Items.Next.Name"),
            Language.file().getStringList("GUI.Items.Next.Description"));
    static ItemStack back = Itens.createItemLore(Material.YELLOW_STAINED_GLASS, 1, Language.file().getString("GUI.Items.Back.Name"),
            Language.file().getStringList("GUI.Items.Back.Description"));
    static ItemStack close = Itens.createItemLore(Material.ACACIA_DOOR, 1, Language.file().getString("GUI.Items.Close.Name"),
            Language.file().getStringList("GUI.Items.Close.Description"));

    static ItemStack search = Itens.createItemLore(Material.OAK_SIGN, 1, Language.file().getString("GUI.Items.Search.Name"),
            Language.file().getStringList("GUI.Items.Close.Description"));
    private static final Map<UUID, MaterialPicker> inventories = new HashMap<>();
    int pageNumber = 0;
    Pagination<Material> p;
    private Inventory inv;
    private final UUID uuid;
    private final ArrayList<Material> items;
    private final HashMap<Integer, Material> display = new HashMap<>();
    private final RMine min;
    private final PickType pt;

    private String add;

    public MaterialPicker(RealMines rm, RMine m, Player pl, PickType block, String additional) {
        this.add = additional;
        this.rm = rm;
        this.uuid = pl.getUniqueId();
        this.min = m;
        this.pt = block;

        switch (block) {
            case ICON:
                inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + m.getDisplayName()));
                break;
            default:
                inv = Bukkit.getServer().createInventory(null, 54, Text.color("Pick a new block"));
                break;
        }
        items = Itens.getValidBlocks();

        p = new Pagination<>(28, items);
        fillChest(p.getPage(pageNumber));

        this.register();
    }

    public MaterialPicker(RealMines rm, RMine m, Player pl, PickType block, String search, String additional) {
        this.add = additional;
        this.rm = rm;

        this.uuid = pl.getUniqueId();
        this.min = m;
        this.pt = block;

        switch (block) {
            case ICON:
                inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + m.getDisplayName()));
                break;
            default:
                inv = Bukkit.getServer().createInventory(null, 54, Text.color("Pick a new block"));
                break;
        }

        items = searchMaterial(search);

        p = new Pagination<>(28, items);
        fillChest(p.getPage(pageNumber));

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
                        MaterialPicker current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        Player gp = (Player) clicker;

                        switch (e.getRawSlot()) {
                            case 4:
                                new PlayerInput(gp, input -> {
                                    if (current.searchMaterial(input).size() == 0) {
                                        gp.sendMessage(Text.color("&fNothing found for your results."));
                                        current.exit(current.rm, gp);
                                        return;
                                    }
                                    MaterialPicker df = new MaterialPicker(current.rm, current.min, gp, current.pt, input, current.add);
                                    df.openInventory(gp);
                                }, input -> {
                                    gp.closeInventory();
                                    MineBlocksViewer v = new MineBlocksViewer(current.rm, gp, current.min);
                                    v.openInventory(gp);
                                });
                                break;
                            case 49:
                                current.exit(current.rm, gp);
                                break;
                            case 26:
                            case 35:
                                nextPage(current);
                                gp.playSound(gp.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                break;
                            case 18:
                            case 27:
                                backPage(current);
                                gp.playSound(gp.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                break;
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            Material a = current.display.get(e.getRawSlot());

                            switch (current.pt) {
                                case ICON:
                                    current.min.setIcon(a);
                                    current.min.saveData(BlockMine.Data.ICON);
                                    gp.closeInventory();
                                    current.rm.getGUIManager().openMine(current.min, gp);
                                    break;
                                case BLOCK:
                                    current.min.addBlock(new MineBlock(a, 10D));
                                    gp.closeInventory();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm, () -> {
                                        MineBlocksViewer v = new MineBlocksViewer(current.rm, gp, current.min);
                                        v.openInventory(gp);
                                    }, 3);
                                    break;
                                case FACE_MATERIAL:
                                    MineCuboid.CuboidDirection cd = MineCuboid.CuboidDirection.valueOf(current.add);
                                    current.min.setFaceBlock(cd, a);
                                    gp.closeInventory();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm, () -> {
                                        MineFaces v = new MineFaces(current.rm, gp, current.min);
                                        v.openInventory(gp);
                                    }, 3);
                                    break;
                            }
                        }

                        e.setCancelled(true);
                    }
                }
            }

            private void backPage(MaterialPicker asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    asd.pageNumber--;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(MaterialPicker asd) {
                if (asd.p.exists(asd.pageNumber + 1)) {
                    asd.pageNumber++;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
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

    private ArrayList<Material> searchMaterial(String s) {
        ArrayList<Material> ms = new ArrayList<>();
        for (Material m : Itens.getValidBlocks()) {
            if (m.name().toLowerCase().contains(s.toLowerCase())) {
                ms.add(m);
            }
        }
        return ms;
    }

    public void fillChest(List<Material> items) {
        inv.clear();
        display.clear();

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, placeholder);
        }

        inv.setItem(4, search);

        inv.setItem(45, placeholder);
        inv.setItem(46, placeholder);
        inv.setItem(47, placeholder);
        inv.setItem(48, placeholder);
        inv.setItem(49, placeholder);
        inv.setItem(50, placeholder);
        inv.setItem(51, placeholder);
        inv.setItem(52, placeholder);
        inv.setItem(53, placeholder);
        inv.setItem(36, placeholder);
        inv.setItem(44, placeholder);
        inv.setItem(9, placeholder);
        inv.setItem(17, placeholder);

        inv.setItem(18, back);
        inv.setItem(27, back);
        inv.setItem(26, next);
        inv.setItem(35, next);

        int slot = 0;
        for (ItemStack i : inv.getContents()) {
            if (i == null && items.size() != 0) {
                Material s = items.get(0);
                inv.setItem(slot,
                        Itens.createItemLore(s, 1, "§3§l" + s.name(), Collections.singletonList("&fClick to pick this.")));
                display.put(slot, s);
                items.remove(0);
            }
            slot++;
        }

        inv.setItem(49, close);
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

    protected void exit(RealMines rm, Player gp) {
        switch (this.pt) {
            case ICON:
                gp.closeInventory();
                rm.getGUIManager().openMine(this.min, gp);
                break;
            case BLOCK:
                MineBlocksViewer v = new MineBlocksViewer(rm, gp, this.min);
                v.openInventory(gp);
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
