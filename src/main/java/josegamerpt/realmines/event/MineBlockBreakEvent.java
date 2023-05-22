package josegamerpt.realmines.event;

import josegamerpt.realmines.mine.RMine;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MineBlockBreakEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public final RMine mine;

    public MineBlockBreakEvent(final RMine m) {
        this.mine = m;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public RMine getMine() {
        return this.mine;
    }

}
