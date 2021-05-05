package josegamerpt.realmines.utils;

import dev.dbassett.skullcreator.SkullCreator;
import josegamerpt.realmines.mines.Mine;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Itens {

    public static ArrayList<Material> getValidBlocks() {
        ArrayList<Material> ms = new ArrayList<>();
        for (Material m : Material.values()) {
            if (!m.equals(Material.AIR) && m.isSolid() && m.isBlock() && m.isItem()) {
                ms.add(m);
            }
        }
        return ms;
    }

    public static ItemStack createItem(Material material, int quantidade, String nome) {
        ItemStack item = new ItemStack(material, quantidade);
        ItemMeta meta = item.getItemMeta();
        if (nome != null) {
            meta.setDisplayName(Text.color(nome));
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItemLore(Material material, int quantidade, String nome, List<String> desc) {
        ItemStack item = new ItemStack(material, quantidade);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(nome));
        meta.setLore(Text.color(desc));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItemLoreEnchanted(Material m, int i, String name, List<String> desc) {
        ItemStack item = new ItemStack(m, i);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(name));
        meta.setLore(Text.color(desc));
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack changeItemStack(String name, List<String> list, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(name));
        meta.setLore(Text.color(list));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getMineColor(Mine.Color c, String name, List<String> desc) {
        switch (c) {
            case RED:
                return Itens.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTdjMWYxZWFkNGQ1MzFjYWE0YTViMGQ2OWVkYmNlMjlhZjc4OWEyNTUwZTVkZGJkMjM3NzViZTA1ZTJkZjJjNCJ9fX0="));
            case BLUE:
                return Itens.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2IxOWRjNGQ0Njc4ODJkYmNhMWI1YzM3NDY1ZjBjZmM3MGZmMWY4MjllY2Y0YTg2NTc5NmI4ZTVjMjgwOWEifX19"));
            case YELLOW:
                return Itens.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTRjNDE0MWMxZWRmM2Y3ZTQxMjM2YmQ2NThjNWJjN2I1YWE3YWJmN2UyYTg1MmI2NDcyNTg4MThhY2Q3MGQ4In19fQ=="));
            case WHITE:
                return Itens.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVhNzcwZTdlNDRiM2ExZTZjM2I4M2E5N2ZmNjk5N2IxZjViMjY1NTBlOWQ3YWE1ZDUwMjFhMGMyYjZlZSJ9fX0="));
            case BROWN:
                return Itens.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDU3NGE0Njc3MzcyOTViZDlkZGM4MjU0NWExYTRlMTQ2YTk0M2QwNWVjYzgyMWY5Y2M2YTU0M2ZmZTk5MzRhIn19fQ=="));
            case ORANGE:
                return Itens.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjU5OTY5NTdiZmQ0Y2FmZTVkOTk5Njg0YzNkNTI5NGJkOWM1ZGZhNzg3ZGY0NjdiYjBhZmNmNTViYWFiZTgifX19"));
            case GREEN:
                return Itens.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzZmNjlmN2I3NTM4YjQxZGMzNDM5ZjM2NThhYmJkNTlmYWNjYTM2NmYxOTBiY2YxZDZkMGEwMjZjOGY5NiJ9fX0="));
            case GRAY:
                return Itens.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTUwOWRhOTg5NWJiZTc4YzFkY2NkMjZjYTY1MzY4ZDIxYWQ0N2VmYTk2ZTZiNjQxYjU4OTMzNjY0NTNhZWYifX19"));
            case PURPLE:
                return Itens.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJhMGFkMzVjM2JhZTg3MTRmNWRhNWI5ZTc5OWM5ZmZkODY2M2YxYWJkNzEzNDNhMmZhMDE2ZDAyMmI0YmYifX19"));
        }
        return null;
    }
}
