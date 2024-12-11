package joserodpt.realmines.api.mine.components;

import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.utils.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RMBlockSet {

    private String key, description;
    private Material icon;
    private Map<Material, MineItem> items;

    //new with random key
    public RMBlockSet() {
        this("set-" + System.currentTimeMillis(), "New block set-" + System.currentTimeMillis(), Material.CAULDRON, new HashMap<>());
    }

    //new with key
    public RMBlockSet(String key) {
        this(key, "Description for block set: " + key, Material.CAULDRON, new HashMap<>());
    }

    //already existing
    public RMBlockSet(String key, String description, Material icon, Map<Material, MineItem> items) {
        this.key = key;
        this.description = description;
        this.icon = icon;
        this.items = items;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<Material, MineItem> getItems() {
        return items;
    }

    public ItemStack getIcon(boolean sel) {
        return sel ? Items.createItemLoreEnchanted(icon, getAmount(), "&f" + this.key, getLore()) : Items.createItem(icon, getAmount(), "&f" + this.key, getLore());
    }

    private int getAmount() {
        return Math.min(64, Math.max(1, items.size()));
    }

    private List<String> getLore() {
        return Arrays.asList("&7Description: " + this.description, "&6", "&a&nLeft-click&r&f to view it's contents", "&a&nShift-Left-click&r&f to change it's icon", "&e&nRight-click&r&f to change the name", "&e&nShift-Right-click&r&f to change the description", "&c&nQ (Drop)&r&f to delete this set");
    }

    public boolean isDefault() {
        return key.equalsIgnoreCase("default");
    }

    public Material getIconMaterial() {
        return icon;
    }

    public void setIcon(Material mat) {
        this.icon = mat;
    }

    public void setKey(String s) {
        this.key = s;
    }

    public boolean contains(MineItem mi) {
        return items.containsKey(mi.getMaterial());
    }

    public void remove(MineItem mb) {
        items.remove(mb.getMaterial());
    }

    public void add(MineItem mineBlock) {
        this.items.put(mineBlock.getMaterial(), mineBlock);
    }
}
