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
 * @author José Rodrigues © 2019-2026
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.mine.RMine;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RealMinesBlockBreakEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Cancellable cancellable;
    private final RMine mine;
    private final boolean broken;
    private final Block b;
    private final Player p;

    public RealMinesBlockBreakEvent(final Cancellable cancellable, final Player p, final RMine m, final Block b, final boolean broken) {
        this.cancellable = cancellable;
        this.p = p;
        this.mine = m;
        this.b = b;
        this.broken = broken;
    }

    public Cancellable getCancellable() {
        return cancellable;
    }

    public Block getBlock() {
        return b;
    }

    public Material getMaterial() {
        return b.getType();
    }

    public Player getPlayer() {
        return p;
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
