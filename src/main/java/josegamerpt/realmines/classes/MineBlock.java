package josegamerpt.realmines.classes;

import org.bukkit.Material;

public class MineBlock {

	private Material material;
	private Double percentage;

	public MineBlock(Material m, Double p) {
		this.material = m;
		this.percentage = p;
	}

	public double getPercentage() {
		return percentage;
	}

	public Material getMaterial() {
		return material;
	}

	public Integer getPerInt() {
		return Integer.valueOf((percentage + "").replace(".0", ""));
	}

	public void setPercentage(double d) {
		this.percentage = d;
	}
}
