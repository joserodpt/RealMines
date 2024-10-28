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

import java.util.List;

public class Items {

    public static ItemStack createItem(Material m, final int quantidade, final String nome) {
        m = checkValidMaterialItem(m);
        final ItemStack item = new ItemStack(m, quantidade);
        final ItemMeta meta = item.getItemMeta();
        if (nome != null) {
            meta.setDisplayName(Text.color(nome));
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItem(Material m, final int quantidade, final String nome, final List<String> desc) {
        m = checkValidMaterialItem(m);
        final ItemStack item = new ItemStack(m, quantidade);
        if (item.getItemMeta() != null) {
            final ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Text.color(nome));
            meta.setLore(Text.color(desc));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createItemLoreEnchanted(Material m, final int i, final String name, final List<String> desc) {
        m = checkValidMaterialItem(m);
        final ItemStack item = new ItemStack(m, i);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(name));
        meta.setLore(Text.color(desc));
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    private static Material checkValidMaterialItem(Material m) {
        if (m == Material.WATER) {
            return Material.WATER_BUCKET;
        }
        if (m == Material.LAVA) {
            return Material.LAVA_BUCKET;
        }

        if (!m.isItem()) {
            //the material can be a crop, if so, try to get the crop item
            Material cropIcon = FarmItem.findIconForCrop(m);
            if (cropIcon != null && cropIcon.isItem()) {
                return cropIcon;
            } else {
                return Material.STONE;
            }
        } else {
            return m;
        }
    }


    public static ItemStack changeItemStack(final String name, final List<String> list, final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(name));
        meta.setLore(Text.color(list));
        item.setItemMeta(meta);
        return item;
    }
}
