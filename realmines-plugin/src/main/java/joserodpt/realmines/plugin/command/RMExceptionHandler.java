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

import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.utils.Text;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.MissingArgumentException;
import revxrsal.commands.exception.NoPermissionException;
import revxrsal.commands.exception.UnknownCommandException;
import revxrsal.commands.node.ParameterNode;

/**
 * Puts the errors Lamp raises through RealMines' own language file, so a mistyped command reads the
 * same as every other message the plugin sends. Anything not overridden here keeps Lamp's own
 * wording, which is already specific about what it couldn't parse.
 */
public class RMExceptionHandler extends BukkitExceptionHandler {

    @Override
    public void onUnknownCommand(final UnknownCommandException e, final BukkitCommandActor actor) {
        TranslatableLine.SYSTEM_ERROR_COMMAND.send(actor.sender());
    }

    @Override
    public void onNoPermission(final NoPermissionException e, final BukkitCommandActor actor) {
        TranslatableLine.SYSTEM_ERROR_PERMISSION.send(actor.sender());
    }

    /**
     * Player-only commands take a {@link org.bukkit.entity.Player} instead of a
     * {@link org.bukkit.command.CommandSender}, and this is where console gets told so.
     */
    @Override
    public void onSenderNotPlayer(final SenderNotPlayerException e, final BukkitCommandActor actor) {
        TranslatableLine.SYSTEM_PLAYER_ONLY.send(actor.sender());
    }

    @Override
    public void onMissingArgument(final MissingArgumentException e, final BukkitCommandActor actor,
                                  final ParameterNode<BukkitCommandActor, ?> parameter) {
        Text.send(actor.sender(), usageOf(e.command()));
    }

    /**
     * The handwritten {@link Usage} on the method, which spells the command out the way players are
     * used to seeing it. Commands without one fall back to the generic line, as they always did.
     */
    private static String usageOf(final ExecutableCommand<?> command) {
        final Usage usage = command.annotations().get(Usage.class);
        return usage == null ? TranslatableLine.SYSTEM_ERROR_USAGE.get() : usage.value();
    }
}
