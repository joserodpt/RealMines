package josegamerpt.realmines.mines;

import org.bukkit.block.Block;

public class MineSign {

    private Block block;
    private String mod;

    public MineSign(Block b, String m) {
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
