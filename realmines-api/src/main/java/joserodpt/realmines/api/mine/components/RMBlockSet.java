package joserodpt.realmines.api.mine.components;

import joserodpt.realmines.api.mine.components.items.MineItem;
import org.bukkit.Material;

import java.util.Map;

public class RMBlockSet {

    String key;
    Map<Material, MineItem> items;

    public RMBlockSet(String key, Map<Material, MineItem> items) {
        this.key = key;
        this.items = items;
    }

    public String getKey() {
        return key;
    }

    public Map<Material, MineItem> getItems() {
        return items;
    }

}
