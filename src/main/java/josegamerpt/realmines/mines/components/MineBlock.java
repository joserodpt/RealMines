package josegamerpt.realmines.mines.components;

import org.bukkit.Material;

public class MineBlock {

	private Material material;
	private Double percentage;

	public MineBlock(Material m, Double p) {
		this.material = m;
		this.percentage = p;
	}

	public MineBlock(Material m) {
		this.material = m;
		this.percentage = 0.1D;
	}

	public double getPercentage() {
		return this.percentage;
	}

	public Material getMaterial() {
		return this.material;
	}

	public void setPercentage(double d) {
		this.percentage = d;
	}

	@Override
	public String toString() {
		return "MineBlock{" +
				"material=" + material.toString() +
				", percentage=" + percentage +
				'}';
	}
}
