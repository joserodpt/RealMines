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
		String s = (percentage + "").replace(".0", "");
		return Integer.valueOf(s);

	}

	public void setPercentage(double d) {
		this.percentage = d;
	}
}
