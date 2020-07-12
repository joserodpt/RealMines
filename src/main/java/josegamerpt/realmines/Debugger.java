package josegamerpt.realmines;

import josegamerpt.realmines.utils.Text;

public class Debugger {

	public static int state = 0;

	public static void print(String s) {
		if (state == 1) {
			System.out.print(Text.color(s));
		}
	}

}
