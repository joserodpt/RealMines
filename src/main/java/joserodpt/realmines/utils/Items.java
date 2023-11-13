package joserodpt.realmines.utils;

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

import joserodpt.realmines.gui.BlockPickerGUI;
import joserodpt.realmines.mine.types.farm.FarmItem;
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

    public static List<Material> getValidBlocks(BlockPickerGUI.PickType pt) {
        if (Objects.requireNonNull(pt) == BlockPickerGUI.PickType.FARM_ITEM) {
            return FarmItem.getIcons();
        }
        return Arrays.stream(Material.values())
                .filter(m -> !m.equals(Material.AIR) && m.isSolid() && m.isBlock() && m.isItem())
                .collect(Collectors.toList());
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
}
