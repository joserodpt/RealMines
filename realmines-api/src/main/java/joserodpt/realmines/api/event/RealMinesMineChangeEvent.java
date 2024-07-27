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
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RealMinesMineChangeEvent extends Event {

    public enum ChangeOperation {ADDED, REMOVED, BOUNDS_UPDATED}

    private static final HandlerList HANDLERS = new HandlerList();

    private final RMine mine;
    private final ChangeOperation co;

    public RealMinesMineChangeEvent(final RMine m, final ChangeOperation co) {
        this.mine = m;
        this.co = co;
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

    public ChangeOperation getChangeOperation() {
        return co;
    }
}
