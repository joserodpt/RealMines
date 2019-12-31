package josegamerpt.realmines.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import josegamerpt.realmines.classes.Mine;

public class MineBlockBreakEvent extends Event {
	
    private static final HandlerList HANDLERS = new HandlerList();
    
    public final Mine mine;

    public MineBlockBreakEvent(Mine m) {
        this.mine = m;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    
    public Mine getMine() {
        return this.mine;
    }

}
