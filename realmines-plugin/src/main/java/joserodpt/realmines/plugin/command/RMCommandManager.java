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

import joserodpt.realmines.api.converters.RMSupportedConverters;
import joserodpt.realmines.api.managers.PrivateMineTemplate;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.plugin.RealMines;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.Lamp;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Builds the Lamp instance every RealMines command hangs off, and hands it the shared tab
 * completions and the language-file error messages. Lamp registers the commands straight onto the
 * server's command map, which is why none of them appear in plugin.yml.
 */
public final class RMCommandManager {

    private final Lamp<BukkitCommandActor> lamp;

    public RMCommandManager(final RealMines rm) {
        final Map<RMSuggestion, SuggestionProvider<BukkitCommandActor>> suggestions = suggestions(rm);

        //Brigadier stays on. Lamp's own matcher treats leftover input as merely a worse match, so
        //`/rm reload junk` would quietly fall back to the bare `/rm` handler; Brigadier's tree
        //refuses it outright. Where it can't attach (Spigot on 1.19.1+) Lamp falls back on its own
        //and RMExceptionHandler's @Usage messages are what players see instead.
        this.lamp = BukkitLamp.builder(rm.getPlugin())
                .exceptionHandler(new RMExceptionHandler())
                .suggestionProviders(providers -> providers.addProviderForAnnotation(
                        SuggestFrom.class, annotation -> suggestions.get(annotation.value())))
                .build();

        this.lamp.register(
                new MineCMD(rm),
                new MineResetTaskCMD(rm),
                new PrivateMineCMD(rm));
    }

    private static Map<RMSuggestion, SuggestionProvider<BukkitCommandActor>> suggestions(final RealMines rm) {
        final Map<RMSuggestion, SuggestionProvider<BukkitCommandActor>> sources = new EnumMap<>(RMSuggestion.class);

        sources.put(RMSuggestion.NEW_MINE_NAMES, context -> IntStream.range(0, 100)
                .mapToObj(i -> "Mine" + i)
                .collect(Collectors.toList()));

        sources.put(RMSuggestion.NEW_TASK_NAMES, context -> IntStream.range(0, 50)
                .mapToObj(i -> "MineResetTask" + i)
                .collect(Collectors.toList()));

        sources.put(RMSuggestion.MINE_TYPES,
                SuggestionProvider.of("b", "s", "f", "blocks", "farm", "schem", "schematic"));

        sources.put(RMSuggestion.CONVERTERS, context -> Arrays.stream(RMSupportedConverters.values())
                .map(RMSupportedConverters::getSourceName)
                .collect(Collectors.toList()));

        sources.put(RMSuggestion.MINES, context -> rm.getMineManager().getRegisteredMines());

        sources.put(RMSuggestion.PLAYERS, context -> Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList()));

        sources.put(RMSuggestion.PRIVATE_TEMPLATES, context -> rm.getPrivateMinesManager().getTemplates().stream()
                .map(PrivateMineTemplate::getID)
                .collect(Collectors.toList()));

        sources.put(RMSuggestion.OWNED_TEMPLATES, context -> {
            final Player p = context.actor().asPlayer();
            if (p == null) {
                return Collections.emptyList();
            }
            return rm.getPrivateMinesManager().getMinesOf(p.getUniqueId()).stream()
                    .map(mine -> mine.getPrivateData().getTemplate())
                    .collect(Collectors.toList());
        });

        sources.put(RMSuggestion.RESET_TASKS, context -> rm.getMineResetTasksManager().getRegisteredTasks());

        //reads the mine off the argument before it, which every command using this names "name"
        sources.put(RMSuggestion.MINE_COUNTDOWN, context -> {
            final String mineName = context.getResolvedArgumentOrNull("name");
            if (mineName == null) {
                return Collections.emptyList();
            }
            final RMine mine = rm.getMineManager().getMine(mineName);
            if (mine == null) {
                return Collections.emptyList();
            }
            //null whenever the mine has no timer or no countdown on it
            final Integer countdown = mine.getCountdown();
            return countdown == null ? Collections.emptyList() : List.of(countdown.toString());
        });

        return sources;
    }

    public Lamp<BukkitCommandActor> getLamp() {
        return this.lamp;
    }
}
