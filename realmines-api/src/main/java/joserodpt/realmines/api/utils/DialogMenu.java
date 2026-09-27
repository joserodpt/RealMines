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
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * A dialog of buttons, one per choice, with a close button under them - a menu of other dialogs,
 * such as the categories of a settings screen.
 *
 * <p>Like {@link DialogForm} this never refers to UniDialog, and shows nothing unless
 * {@link DialogForm#isSupported()}.</p>
 */
public final class DialogMenu {

    /** How many choices one menu can hold: each has a click action registered up front. */
    public static final int MAX_OPTIONS = 18;

    static final class Option {
        final String label;
        final String tooltip;
        final Runnable chosen;

        private Option(final String label, final String tooltip, final Runnable chosen) {
            this.label = label;
            this.tooltip = tooltip;
            this.chosen = chosen;
        }
    }

    final String title;
    final String description;
    final List<Option> options = new ArrayList<>();
    String close = Text.color("&cClose");
    int columns = 1;
    /** Shown above the description, or nothing when null. */
    Material icon;

    public DialogMenu(final String title, final String description) {
        this.title = Text.color(title);
        this.description = Text.color(description);
    }

    /** A button. {@code tooltip} is shown on hover, or nothing when null. */
    public DialogMenu option(final String label, final String tooltip, final Runnable chosen) {
        if (this.options.size() >= MAX_OPTIONS) {
            throw new IllegalStateException("A dialog menu holds at most " + MAX_OPTIONS + " options.");
        }
        this.options.add(new Option(Text.color(label), tooltip == null ? null : Text.color(tooltip), chosen));
        return this;
    }

    public DialogMenu close(final String close) {
        this.close = Text.color(close);
        return this;
    }

    /** An item shown above the description. Paper only: Spigot's dialogs have no item bodies, so they show the text alone. */
    public DialogMenu icon(final Material icon) {
        this.icon = icon;
        return this;
    }

    public DialogMenu columns(final int columns) {
        this.columns = Math.max(1, columns);
        return this;
    }

    /**
     * Shows the menu a tick later, so it can be opened from inside an inventory click. Every callback
     * runs on the main thread; a menu closed with Escape runs none.
     *
     * @param failed run instead if the menu turns out not to be showable after all
     * @return false if dialogs are not supported, in which case nothing is shown and nothing is run
     */
    public boolean open(final Player p, final Runnable closed, final Runnable failed) {
        if (!DialogForm.isSupported()) {
            return false;
        }
        p.closeInventory();
        RealMinesAPI.getInstance().getPlugin().getServer().getScheduler().runTask(RealMinesAPI.getInstance().getPlugin(), () -> {
            if (p.isOnline() && !DialogInput.openMenu(p, this, closed)) {
                failed.run();
            }
        });
        return true;
    }
}
