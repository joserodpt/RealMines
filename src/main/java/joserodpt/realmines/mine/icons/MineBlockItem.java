package joserodpt.realmines.mine.icons;

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

import joserodpt.realmines.config.Language;
import joserodpt.realmines.util.Items;
import joserodpt.realmines.util.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public class MineBlockItem extends MineItem {

    public MineBlockItem(final Material m) {
        super(m);
    }

    public MineBlockItem(final Material m, final Double percentage) {
        super(m, percentage);
    }

    @Override
    public ItemStack getItem() {
        return Items.createItemLore(super.getMaterial(), 1, Language.file().getString("GUI.Items.Mine-Block.Block.Name").replace("%material%", super.getMaterial().name()), Language.file().getStringList("GUI.Items.Mine-Block.Block.Description")
                .stream()
                .map(s -> Text.color(s.replaceAll("%percentage%", String.valueOf(super.getPercentage() * 100))))
                .collect(Collectors.toList()));
    }

    @Override
    public Type getType() {
        return Type.BLOCK;
    }

    @Override
    public String toString() {
        return super.getMaterial().name() + ";" + super.getPercentage();
    }
}