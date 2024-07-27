package joserodpt.realmines.api.event;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.mine.RMine;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class OnMineResetEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final RMine mine;
    private final RMine.ResetCause resetCause;
    private boolean cancelled = false;


    public OnMineResetEvent(final RMine m, final RMine.ResetCause rc) {
        this.mine = m;
        this.resetCause = rc;
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

    public RMine.ResetCause getResetCause() {
        return resetCause;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
