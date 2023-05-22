package josegamerpt.realmines.mine.components;

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
