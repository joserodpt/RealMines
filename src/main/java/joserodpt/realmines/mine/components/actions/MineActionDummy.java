package joserodpt.realmines.mine.components.actions;

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

import joserodpt.realmines.util.Items;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MineActionDummy extends MineAction {

    public MineActionDummy() {
        super();
    }

    public void execute(final Player p, final Location l, double randomChance) { }

    @Override
    public Type getType() {
        return Type.DUMMY;
    }

    @Override
    public Double getValue() {
        return 0D;
    }

    @Override
    public ItemStack getItem() {
        return Items.createItem(Material.DEAD_BUSH, 1, "&fAdd Break Actions in the Icon Above");
    }
}
