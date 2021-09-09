package josegamerpt.realmines.mines.components;

import org.bukkit.Material;

public class MineBlock {

	private Material material;
	private Double percentage;

	public MineBlock(Material m, Double p) {
		this.material = m;
		this.percentage = p;
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
}
