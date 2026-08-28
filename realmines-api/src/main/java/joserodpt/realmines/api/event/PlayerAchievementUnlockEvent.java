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

import joserodpt.realmines.api.achievements.RMAchievement;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired right before an achievement's rewards are handed out. Cancelling it stops the rewards, the
 * message and the unlock from being recorded, so the player can still earn it later.
 */
public class PlayerAchievementUnlockEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final RMAchievement achievement;
    private boolean cancelled;

    public PlayerAchievementUnlockEvent(final Player player, final RMAchievement achievement) {
        this.player = player;
        this.achievement = achievement;
    }

    public Player getPlayer() {
        return this.player;
    }

    public RMAchievement getAchievement() {
        return this.achievement;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
