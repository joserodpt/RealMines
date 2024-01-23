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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.mine.RMine;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MineBlockBreakEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final RMine mine;
    private final boolean broken;
    private final Block b;
    private final Player p;

    public MineBlockBreakEvent(final Player p, final RMine m, final Block b, final boolean broken) {
        this.p = p;
        this.mine = m;
        this.b = b;
        this.broken = broken;
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
