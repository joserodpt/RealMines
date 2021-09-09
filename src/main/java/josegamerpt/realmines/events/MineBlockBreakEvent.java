package josegamerpt.realmines.events;

import josegamerpt.realmines.mines.RMine;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MineBlockBreakEvent extends Event {
	
    private static final HandlerList HANDLERS = new HandlerList();
    
    public final RMine mine;

    public MineBlockBreakEvent(RMine m) {
        this.mine = m;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    
    public RMine getMine() {
        return this.mine;
    }

}
