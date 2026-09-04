package joserodpt.realmines.api.mine.components;

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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Ownership record of a private mine. Lives in the {@code private:} section of the mine's own file,
 * which is what tells RealMines that a mine belongs to a player instead of to the server.
 */
public class PrivateMineData {

    public enum Lifecycle {
        /**
         * Kept until the owner releases it or an admin deletes it.
         */
        PERSISTENT,
        /**
         * Deleted once {@link #getExpiresAt()} passes. Survives restarts until then.
         */
        TIME_LIMITED,
        /**
         * Deleted when the owner logs out, and on the next boot if the server crashed first.
         */
        SESSION;

        public static Lifecycle fromString(final String s, final Lifecycle def) {
            if (s == null) {
                return def;
            }
            try {
                return valueOf(s.toUpperCase());
            } catch (final IllegalArgumentException e) {
                return def;
            }
        }
    }

    public static final String ROOT = "private";
    public static final long NEVER = -1L;

    private final String template;
    private final UUID owner;
    private String ownerName;
    private final int slot;
    private final Lifecycle lifecycle;
    private final long createdAt;
    private long expiresAt;
    private double paid;
    private int platformWidth;
    private final Set<UUID> trusted = new LinkedHashSet<>();

    public PrivateMineData(final String template, final UUID owner, final String ownerName, final int slot,
                           final Lifecycle lifecycle, final long createdAt, final long expiresAt, final double paid) {
        this.template = template;
        this.owner = owner;
        this.ownerName = ownerName;
        this.slot = slot;
        this.lifecycle = lifecycle;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.paid = paid;
    }

    /**
     * Reads the {@code private:} section of a mine file, or returns null when the mine isn't a private one.
     */
    public static PrivateMineData load(final FileConfiguration config) {
        final ConfigurationSection section = config.getConfigurationSection(ROOT);
        if (section == null) {
            return null;
        }

        final String owner = section.getString("owner");
        final String template = section.getString("template");
        if (owner == null || template == null) {
            return null;
        }

        final UUID ownerUUID;
        try {
            ownerUUID = UUID.fromString(owner);
        } catch (final IllegalArgumentException e) {
            return null;
        }

        final PrivateMineData data = new PrivateMineData(template, ownerUUID, section.getString("owner-name", ""),
                section.getInt("slot", 0),
                Lifecycle.fromString(section.getString("lifecycle"), Lifecycle.PERSISTENT),
                section.getLong("created-at", System.currentTimeMillis() / 1000L),
                section.getLong("expires-at", NEVER),
                section.getDouble("paid", 0D));

        data.platformWidth = section.getInt("platform-width", 0);

        for (final String trusted : section.getStringList("trusted")) {
            try {
                data.trusted.add(UUID.fromString(trusted));
            } catch (final IllegalArgumentException ignored) {
                //a hand-edited file, skip the entry rather than dropping the whole mine
            }
        }

        return data;
    }

    /**
     * Writes this record into a mine config. Does not save the file - the caller decides when to flush,
     * because {@code RMine#saveData} restarts the reset timer and this must not.
     */
    public void save(final FileConfiguration config) {
        config.set(ROOT + ".template", this.template);
        config.set(ROOT + ".owner", this.owner.toString());
        config.set(ROOT + ".owner-name", this.ownerName);
        config.set(ROOT + ".slot", this.slot);
        config.set(ROOT + ".lifecycle", this.lifecycle.name());
        config.set(ROOT + ".created-at", this.createdAt);
        config.set(ROOT + ".expires-at", this.expiresAt);
        config.set(ROOT + ".paid", this.paid);
        config.set(ROOT + ".platform-width", this.platformWidth);
        config.set(ROOT + ".trusted", this.trusted.stream().map(UUID::toString).collect(Collectors.toList()));
    }

    public String getTemplate() {
        return this.template;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public void setOwnerName(final String ownerName) {
        this.ownerName = ownerName;
    }

    public int getSlot() {
        return this.slot;
    }

    public Lifecycle getLifecycle() {
        return this.lifecycle;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public long getExpiresAt() {
        return this.expiresAt;
    }

    public void setExpiresAt(final long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public double getPaid() {
        return this.paid;
    }

    public void setPaid(final double paid) {
        this.paid = paid;
    }

    /**
     * How wide the walkway built around this mine is, or 0 when it was given none.
     * <p>
     * Remembered per mine rather than read back off the template, because the platform has to be taken
     * away exactly as it was put down - even if the template was widened or deleted since.
     */
    public int getPlatformWidth() {
        return this.platformWidth;
    }

    public void setPlatformWidth(final int platformWidth) {
        this.platformWidth = platformWidth;
    }

    public boolean hasExpired() {
        return this.expiresAt != NEVER && System.currentTimeMillis() / 1000L >= this.expiresAt;
    }

    /**
     * Seconds until this mine expires, or -1 when it never does.
     */
    public long getSecondsLeft() {
        return this.expiresAt == NEVER ? NEVER : Math.max(0L, this.expiresAt - System.currentTimeMillis() / 1000L);
    }

    public boolean isOwner(final UUID uuid) {
        return this.owner.equals(uuid);
    }

    public boolean isOwner(final Player p) {
        return p != null && this.isOwner(p.getUniqueId());
    }

    public boolean isTrusted(final UUID uuid) {
        return this.trusted.contains(uuid);
    }

    public boolean addTrusted(final UUID uuid) {
        return !this.owner.equals(uuid) && this.trusted.add(uuid);
    }

    public boolean removeTrusted(final UUID uuid) {
        return this.trusted.remove(uuid);
    }

    public Set<UUID> getTrusted() {
        return Collections.unmodifiableSet(this.trusted);
    }

    public List<UUID> getTrustedList() {
        return new ArrayList<>(this.trusted);
    }

    public int getTrustedCount() {
        return this.trusted.size();
    }
}
