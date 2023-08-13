package joserodpt.realmines.mine.component;

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

import org.bukkit.block.Block;

public class MineSign {

    private final Block block;
    private final String mod;

    public MineSign(final Block b, final String m) {
        this.block = b;
        this.mod = m;
    }

    public Block getBlock() {
        return this.block;
    }

    public String getModifier() {
        return this.mod;
    }

}
