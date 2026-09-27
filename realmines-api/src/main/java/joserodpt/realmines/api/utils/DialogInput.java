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

import io.github.projectunified.unidialog.core.DialogManager;
import io.github.projectunified.unidialog.core.action.DialogActionBuilder;
import io.github.projectunified.unidialog.core.body.DialogBodyBuilder;
import io.github.projectunified.unidialog.core.body.ItemBody;
import io.github.projectunified.unidialog.core.body.TextBody;
import io.github.projectunified.unidialog.core.dialog.ConfirmationDialog;
import io.github.projectunified.unidialog.core.dialog.Dialog;
import io.github.projectunified.unidialog.core.dialog.MultiActionDialog;
import io.github.projectunified.unidialog.core.input.BooleanInput;
import io.github.projectunified.unidialog.core.input.DialogInputBuilder;
import io.github.projectunified.unidialog.core.input.NumberRangeInput;
import io.github.projectunified.unidialog.core.input.TextInput;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shows a {@link DialogForm} through UniDialog.
 *
 * <p>UniDialog is built for Java 21 and the 1.21.6 API, while this plugin still loads on servers
 * much older than that. So only {@link DialogForm} talks to this class, and only once
 * {@link #setup} has found the server has dialogs; the platform's manager is created by name, so
 * neither backend is loaded on the platform it is not for.</p>
 */
final class DialogInput {

    private static final String PAPER_MANAGER = "io.github.projectunified.unidialog.paper.PaperDialogManager";
    private static final String SPIGOT_MANAGER = "io.github.projectunified.unidialog.spigot.SpigotDialogManager";

    private static final String SUBMIT = "form_submit";
    private static final String CANCEL = "form_cancel";
    /** Followed by the option's index: a menu's buttons, which send nothing else to tell them apart. */
    private static final String CHOICE = "menu_choice_";

    /** The form or menu each player has on screen: what its buttons do. */
    private static final Map<UUID, Pending> pending = new ConcurrentHashMap<>();

    @SuppressWarnings("rawtypes")
    private static DialogManager manager;
    private static Plugin owner;
    /** Paper's backend rather than Spigot's, which cannot show items. */
    private static boolean paper;
    private static Logger logger;

    private static final class Pending {
        private final Consumer<Map<String, String>> confirmed;
        private final Runnable cancelled;
        /** A menu's options, in order; empty for a form. */
        private final List<Runnable> choices;

        private Pending(final Consumer<Map<String, String>> confirmed, final Runnable cancelled, final List<Runnable> choices) {
            this.confirmed = confirmed;
            this.cancelled = cancelled;
            this.choices = choices;
        }
    }

    private DialogInput() {
    }

    /** @return whether the server has dialogs and they are now in use */
    static boolean setup(final Plugin plugin) {
        owner = plugin;
        logger = plugin.getLogger();
        final String managerClass = hasClass("io.papermc.paper.dialog.Dialog") ? PAPER_MANAGER
                : hasClass("net.md_5.bungee.api.dialog.Dialog") && hasClass("org.bukkit.event.player.PlayerCustomClickEvent") ? SPIGOT_MANAGER
                : null;
        if (managerClass == null) {
            return false;
        }

        try {
            @SuppressWarnings("rawtypes") final DialogManager created = (DialogManager) Class.forName(managerClass)
                    .getConstructor(Plugin.class).newInstance(plugin);
            created.registerCustomAction(SUBMIT, (BiConsumer<UUID, Map<String, String>>) (uuid, values) -> answer(uuid, values, false));
            created.registerCustomAction(CANCEL, (BiConsumer<UUID, Map<String, String>>) (uuid, values) -> answer(uuid, values, true));
            for (int i = 0; i < DialogMenu.MAX_OPTIONS; i++) {
                final int option = i;
                created.registerCustomAction(CHOICE + i, (BiConsumer<UUID, Map<String, String>>) (uuid, values) -> choose(uuid, option));
            }
            created.register();
            manager = created;
            paper = managerClass.equals(PAPER_MANAGER);
            return true;
        } catch (final Throwable e) {
            //a LinkageError too: a server whose dialog API is not the one UniDialog was built for
            //keeps the inventory and chat prompts rather than failing to enable
            logger.log(Level.WARNING, "Dialogs are not available, so the inventory and chat prompts are used instead.", e);
            return false;
        }
    }

    private static void answer(final UUID uuid, final Map<String, String> values, final boolean cancelled) {
        //the dialog's events are not promised to arrive on the main thread
        Bukkit.getScheduler().runTask(owner, () -> {
            //taken in one step, so a double click cannot answer the same form twice
            final Pending current = pending.remove(uuid);
            if (current == null) {
                return;
            }
            if (cancelled) {
                current.cancelled.run();
            } else {
                current.confirmed.accept(values);
            }
        });
    }

    private static void choose(final UUID uuid, final int option) {
        Bukkit.getScheduler().runTask(owner, () -> {
            final Pending current = pending.remove(uuid);
            if (current != null && option < current.choices.size()) {
                current.choices.get(option).run();
            }
        });
    }

    /** @return false if it could not be shown */
    @SuppressWarnings({"rawtypes", "unchecked"})
    static boolean openMenu(final Player p, final DialogMenu menu, final Runnable closed) {
        if (manager == null) {
            return false;
        }
        try {
            final MultiActionDialog dialog = manager.createMultiActionDialog();
            dialog.title(menu.title);
            //the icon first, so it sits above the description
            if (menu.icon != null && paper) {
                dialog.body((Consumer<DialogBodyBuilder>) body -> {
                    final ItemBody item = body.item();
                    item.item(new ItemStack(menu.icon));
                    item.showTooltip(false);
                });
            }
            if (!menu.description.isEmpty()) {
                dialog.body((Consumer<DialogBodyBuilder<?>>) body -> {
                    final TextBody<?> text = body.text();
                    text.text(menu.description);
                });
            }
            final List<Runnable> choices = new ArrayList<>();
            for (int i = 0; i < menu.options.size(); i++) {
                final DialogMenu.Option option = menu.options.get(i);
                final String id = CHOICE + i;
                dialog.action((Consumer<DialogActionBuilder<?, ?>>) action -> {
                    action.label(option.label);
                    action.tooltip(option.tooltip);
                    action.width(MENU_WIDTH);
                    action.dynamicCustom(id);
                });
                choices.add(option.chosen);
            }
            //always given, never left null for the backend to pass on
            dialog.exitAction((Consumer<DialogActionBuilder<?, ?>>) action -> {
                action.label(menu.close);
                action.width(MENU_WIDTH);
                action.dynamicCustom(CANCEL);
            });
            dialog.columns(menu.columns);
            dialog.canCloseWithEscape(true);
            dialog.afterAction(Dialog.AfterAction.CLOSE);

            pending.put(p.getUniqueId(), new Pending(values -> { }, closed, choices));
            if (dialog.opener().open(p.getUniqueId())) {
                return true;
            }
            pending.remove(p.getUniqueId());
            return false;
        } catch (final Throwable e) {
            pending.remove(p.getUniqueId());
            logger.log(Level.WARNING, "Couldn't show a dialog to " + p.getName() + ".", e);
            return false;
        }
    }

    /** Wide enough for a category's name on one line. */
    private static final int MENU_WIDTH = 200;

    /** @return false if it could not be shown */
    @SuppressWarnings({"rawtypes", "unchecked"})
    static boolean open(final Player p, final DialogForm form, final Consumer<Map<String, String>> confirmed, final Runnable cancelled) {
        if (manager == null) {
            return false;
        }
        try {
            final ConfirmationDialog dialog = manager.createConfirmationDialog();
            dialog.title(form.title);
            if (!form.description.isEmpty()) {
                dialog.body((Consumer<DialogBodyBuilder<?>>) body -> {
                    final TextBody<?> text = body.text();
                    text.text(form.description);
                });
            }
            for (final DialogForm.Field field : form.fields) {
                dialog.input(inputName(field.key), (Consumer<DialogInputBuilder>) input -> build(input, field));
            }
            dialog.yesAction((Consumer<DialogActionBuilder<?, ?>>) action -> {
                action.label(form.confirm);
                action.dynamicCustom(SUBMIT);
            });
            dialog.noAction((Consumer<DialogActionBuilder<?, ?>>) action -> {
                action.label(form.cancel);
                action.dynamicCustom(CANCEL);
            });
            dialog.canCloseWithEscape(form.closeWithEscape);
            //set even though closing is what it does anyway: UniDialog's Paper backend has no default
            //for it and hands Paper a null, which Paper's codec throws on
            dialog.afterAction(Dialog.AfterAction.CLOSE);

            //recorded before it is shown, so an answer cannot arrive with nothing to receive it
            pending.put(p.getUniqueId(), new Pending(values -> confirmed.accept(byKey(form, values)), cancelled, Collections.emptyList()));
            if (dialog.opener().open(p.getUniqueId())) {
                return true;
            }
            pending.remove(p.getUniqueId());
            return false;
        } catch (final Throwable e) {
            pending.remove(p.getUniqueId());
            //the caller falls back, but a dialog that never shows is worth saying so
            logger.log(Level.WARNING, "Couldn't show a dialog to " + p.getName() + ".", e);
            return false;
        }
    }

    /**
     * The name a field is sent under. Paper only allows letters, digits and underscores, which
     * rules out config paths such as {@code Stats.Leaderboard-Size}; {@link #byKey} maps them back.
     */
    private static String inputName(final String key) {
        return key.replaceAll("[^A-Za-z0-9_]", "_");
    }

    /** The answers under the keys the form's fields were added with, rather than their input names. */
    private static Map<String, String> byKey(final DialogForm form, final Map<String, String> values) {
        final Map<String, String> answers = new HashMap<>();
        if (values == null) {
            return answers;
        }
        for (final DialogForm.Field field : form.fields) {
            final String value = values.get(inputName(field.key));
            if (value != null) {
                answers.put(field.key, value);
            }
        }
        return answers;
    }

    private static void build(final DialogInputBuilder input, final DialogForm.Field field) {
        switch (field.kind) {
            case TEXT: {
                final TextInput<?> text = input.textInput();
                text.label(field.label);
                text.initial(field.text);
                text.maxLength(field.maxLength);
                break;
            }
            case TOGGLE: {
                final BooleanInput<?> toggle = input.booleanInput();
                toggle.label(field.label);
                toggle.initial(field.toggled);
                break;
            }
            case SLIDER: {
                final NumberRangeInput<?> slider = input.numberRangeInput();
                slider.label(field.label);
                slider.start(field.min);
                slider.end(field.max);
                //both set, never left null: the same Paper backend passes those straight through
                slider.step(field.step);
                slider.initial(field.value);
                break;
            }
        }
    }

    /** Takes the form off a player's screen, when what it asks is answered some other way. */
    static void close(final UUID uuid) {
        //dropped whether or not there is anything to clear, so a form closed with Escape is forgotten too
        if (pending.remove(uuid) != null && manager != null) {
            try {
                manager.clearDialog(uuid);
            } catch (final Throwable ignored) {
                //nothing on screen to clear
            }
        }
    }

    static void shutdown() {
        pending.clear();
        if (manager != null) {
            manager.unregister();
            manager = null;
        }
    }

    private static boolean hasClass(final String name) {
        try {
            Class.forName(name, false, DialogInput.class.getClassLoader());
            return true;
        } catch (final Throwable e) {
            return false;
        }
    }
}
