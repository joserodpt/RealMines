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

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.TranslatableLine;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * A form shown in a dialog, on servers that have them (1.21.6 and up): a title, a description, and
 * any number of text boxes, switches and sliders, with a confirm and a cancel button.
 *
 * <p>Nothing here refers to UniDialog, so this can be used anywhere; check {@link #isSupported()}
 * and keep an inventory or chat version for servers without dialogs.</p>
 */
public final class DialogForm {

    /**
     * Whether dialogs are in use. Nothing touches {@link DialogInput} unless this is set: it refers
     * to UniDialog, which is built for Java 21, so on an older server even loading it fails.
     */
    private static boolean supported = false;

    enum Kind {TEXT, TOGGLE, SLIDER}

    static final class Field {
        final Kind kind;
        final String key;
        final String label;
        final String text;
        final boolean toggled;
        final int maxLength;
        final float min;
        final float max;
        final float step;
        final float value;

        private Field(final Kind kind, final String key, final String label, final String text, final boolean toggled,
                      final int maxLength, final float min, final float max, final float step, final float value) {
            this.kind = kind;
            this.key = key;
            this.label = label;
            this.text = text;
            this.toggled = toggled;
            this.maxLength = maxLength;
            this.min = min;
            this.max = max;
            this.step = step;
            this.value = value;
        }
    }

    final String title;
    final String description;
    final List<Field> fields = new ArrayList<>();
    String confirm = TranslatableLine.SYSTEM_DIALOG_CONFIRM.get();
    String cancel = TranslatableLine.SYSTEM_DIALOG_CANCEL.get();
    boolean closeWithEscape = true;

    public DialogForm(final String title, final String description) {
        this.title = Text.color(title);
        this.description = Text.color(description);
    }

    /** A text box. {@code initial} is shown as typed, colour codes and all. */
    public DialogForm text(final String key, final String label, final String initial, final int maxLength) {
        this.fields.add(new Field(Kind.TEXT, key, Text.color(label), initial == null ? "" : initial, false, maxLength, 0, 0, 0, 0));
        return this;
    }

    public DialogForm toggle(final String key, final String label, final boolean initial) {
        this.fields.add(new Field(Kind.TOGGLE, key, Text.color(label), null, initial, 0, 0, 0, 0, 0));
        return this;
    }

    /** A slider from {@code min} to {@code max} in steps of {@code step}, starting at {@code initial}. */
    public DialogForm slider(final String key, final String label, final int min, final int max, final int step, final int initial) {
        return this.slider(key, label, (float) min, (float) max, (float) step, (float) initial);
    }

    /** A slider that can stop between whole numbers; read it back with {@link Answers#decimal}. */
    public DialogForm slider(final String key, final String label, final float min, final float max, final float step, final float initial) {
        this.fields.add(new Field(Kind.SLIDER, key, Text.color(label), null, false, 0, min, max, step,
                Math.max(min, Math.min(max, initial))));
        return this;
    }

    public DialogForm buttons(final String confirm, final String cancel) {
        this.confirm = Text.color(confirm);
        this.cancel = Text.color(cancel);
        return this;
    }

    /**
     * Whether Escape may close it. Closing that way tells the server nothing, so a form whose
     * caller is waiting on an answer should turn this off.
     */
    public DialogForm closeWithEscape(final boolean closeWithEscape) {
        this.closeWithEscape = closeWithEscape;
        return this;
    }

    /**
     * Shows the form, a tick later so it can be opened from inside an inventory click. Every
     * callback runs on the main thread.
     *
     * @param failed run instead if the form turns out not to be showable after all, so the caller
     *               can fall back to its inventory or chat version
     * @return false if dialogs are not supported, in which case nothing is shown and nothing is run
     */
    public boolean open(final Player p, final Consumer<Answers> confirmed, final Runnable cancelled, final Runnable failed) {
        if (!supported) {
            return false;
        }
        p.closeInventory();
        RealMinesAPI.getInstance().getPlugin().getServer().getScheduler().runTask(RealMinesAPI.getInstance().getPlugin(), () -> {
            if (p.isOnline() && !DialogInput.open(p, this, values -> confirmed.accept(new Answers(values)), cancelled)) {
                failed.run();
            }
        });
        return true;
    }

    /** Takes whatever form is on this player's screen away, and forgets its callbacks. */
    public static void close(final UUID uuid) {
        if (supported) {
            DialogInput.close(uuid);
        }
    }

    public static boolean isSupported() {
        return supported;
    }

    /** Uses dialogs from here on if the server has them. Called once the plugin is enabled. */
    public static void setup(final Plugin plugin) {
        shutdown();
        //dialogs came with 1.21.6, which already needs Java 21
        if (Runtime.version().feature() < 21) {
            return;
        }
        try {
            supported = DialogInput.setup(plugin);
        } catch (final Throwable e) {
            plugin.getLogger().log(Level.WARNING, "Dialogs are not available, so the inventory and chat prompts are used instead.", e);
        }
    }

    public static void shutdown() {
        if (supported) {
            supported = false;
            DialogInput.shutdown();
        }
    }

    /** What the player sent back, by the keys the fields were added with. */
    public static final class Answers {
        private final Map<String, String> values;

        Answers(final Map<String, String> values) {
            this.values = values == null ? Collections.emptyMap() : values;
        }

        public String text(final String key, final String fallback) {
            final String value = this.values.get(key);
            return value == null ? fallback : value;
        }

        public boolean toggle(final String key, final boolean fallback) {
            final String value = this.values.get(key);
            if (value == null) {
                return fallback;
            }
            //Spigot sends true/false; Paper can send a switch as the number it is stored as
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(value);
            }
            try {
                return Float.parseFloat(value.replaceAll("[bB]$", "")) != 0;
            } catch (final NumberFormatException e) {
                return fallback;
            }
        }

        public double decimal(final String key, final double fallback) {
            final String value = this.values.get(key);
            if (value == null) {
                return fallback;
            }
            try {
                return Double.parseDouble(value);
            } catch (final NumberFormatException e) {
                return fallback;
            }
        }

        /**
         * A slider's value if the player moved it, or null if it still sits where it started. A
         * starting value between two steps can come back as the nearest step, which counts as
         * unmoved too, so saving a form never rounds a value nobody touched.
         */
        public Double moved(final String key, final double initial, final double step) {
            final double value = this.decimal(key, initial);
            final double snapped = step > 0 ? Math.round(initial / step) * step : initial;
            if (Math.abs(value - initial) < 1.0E-4 || Math.abs(value - snapped) < 1.0E-4) {
                return null;
            }
            return value;
        }

        public int number(final String key, final int fallback) {
            final String value = this.values.get(key);
            if (value == null) {
                return fallback;
            }
            try {
                return Math.round(Float.parseFloat(value));
            } catch (final NumberFormatException e) {
                return fallback;
            }
        }
    }
}
