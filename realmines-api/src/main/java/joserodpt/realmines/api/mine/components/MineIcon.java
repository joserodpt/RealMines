package joserodpt.realmines.api.mine.components;

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

import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.utils.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MineIcon {

    private RMine m;

    public MineIcon() { }

    public MineIcon(RMine m) {
        this.m = m;
    }

    public ItemStack getMineItem() {
        return this.getMine() != null ? this.m.getMineIcon() :
         Items.createItemLore(Material.DEAD_BUSH, 1, RMLanguageConfig.file().getString("GUI.Items.No-Mines-Found.Name"), RMLanguageConfig.file().getStringList("GUI.Items.No-Mines-Found.Description"));
    }

    public RMine getMine() {
        return this.m;
    }
}
