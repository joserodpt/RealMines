package joserodpt.realmines.plugin.command;

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

/**
 * The tab completion sources RealMines commands share. Each one is wired to a provider in
 * {@link RMCommandManager}, and pulled onto a parameter with {@link SuggestFrom}.
 */
public enum RMSuggestion {
    /** Mine names that are free to type at {@code /rm create}, not the ones that exist. */
    NEW_MINE_NAMES,
    /** Reset task names to type at {@code /rmrt create}, same idea as {@link #NEW_MINE_NAMES}. */
    NEW_TASK_NAMES,
    /** The mine types {@code /rm create} accepts, long and short spellings both. */
    MINE_TYPES,
    /** Plugins RealMines can import mines from. */
    CONVERTERS,
    /** Every registered mine, private ones included. */
    MINES,
    /** Online players only, so an unknown name never costs a Mojang lookup. */
    PLAYERS,
    /** Every private mine template. */
    PRIVATE_TEMPLATES,
    /** Only the templates the caller already holds a mine of. */
    OWNED_TEMPLATES,
    /** Every registered mine reset task. */
    RESET_TASKS,
    /** The countdown already set on the mine named earlier in the same command. */
    MINE_COUNTDOWN
}
