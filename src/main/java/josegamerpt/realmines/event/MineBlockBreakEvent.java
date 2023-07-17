package josegamerpt.realmines.event;

import josegamerpt.realmines.mine.RMine;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MineBlockBreakEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final RMine mine;
    private final boolean broken;

    public MineBlockBreakEvent(final RMine m, final boolean broken) {
        this.mine = m;
        this.broken = broken;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public RMine getMine() {
        return this.mine;
    }

    public boolean isBroken() {
        return broken;
    }
}
