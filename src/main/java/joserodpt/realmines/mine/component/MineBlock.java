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

import org.bukkit.Material;

public class MineBlock {

    private final Material material;
    private Double percentage;

    public MineBlock(final Material m, final Double p) {
        this.material = m;
        this.percentage = p;
    }

    public MineBlock(final Material m) {
        this.material = m;
        this.percentage = 0.1D;
    }

    public double getPercentage() {
        return this.percentage;
    }

    public void setPercentage(final double d) {
        this.percentage = d;
    }

    public Material getMaterial() {
        return this.material;
    }

    @Override
    public String toString() {
        return "MineBlock{" +
                "material=" + this.material.toString() +
                ", percentage=" + this.percentage +
                '}';
    }
}
