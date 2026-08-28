package joserodpt.realmines.api.utils;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.utils.skulls.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Player heads that actually show the right skin.
 * <p>
 * {@code SkullMeta.setOwningPlayer} only resolves a texture when the server already holds that
 * player's profile, which is why heads come out blank for anyone who isn't online, and on offline
 * mode or proxied servers. The fix is to hold the real Mojang texture and apply that instead.
 * <p>
 * Textures are fetched off the main thread and kept here, so building a head is always instant and
 * never does any network work. A head asked for before its texture has arrived falls back to the
 * plain Bukkit profile, and {@link #preload(Collection, Runnable)} exists so a GUI can redraw once
 * the real ones land.
 * <p>
 * The applying itself is left to {@link SkullCreator}, which already picks the right approach for
 * the running server version.
 */
public final class PlayerHeads {

    private static final String MOJANG_PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    /**
     * How long to leave a player alone after a failed lookup. Mojang rate limits this endpoint, and
     * without this a leaderboard full of unresolvable players would retry every time it is opened.
     */
    private static final long RETRY_AFTER_MS = 10 * 60 * 1000L;

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 3000;

    private static final Map<UUID, String> TEXTURES = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> FAILED = new ConcurrentHashMap<>();

    /**
     * Stops several players opening the same leaderboard from all fetching the same profiles.
     */
    private static final Set<UUID> IN_FLIGHT = ConcurrentHashMap.newKeySet();

    private PlayerHeads() {
    }

    /**
     * A head for this player, named and with the given lore.
     * <p>
     * Instant and safe on the main thread. Uses the real skin when it is known, and the plain
     * Bukkit profile otherwise.
     */
    public static ItemStack getHead(final UUID uuid, final String name, final List<String> lore) {
        ItemStack head = null;

        final String texture = TEXTURES.get(uuid);
        if (texture != null) {
            try {
                head = SkullCreator.itemFromBase64(texture);
            } catch (final Exception e) {
                //a texture we can't apply is worse than none, so fall through to the plain profile
                TEXTURES.remove(uuid);
            }
        }

        if (head == null) {
            head = SkullCreator.itemFromUuid(uuid);
        }

        return Items.changeItemStack(name, lore, head);
    }

    /**
     * Whether this player's real skin is already known, so a head for them will be correct.
     */
    public static boolean hasTexture(final UUID uuid) {
        return TEXTURES.containsKey(uuid);
    }

    /**
     * Fetches whichever of these players we don't have a skin for yet, in one background task, then
     * runs the callback on the main thread if any of them arrived.
     * <p>
     * Meant for a GUI to call as it opens: draw immediately with whatever is known, and redraw once
     * from the callback. The callback does not run when there was nothing to fetch, so a GUI whose
     * heads were all cached never redraws for nothing.
     */
    public static void preload(final Collection<UUID> uuids, final Runnable onLoaded) {
        final long now = System.currentTimeMillis();

        final List<UUID> missing = new ArrayList<>();
        for (final UUID uuid : uuids) {
            if (uuid == null || TEXTURES.containsKey(uuid)) {
                continue;
            }
            final Long failedAt = FAILED.get(uuid);
            if (failedAt != null && now - failedAt < RETRY_AFTER_MS) {
                continue;
            }
            if (IN_FLIGHT.add(uuid)) {
                missing.add(uuid);
            }
        }

        if (missing.isEmpty()) {
            return;
        }

        runAsync(() -> {
            boolean any = false;
            try {
                for (final UUID uuid : missing) {
                    //one at a time on purpose: this endpoint is rate limited, and a leaderboard
                    //asking for its whole page at once is exactly what would trip it
                    if (fetch(uuid)) {
                        any = true;
                    }
                }
            } finally {
                IN_FLIGHT.removeAll(missing);
            }

            if (any && onLoaded != null) {
                runSync(onLoaded);
            }
        });
    }

    /**
     * Warms this player's skin in the background, so heads for them are right from the first time
     * anyone opens a GUI showing them.
     * <p>
     * RealMines does not call this on join. Mojang rate limits the profile endpoint, and warming
     * every player who logs in spends a request on a head that may never be shown - the GUIs fetch
     * what they display and redraw instead. Offered here for anyone who would rather pay that cost
     * up front for instant heads.
     */
    public static void cache(final Player player) {
        if (player != null) {
            preload(java.util.Collections.singletonList(player.getUniqueId()), null);
        }
    }

    /**
     * Drops a cached skin, so a player who changed theirs gets the new one on their next join.
     */
    public static void forget(final UUID uuid) {
        TEXTURES.remove(uuid);
        FAILED.remove(uuid);
    }

    /**
     * @return true if a texture was stored
     */
    private static boolean fetch(final UUID uuid) {
        HttpURLConnection connection = null;
        try {
            final URL url = new URL(MOJANG_PROFILE_URL + uuid.toString().replace("-", "") + "?unsigned=false");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                //204 means no such profile, 429 means we are going too fast. Both are worth backing
                //off from rather than asking again on the next GUI open.
                FAILED.put(uuid, System.currentTimeMillis());
                return false;
            }

            //the instance method rather than the static JsonParser.parseString: the latter only
            //exists from gson 2.8.6, and this plugin still supports servers that ship an older one
            @SuppressWarnings("deprecation")
            final JsonObject profile = new JsonParser().parse(read(connection)).getAsJsonObject();
            if (profile.has("properties")) {
                for (final JsonElement element : profile.getAsJsonArray("properties")) {
                    final JsonObject property = element.getAsJsonObject();
                    if ("textures".equals(property.get("name").getAsString())) {
                        TEXTURES.put(uuid, property.get("value").getAsString());
                        FAILED.remove(uuid);
                        return true;
                    }
                }
            }

            FAILED.put(uuid, System.currentTimeMillis());
            return false;
        } catch (final Exception e) {
            //offline server, no internet, blocked outbound: heads stay vanilla, nothing else breaks
            FAILED.put(uuid, System.currentTimeMillis());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String read(final HttpURLConnection connection) throws IOException {
        try (InputStream in = connection.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final byte[] buffer = new byte[8192];
            int count;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static void runAsync(final Runnable runnable) {
        if (RealMinesAPI.getInstance().getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(RealMinesAPI.getInstance().getPlugin(), runnable);
        }
    }

    private static void runSync(final Runnable runnable) {
        if (RealMinesAPI.getInstance().getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTask(RealMinesAPI.getInstance().getPlugin(), runnable);
        }
    }
}
