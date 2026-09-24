package joserodpt.realmines.api.config;

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

import joserodpt.realmines.api.utils.Text;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Every line the plugin says to a player, as a constant pointing at its route in language.yml.
 *
 * <p>Placeholders are filled with {@link #with(TranslatableLinePlaceholder, Object)}, which hands
 * back a new {@link Message} rather than changing the constant:</p>
 *
 * <pre>{@code
 * TranslatableLine.MINE_COUNTDOWN_SET.with(MINE, mine.getDisplayName()).with(TIME, seconds).send(p);
 * }</pre>
 *
 * <p>The values used to be stored on the constants themselves, which are shared by every caller -
 * so a value set for one message stayed behind for the next, a line could hold at most two, and
 * the same placeholder could not appear twice with different values.</p>
 */
public enum TranslatableLine {
    // Mines related messages
    MINE_RESET_STARTING("Mines.Reset.Starting"),
    MINE_RESET_PERCENTAGE("Mines.Reset.Percentage"),
    MINE_RESET_ANNOUNCEMENT("Mines.Reset.Announcement"),
    MINE_RESET_WARNING("Mines.Reset.Warning"),
    MINE_TELEPORT("Mines.Teleport"),
    MINE_TELEPORT_SET("Mines.Teleport-Set"),
    MINE_COUNTDOWN_SET("Mines.Countdown-Set"),
    MINE_COUNTDOWN_SET_UNSUCCESSFUL("Mines.Countdown-Set-Unsuccessful"),
    MINE_NO_TELEPORT_LOCATION("Mines.No-Teleport-Location"),
    // break actions
    MINE_BREAK_ACTION_GIVE_MONEY("Mines.Break-Actions.Give-Money"),
    MINE_BREAK_ACTION_DROP_ITEM("Mines.Break-Actions.Drop-Item"),
    MINE_BREAK_ACTION_GIVE_ITEM("Mines.Break-Actions.Give-Item"),
    MINE_BREAK_ACTION_INPUT_COMMAND("Mines.Break-Actions.Input.Command"),
    MINE_BREAK_ACTION_INPUT_AMOUNT("Mines.Break-Actions.Input.Amount"),
    MINE_BREAK_ACTION_INPUT_AMOUNT_ERROR("Mines.Break-Actions.Input.Amount-Error"),
    MINE_BREAK_ACTION_INPUT_CHANCE("Mines.Break-Actions.Input.Chance"),

    // GUI related messages
    GUI_RESET_NAME("GUI.Reset-Name"),
    GUI_FACES_NAME("GUI.Faces-Name"),
    GUI_FACES_ITEM_NAME("GUI.Faces.Name"),
    GUI_CHOOSE_NAME("GUI.Choose-Name"),
    GUI_SELECT_ICON_NAME("GUI.Select-Icon-Name"),
    GUI_PICK_NEW_BLOCK_NAME("GUI.Pick-New-Block-Name"),
    GUI_MINE_BLOCKS_NAME("GUI.Mine-Blocks-Name"),
    GUI_COLOR_PICKER_NAME("GUI.Color-Picker-Name"),

    //faltam as faces

    GUI_RESET_BY_PERCENTAGE_ON("GUI.Resets.Percentage-On.Name"),
    GUI_RESET_BY_PERCENTAGE_OFF("GUI.Resets.Percentage-Off.Name"),
    GUI_RESET_BY_TIME_ON("GUI.Resets.Time-On.Name"),
    GUI_RESET_BY_TIME_OFF("GUI.Resets.Time-Off.Name"),
    GUI_SCHEMATIC_NAME("GUI.Items.Schematic.Name"),
    GUI_FARM_NAME("GUI.Items.Farm.Name"),
    GUI_BLOCKS_NAME("GUI.Items.Blocks.Name"),
    GUI_RESETS_NAME("GUI.Items.Resets.Name"),
    GUI_TELEPORT_NAME("GUI.Items.Teleport.Name"),
    GUI_ICON_NAME("GUI.Items.Icon.Name"),
    GUI_NAME_NAME("GUI.Items.Name.Name"),
    GUI_DISPLAYNAME_NAME("GUI.Items.Displayname.Name"),
    GUI_CLEAR_NAME("GUI.Items.Clear.Name"),
    GUI_BOUNDARIES_NAME("GUI.Items.Boundaries.Name"),
    GUI_MINE_COLOR_NAME("GUI.Items.MineColor.Name"),
    GUI_ADD_ITEMS_NAME("GUI.Items.Add.Name"),
    GUI_SEARCH_ITEM_NAME("GUI.Items.Search.Name"),
    GUI_NEXT_PAGE_NAME("GUI.Items.Next.Name"),
    GUI_PREVIOUS_PAGE_NAME("GUI.Items.Back.Name"),
    GUI_GO_BACK_NAME("GUI.Items.Go-Back.Name"),
    GUI_CLOSE_NAME("GUI.Items.Close.Name"),
    GUI_MINE_BLOCK_NAME("GUI.Items.Mine-Block.Block.Name"),
    GUI_SCHEMATIC_BLOCK_NAME("GUI.Items.Mine-Block.Schematic-Block.Name"),
    GUI_FARM_ITEM_NAME("GUI.Items.Farm-Item.Name"),
    GUI_NO_BLOCKS_NAME("GUI.Items.No-Blocks.Name"),
    GUI_NO_MINES_FOUND_NAME("GUI.Items.No-Mines-Found.Name"),
    GUI_MINE_DUPLICATE("GUI.Items.Mine.Duplicate"),
    GUI_PICK_NAME("GUI.Items.Pick.Name"),
    GUI_COLORS_RED("GUI.Items.Colors.Red"),
    GUI_COLORS_GREEN("GUI.Items.Colors.Green"),
    GUI_COLORS_BLUE("GUI.Items.Colors.Blue"),
    GUI_COLORS_BROWN("GUI.Items.Colors.Brown"),
    GUI_COLORS_GRAY("GUI.Items.Colors.Gray"),
    GUI_COLORS_WHITE("GUI.Items.Colors.White"),
    GUI_COLORS_ORANGE("GUI.Items.Colors.Orange"),
    GUI_COLORS_YELLOW("GUI.Items.Colors.Yellow"),
    GUI_COLORS_PURPLE("GUI.Items.Colors.Purple"),

    // System related messages
    SYSTEM_PLAYER_ONLY("System.Player-Only"),
    SYSTEM_BOUNDARIES_NOT_SET("System.Boundaries-Not-Set"),
    SYSTEM_CANT_ADD_ITEM("System.Cant-Add-Item"),
    SYSTEM_MINE_CREATED("System.Mine-Created"),
    SYSTEM_RELOADED("System.Reloaded"),
    SYSTEM_MINE_TASK_CREATED("System.Mine-Task-Created"),
    SYSTEM_MINE_TASK_EXISTS("System.Mine-Task-Exists"),
    SYSTEM_MINE_EXISTS("System.Mine-Exists"),
    SYSTEM_MINE_LINKED("System.Mine-Linked"),
    SYSTEM_MINE_UNLINKED("System.Mine-Unlinked"),
    SYSTEM_MINE_DELETED("System.Mine-Deleted"),
    SYSTEM_MINE_TASK_DOESNT_EXIST("System.Mine-Task-Doesnt-Exist"),
    SYSTEM_MINE_DOESNT_EXIST("System.Mine-Doesnt-Exist"),
    SYSTEM_MINE_RENAMED("System.Mine-Renamed"),
    SYSTEM_MINE_DUPLICATED("System.Mine-Duplicated"),
    SYSTEM_MINE_CLEAR("System.Mine-Clear"),
    SYSTEM_STOPPED_MINE_TASKS("System.Stopped-Mine-Tasks"),
    SYSTEM_STARTED_MINE_TASKS("System.Started-Mine-Tasks"),
    SYSTEM_REMOVE("System.Remove"),
    SYSTEM_REGION_UPDATED("System.Region-Updated"),
    SYSTEM_DEFAULT_LOCATION_SET("System.Default-Location-Set"),
    SYSTEM_ADD_BLOCKS("System.Add-Blocks"),
    SYSTEM_BLOCK_COUNT("System.Block-Count"),
    SYSTEM_BLOCKS_ADDED("System.Blocks-Added"),
    SYSTEM_SILENT_OFF("System.Silent-Off"),
    SYSTEM_SILENT_ON("System.Silent-On"),
    SYSTEM_UPDATE_FOUND("System.Update-Found"),
    SYSTEM_INPUT_CANCELLED("System.Input-Cancelled"),
    SYSTEM_ERROR_OCCURRED("System.Error-Occurred"),
    SYSTEM_ERROR_PERMISSION("System.Error-Permission"),
    SYSTEM_ERROR_BREAK_PERMISSION("System.Error-Break-Permission"),
    SYSTEM_ERROR_COMMAND("System.Error-Command"),
    SYSTEM_ERROR_USAGE("System.Error-Usage"),
    SYSTEM_NOTHING_FOUND("System.Nothing-Found"),
    SYSTEM_INVALID_SCHEMATIC("System.Invalid-Schematic"),
    SYSTEM_INPUT_SCHEMATIC("System.Input-Schematic"),
    SYSTEM_INPUT_PARSE("System.Input-Parse"),
    SYSTEM_INPUT_LIMIT_ERROR("System.Input-Limit-Error"),
    SYSTEM_INPUT_PERCENTAGE_ERROR("System.Input-Percentage-Error"),
    SYSTEM_INPUT_PERCENTAGE_ERROR_GREATER("System.Input-Percentage-Error-Greater"),
    SYSTEM_INPUT_PERCENTAGE_ERROR_LOWER("System.Input-Percentage-Error-Lower"),
    SYSTEM_INPUT_LIMIT_ERROR_GREATER("System.Input-Limit-Error-Greater"),
    SYSTEM_INPUT_SECONDS("System.Input-Seconds"),
    SYSTEM_INPUT_MINE_NAME("System.Input-Mine-Name"),
    SYSTEM_INVALID_MINE_NAME("System.Invalid-Mine-Name"),
    SYSTEM_PERCENTAGE_MODIFIED("System.Percentage-Modified"),
    SYSTEM_TIME_MODIFIED("System.Time-Modified"),
    SYSTEM_NO_CONVERTER_AVAILABLE("System.No-Converter-Available"),
    SYSTEM_MINE_FREEZE("System.Mine-Freeze"),
    // Signs related messages
    SIGNS_SETTING_NOT_FOUND("Signs.Setting-Not-Found"),
    SIGNS_MINE_NOT_FOUND("Signs.Mine-Not-Found"),
    SIGNS_MINED_ON("Signs.Mined-On"),
    SIGNS_MINED_BLOCKS_ON("Signs.Mined-Blocks-On"),
    SIGNS_BLOCKS_ON("Signs.Blocks-On"),

    SIGNS_LEFT_ON("Signs.Left-On"),

    // Achievements and stats
    ACHIEVEMENT_UNLOCKED("Achievements.Unlocked"),
    ACHIEVEMENT_BROADCAST("Achievements.Broadcast"),
    ACHIEVEMENT_REWARD("Achievements.Reward"),
    ACHIEVEMENTS_DISABLED("Achievements.Disabled"),
    ACHIEVEMENTS_NONE_CONFIGURED("Achievements.None-Configured"),
    STATS_HEADER("Stats.Header"),
    STATS_TOTAL_MINED("Stats.Total-Mined"),
    STATS_ACHIEVEMENTS("Stats.Achievements"),
    STATS_TOP_MATERIAL("Stats.Top-Material"),
    STATS_NO_DATA("Stats.No-Data"),
    STATS_PLAYER_NOT_FOUND("Stats.Player-Not-Found"),
    PRIVATE_MINE_DISABLED("PrivateMines.Disabled"),
    PRIVATE_MINE_NO_TEMPLATES("PrivateMines.No-Templates"),
    PRIVATE_MINE_TEMPLATE_NOT_FOUND("PrivateMines.Template-Not-Found"),
    PRIVATE_MINE_TEMPLATE_CREATED("PrivateMines.Template-Created"),
    PRIVATE_MINE_TEMPLATE_DELETED("PrivateMines.Template-Deleted"),
    PRIVATE_MINE_TEMPLATE_CREATE_FAILED("PrivateMines.Template-Create-Failed"),
    PRIVATE_MINE_CLAIMED("PrivateMines.Claimed"),
    PRIVATE_MINE_CLAIM_COST("PrivateMines.Claim-Cost"),
    PRIVATE_MINE_ALREADY_OWNED("PrivateMines.Already-Owned"),
    PRIVATE_MINE_LIMIT_REACHED("PrivateMines.Limit-Reached"),
    PRIVATE_MINE_INSUFFICIENT_FUNDS("PrivateMines.Insufficient-Funds"),
    PRIVATE_MINE_NO_ECONOMY("PrivateMines.No-Economy"),
    PRIVATE_MINE_WORLD_MISSING("PrivateMines.World-Missing"),
    PRIVATE_MINE_NO_FREE_SLOT("PrivateMines.No-Free-Slot"),
    PRIVATE_MINE_UNSUPPORTED_TYPE("PrivateMines.Unsupported-Type"),
    PRIVATE_MINE_CLAIM_FAILED("PrivateMines.Claim-Failed"),
    PRIVATE_MINE_NO_MINE("PrivateMines.No-Mine"),
    PRIVATE_MINE_NOT_YOURS("PrivateMines.Not-Yours"),
    PRIVATE_MINE_CANT_BUILD("PrivateMines.Cant-Build"),
    PRIVATE_MINE_CANT_DUPLICATE("PrivateMines.Cant-Duplicate"),
    PRIVATE_MINE_RELEASED("PrivateMines.Released"),
    PRIVATE_MINE_REFUNDED("PrivateMines.Refunded"),
    PRIVATE_MINE_TRUSTED_ADDED("PrivateMines.Trusted-Added"),
    PRIVATE_MINE_TRUSTED_REMOVED("PrivateMines.Trusted-Removed"),
    PRIVATE_MINE_TRUSTED_ALREADY("PrivateMines.Trusted-Already"),
    PRIVATE_MINE_TRUSTED_NOT_FOUND("PrivateMines.Trusted-Not-Found"),
    PRIVATE_MINE_TRUSTED_LIMIT("PrivateMines.Trusted-Limit"),
    PRIVATE_MINE_TRUSTED_EMPTY("PrivateMines.Trusted-Empty"),
    PRIVATE_MINE_TRUSTED_HEADER("PrivateMines.Trusted-Header"),
    PRIVATE_MINE_EXTENDED("PrivateMines.Extended"),
    PRIVATE_MINE_NOT_TIME_LIMITED("PrivateMines.Not-Time-Limited"),
    PRIVATE_MINE_EXPIRED("PrivateMines.Expired"),
    PRIVATE_MINE_INFO_HEADER("PrivateMines.Info-Header"),
    PRIVATE_MINE_INFO_LINE("PrivateMines.Info-Line"),
    PRIVATE_MINE_INFO_EXPIRES("PrivateMines.Info-Expires"),
    PRIVATE_MINE_LIST_EMPTY("PrivateMines.List-Empty"),
    PRIVATE_MINE_LIST_HEADER("PrivateMines.List-Header"),
    PRIVATE_MINE_LIST_LINE("PrivateMines.List-Line"),
    PRIVATE_MINE_PLAYER_NOT_FOUND("PrivateMines.Player-Not-Found"),
    PRIVATE_MINE_NEVER_EXPIRES("PrivateMines.Never-Expires");

    private final String configPath;

    TranslatableLine(String configPath) {
        this.configPath = configPath;
    }

    /** Starts a message from this line with one placeholder filled; chain more with {@link Message#with}. */
    public Message with(TranslatableLinePlaceholder placeholder, Object value) {
        return new Message(this.configPath).with(placeholder, value);
    }

    /** The line with no placeholders filled, coloured. */
    public String get() {
        return new Message(this.configPath).get();
    }

    public void send(CommandSender p) {
        new Message(this.configPath).send(p);
    }

    /** The tokens a line in language.yml may contain. {@code NAME} is written {@code %name%}. */
    public enum TranslatableLinePlaceholder {
        NAME, WORLD, INPUT, MINE, TIME, MONEY, FACE, MATERIAL, VALUE, PERCENTAGE, AGE,
        REMAININGBLOCKS, TOTALBLOCKS, BAR, TASK, DELAY, OBJECT, COUNT, PLAYER, TEMPLATE;

        private final String token = "%" + this.name().toLowerCase() + "%";

        public String getToken() {
            return this.token;
        }
    }

    /**
     * One line with its placeholders filled in. A new one per message and never shared, so nothing
     * set here can leak into the next.
     */
    public static final class Message {
        private final String configPath;
        private final Map<TranslatableLinePlaceholder, String> values = new LinkedHashMap<>();

        private Message(String configPath) {
            this.configPath = configPath;
        }

        /** Fills a placeholder. Setting the same one again replaces its value. */
        public Message with(TranslatableLinePlaceholder placeholder, Object value) {
            this.values.put(placeholder, String.valueOf(value));
            return this;
        }

        public String get() {
            String s = RMLanguageConfig.file().getString(this.configPath);
            for (final Map.Entry<TranslatableLinePlaceholder, String> entry : this.values.entrySet()) {
                s = s.replace(entry.getKey().getToken(), entry.getValue());
            }
            return Text.color(s);
        }

        public void send(CommandSender p) {
            Text.send(p, this.get());
        }
    }
}
