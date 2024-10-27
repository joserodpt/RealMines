package joserodpt.realmines.api.utils;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.mine.types.farm.FarmItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Items {

    public static List<Material> getValidBlocks(PickType pt) {
        if (Objects.requireNonNull(pt) == PickType.FARM_ITEM) {
            return FarmItem.getIcons();
        }
        return Arrays.stream(Material.values())
                .filter(m -> !m.equals(Material.AIR) && m.isSolid() && m.isBlock() && m.isItem())
                .collect(Collectors.toList());
    }

    public static ItemStack createItem(Material material, final int quantidade, final String nome) {
        if (!material.isItem()) {
            material = Material.STONE;
        }

        final ItemStack item = new ItemStack(material, quantidade);
        final ItemMeta meta = item.getItemMeta();
        if (nome != null) {
            meta.setDisplayName(Text.color(nome));
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItem(Material material, final int quantidade, final String nome, final List<String> desc) {
        if (!material.isItem()) {
            material = Material.STONE;
        }

        final ItemStack item = new ItemStack(material, quantidade);
        if (item.getItemMeta() != null) {
            final ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Text.color(nome));
            meta.setLore(Text.color(desc));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createItemLoreEnchanted(Material m, final int i, final String name, final List<String> desc) {
        if (!m.isItem()) {
            m = Material.STONE;
        }

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
}
