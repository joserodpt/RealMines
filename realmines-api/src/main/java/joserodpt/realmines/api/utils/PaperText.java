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
 * @author José Rodrigues © 2019-2026
 * @link https://github.com/joserodpt/RealMines
 */

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.function.Function;

/**
 * How the Paper dialog backend turns RealMines' text into components: {@code §} colours as
 * before, plus a sprite wherever {@link DialogSprites#marker} marked one.
 *
 * <p>Paper's Adventure only, so this is loaded by name and only on Paper - Spigot has neither the
 * classes nor, before 1.21.9, the sprites.</p>
 */
final class PaperText implements Function<String, Component> {

    private static final char START = DialogSprites.START;
    private static final char SEPARATOR = DialogSprites.SEPARATOR;
    private static final char END = DialogSprites.END;

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Override
    public Component apply(final String text) {
        if (text.indexOf(START) < 0) {
            return LEGACY.deserialize(text);
        }

        final TextComponent.Builder built = Component.text();
        int at = 0;
        while (at < text.length()) {
            final int start = text.indexOf(START, at);
            final int separator = start < 0 ? -1 : text.indexOf(SEPARATOR, start);
            final int end = separator < 0 ? -1 : text.indexOf(END, separator);
            if (end < 0) {
                built.append(LEGACY.deserialize(text.substring(at)));
                break;
            }
            if (start > at) {
                built.append(LEGACY.deserialize(text.substring(at, start)));
            }
            built.append(Component.object(ObjectContents.sprite(
                    Key.key(text.substring(start + 1, separator)), Key.key(text.substring(separator + 1, end)))));
            at = end + 1;
        }
        return built.build();
    }
}
