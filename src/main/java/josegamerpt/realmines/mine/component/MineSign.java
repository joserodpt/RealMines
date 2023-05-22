package josegamerpt.realmines.mine.component;

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
