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

import joserodpt.realmines.api.config.RMLanguageConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Asks a player to type something, through RealUtils' prompt: a dialog's text box on servers that
 * have them, the chat everywhere else. Kept here, with its own constructors and
 * {@link InputRunnable}, so every screen that asks for input is unchanged.
 */
public class PlayerInput {

    /**
     * Waits for the player to type something, with RealMines' own titles and text box.
     *
     * @param clearInput strip colour codes from what is typed. Without it, {@code &} codes reach
     *                   the runnable as typed, for input such as the prefix that may be coloured
     */
    public PlayerInput(final boolean clearInput, final Player p, final InputRunnable correct, final InputRunnable cancel) {
        this(clearInput, p, null, correct, cancel);
    }

    /** @param dialog the text box's title and description, or null for RealMines' own */
    public PlayerInput(final boolean clearInput, final Player p, final List<String> dialog,
                       final InputRunnable correct, final InputRunnable cancel) {
        new joserodpt.realutils.input.PlayerInput(p, clearInput, null, dialog, correct::run, cancel::run);
    }

    /** Where the prompt's words come from. Called once the plugin is enabled. */
    public static void setup(final Plugin plugin) {
        joserodpt.realutils.input.PlayerInput.setup(plugin,
                p -> RMLanguageConfig.file().getStringList("System.Type-Input"),
                p -> RMLanguageConfig.file().getStringList("System.Type-Input-Dialog"),
                TranslatableLine.SYSTEM_INPUT_CANCELLED::send,
                TranslatableLine.SYSTEM_ERROR_OCCURRED::send);
    }

    public static Listener getListener() {
        return joserodpt.realutils.input.PlayerInput.getListener();
    }

    @FunctionalInterface
    public interface InputRunnable {
        void run(String input);
    }
}
