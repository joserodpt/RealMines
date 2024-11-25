package joserodpt.realmines.api.mine;

import joserodpt.realmines.api.mine.components.items.MineItem;
import org.bukkit.Material;

import java.util.Map;

public class RMBlockSet {

    String name;
    Map<Material, MineItem> items;

    public RMBlockSet(String name, Map<Material, MineItem> items) {
        this.name = name;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public Map<Material, MineItem> getItems() {
        return items;
    }

}
