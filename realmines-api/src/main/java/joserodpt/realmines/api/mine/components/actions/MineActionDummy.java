package joserodpt.realmines.api.mine.components.actions;

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

import joserodpt.realmines.api.utils.Items;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MineActionDummy extends MineAction {

    public MineActionDummy() {
        super();
    }

    public void execute(final Player p, final Location l, double randomChance) {
    }

    @Override
    public MineActionType getType() {
        return MineActionType.DUMMY;
    }

    @Override
    public String getValueString() {
        return "";
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
