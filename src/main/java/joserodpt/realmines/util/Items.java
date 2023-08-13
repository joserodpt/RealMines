package joserodpt.realmines.util;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.mine.RMine;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Items {

    public static ArrayList<Material> getValidBlocks() {
        final ArrayList<Material> ms = new ArrayList<>();
        for (final Material m : Material.values()) {
            if (!m.equals(Material.AIR) && m.isSolid() && m.isBlock() && m.isItem()) {
                ms.add(m);
            }
        }
        return ms;
    }

    public static ItemStack createItem(final Material material, final int quantidade, final String nome) {
        final ItemStack item = new ItemStack(material, quantidade);
        final ItemMeta meta = item.getItemMeta();
        if (nome != null) {
            meta.setDisplayName(Text.color(nome));
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItemLore(final Material material, final int quantidade, final String nome, final List<String> desc) {
        final ItemStack item = new ItemStack(material, quantidade);
        if (item.getItemMeta() != null) {
            final ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Text.color(nome));
            meta.setLore(Text.color(desc));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createItemLoreEnchanted(final Material m, final int i, final String name, final List<String> desc) {
        final ItemStack item = new ItemStack(m, i);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(name));
        meta.setLore(Text.color(desc));
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack changeItemStack(final String name, final List<String> list, final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(name));
        meta.setLore(Text.color(list));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getMineColor(final RMine.Color c, final String name, final List<String> desc) {
        switch (c) {
            case RED:
                return Items.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTdjMWYxZWFkNGQ1MzFjYWE0YTViMGQ2OWVkYmNlMjlhZjc4OWEyNTUwZTVkZGJkMjM3NzViZTA1ZTJkZjJjNCJ9fX0="));
            case BLUE:
                return Items.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2IxOWRjNGQ0Njc4ODJkYmNhMWI1YzM3NDY1ZjBjZmM3MGZmMWY4MjllY2Y0YTg2NTc5NmI4ZTVjMjgwOWEifX19"));
            case YELLOW:
                return Items.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTRjNDE0MWMxZWRmM2Y3ZTQxMjM2YmQ2NThjNWJjN2I1YWE3YWJmN2UyYTg1MmI2NDcyNTg4MThhY2Q3MGQ4In19fQ=="));
            case WHITE:
                return Items.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVhNzcwZTdlNDRiM2ExZTZjM2I4M2E5N2ZmNjk5N2IxZjViMjY1NTBlOWQ3YWE1ZDUwMjFhMGMyYjZlZSJ9fX0="));
            case BROWN:
                return Items.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDU3NGE0Njc3MzcyOTViZDlkZGM4MjU0NWExYTRlMTQ2YTk0M2QwNWVjYzgyMWY5Y2M2YTU0M2ZmZTk5MzRhIn19fQ=="));
            case ORANGE:
                return Items.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjU5OTY5NTdiZmQ0Y2FmZTVkOTk5Njg0YzNkNTI5NGJkOWM1ZGZhNzg3ZGY0NjdiYjBhZmNmNTViYWFiZTgifX19"));
            case GREEN:
                return Items.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzZmNjlmN2I3NTM4YjQxZGMzNDM5ZjM2NThhYmJkNTlmYWNjYTM2NmYxOTBiY2YxZDZkMGEwMjZjOGY5NiJ9fX0="));
            case GRAY:
                return Items.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTUwOWRhOTg5NWJiZTc4YzFkY2NkMjZjYTY1MzY4ZDIxYWQ0N2VmYTk2ZTZiNjQxYjU4OTMzNjY0NTNhZWYifX19"));
            case PURPLE:
                return Items.changeItemStack(name, desc, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJhMGFkMzVjM2JhZTg3MTRmNWRhNWI5ZTc5OWM5ZmZkODY2M2YxYWJkNzEzNDNhMmZhMDE2ZDAyMmI0YmYifX19"));
        }
        return null;
    }
}
