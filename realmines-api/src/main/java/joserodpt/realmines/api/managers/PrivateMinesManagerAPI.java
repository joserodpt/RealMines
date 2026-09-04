package joserodpt.realmines.api.managers;

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
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Hands players their own copy of a template mine, and keeps track of who owns what.
 */
public abstract class PrivateMinesManagerAPI {

    /**
     * Why a claim didn't go through. {@link #OK} is the only success.
     */
    public enum ClaimResult {
        OK,
        DISABLED,
        NO_PERMISSION,
        LIMIT_REACHED,
        ALREADY_OWNED,
        NO_ECONOMY,
        INSUFFICIENT_FUNDS,
        WORLD_MISSING,
        NO_FREE_SLOT,
        UNSUPPORTED_TYPE,
        ERROR
    }

    public abstract boolean isEnabled();

    /**
     * Reads every template in private-mines/templates/, logging and skipping broken ones.
     */
    public abstract void loadTemplates();

    /**
     * Loads the claimed mines from private-mines/&lt;owner uuid&gt;/, deleting session leftovers and expired
     * ones before they are built, and registers the survivors with the mine manager.
     */
    public abstract void loadInstances();

    public abstract Collection<PrivateMineTemplate> getTemplates();

    public abstract PrivateMineTemplate getTemplate(String id);

    /**
     * Freezes a mine into a reusable template. Overwrites the template if one already has that id.
     */
    public abstract PrivateMineTemplate snapshot(RMine source, String id) throws IllegalArgumentException;

    public abstract boolean deleteTemplate(String id);

    public abstract ClaimResult claim(Player p, PrivateMineTemplate template);

    public abstract List<RMine> getMinesOf(UUID owner);

    public abstract RMine getMineOf(UUID owner, String templateID);

    public abstract List<RMine> getPrivateMines();

    /**
     * Refunds what the config says to refund, clears the region so the slot can be reused, and deletes
     * the mine.
     */
    public abstract void release(RMine instance);

    /**
     * Pushes a time limited mine's expiry out by the template's duration, charging its renew cost.
     */
    public abstract ClaimResult extend(Player p, RMine instance);

    /**
     * Whether this player may break blocks in and teleport to this mine. A null player is never allowed,
     * which is what keeps explosions from eating someone's private mine.
     */
    public abstract boolean canUse(Player p, RMine instance);

    public abstract void purgeExpired();

    /**
     * How many private mines one player may hold at once.
     */
    public abstract int getMaxMinesPerPlayer();


    /**
     * Deletes the session mines of one owner, or of everyone when owner is null.
     */
    public abstract void purgeSessionMines(UUID owner);

    public abstract int nextFreeSlot(PrivateMineTemplate template);
}
