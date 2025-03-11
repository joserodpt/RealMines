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
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.utils.Text;
import org.bukkit.command.CommandSender;

public enum TranslatableLine {
    // Mines related messages
    MINE_RESET_STARTING("Mines.Reset.Starting", ReplacableVar.MINE),
    MINE_RESET_PERCENTAGE("Mines.Reset.Percentage"),
    MINE_RESET_ANNOUNCEMENT("Mines.Reset.Announcement", ReplacableVar.MINE),
    MINE_RESET_WARNING("Mines.Reset.Warning", ReplacableVar.MINE),
    MINE_TELEPORT("Mines.Teleport", ReplacableVar.MINE),
    MINE_TELEPORT_SET("Mines.Teleport-Set", ReplacableVar.MINE),
    MINE_NO_TELEPORT_LOCATION("Mines.No-Teleport-Location"),
    // break actions
    MINE_BREAK_ACTION_GIVE_MONEY("Mines.Break-Actions.Give-Money", ReplacableVar.MONEY),
    MINE_BREAK_ACTION_DROP_ITEM("Mines.Break-Actions.Drop-Item"),
    MINE_BREAK_ACTION_GIVE_ITEM("Mines.Break-Actions.Give-Item"),
    MINE_BREAK_ACTION_INPUT_COMMAND("Mines.Break-Actions.Input.Command"),
    MINE_BREAK_ACTION_INPUT_AMOUNT("Mines.Break-Actions.Input.Amount"),
    MINE_BREAK_ACTION_INPUT_AMOUNT_ERROR("Mines.Break-Actions.Input.Amount-Error"),
    MINE_BREAK_ACTION_INPUT_CHANCE("Mines.Break-Actions.Input.Chance"),

    // GUI related messages
    GUI_RESET_NAME("GUI.Reset-Name", ReplacableVar.MINE),
    GUI_FACES_NAME("GUI.Faces-Name"),
    GUI_FACES_ITEM_NAME("GUI.Faces.Name"),
    GUI_CHOOSE_NAME("GUI.Choose-Name", ReplacableVar.MINE),
    GUI_SELECT_ICON_NAME("GUI.Select-Icon-Name", ReplacableVar.MINE),
    GUI_PICK_NEW_BLOCK_NAME("GUI.Pick-New-Block-Name"),
    GUI_MINE_BLOCKS_NAME("GUI.Mine-Blocks-Name", ReplacableVar.MINE),
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
    SYSTEM_MINE_CREATED("System.Mine-Created", ReplacableVar.MINE),
    SYSTEM_RELOADED("System.Reloaded"),
    SYSTEM_MINE_TASK_CREATED("System.Mine-Task-Created", ReplacableVar.TASK, ReplacableVar.DELAY),
    SYSTEM_MINE_TASK_EXISTS("System.Mine-Task-Exists"),
    SYSTEM_MINE_EXISTS("System.Mine-Exists"),
    SYSTEM_MINE_LINKED("System.Mine-Linked"),
    SYSTEM_MINE_UNLINKED("System.Mine-Unlinked"),
    SYSTEM_MINE_DELETED("System.Mine-Deleted"),
    SYSTEM_MINE_TASK_DOESNT_EXIST("System.Mine-Task-Doesnt-Exist"),
    SYSTEM_MINE_DOESNT_EXIST("System.Mine-Doesnt-Exist"),
    SYSTEM_MINE_RENAMED("System.Mine-Renamed", ReplacableVar.NAME),
    SYSTEM_MINE_CLEAR("System.Mine-Clear"),
    SYSTEM_STOPPED_MINE_TASKS("System.Stopped-Mine-Tasks"),
    SYSTEM_STARTED_MINE_TASKS("System.Started-Mine-Tasks"),
    SYSTEM_REMOVE("System.Remove", ReplacableVar.OBJECT),
    SYSTEM_REGION_UPDATED("System.Region-Updated"),
    SYSTEM_ADD_BLOCKS("System.Add-Blocks"),
    SYSTEM_BLOCK_COUNT("System.Block-Count", ReplacableVar.COUNT),
    SYSTEM_BLOCKS_ADDED("System.Blocks-Added", ReplacableVar.COUNT),
    SYSTEM_SILENT_OFF("System.Silent-Off", ReplacableVar.MINE),
    SYSTEM_SILENT_ON("System.Silent-On", ReplacableVar.MINE),
    SYSTEM_UPDATE_FOUND("System.Update-Found"),
    SYSTEM_INPUT_CANCELLED("System.Input-Cancelled"),
    SYSTEM_ERROR_OCCURRED("System.Error-Occurred"),
    SYSTEM_ERROR_PERMISSION("System.Error-Permission"),
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
    SYSTEM_PERCENTAGE_MODIFIED("System.Percentage-Modified", ReplacableVar.VALUE),
    SYSTEM_TIME_MODIFIED("System.Time-Modified", ReplacableVar.VALUE),
    SYSTEM_NO_CONVERTER_AVAILABLE("System.No-Converter-Available"),
    SYSTEM_MINE_FREEZE("System.Mine-Freeze"),
    // Signs related messages
    SIGNS_SETTING_NOT_FOUND("Signs.Setting-Not-Found"),
    SIGNS_MINE_NOT_FOUND("Signs.Mine-Not-Found"),
    SIGNS_MINED_ON("Signs.Mined-On"),
    SIGNS_MINED_BLOCKS_ON("Signs.Mined-Blocks-On"),
    SIGNS_BLOCKS_ON("Signs.Blocks-On"),

    SIGNS_LEFT_ON("Signs.Left-On");

    private final String configPath;
    private ReplacableVar v1, v2 = null;

    TranslatableLine(String configPath) {
        this.configPath = configPath;
    }

    TranslatableLine(String configPath, ReplacableVar v1) {
        this.configPath = configPath;
        this.v1 = v1;
    }

    TranslatableLine(String configPath, ReplacableVar v1, ReplacableVar v2) {
        this.configPath = configPath;
        this.v1 = v1;
        this.v2 = v2;
    }

    public TranslatableLine setV1(ReplacableVar v1) {
        this.v1 = v1;
        return this;
    }

    public TranslatableLine setV2(ReplacableVar v2) {
        this.v2 = v2;
        return this;
    }

    public String get() {
        String s = RMLanguageConfig.file().getString(this.configPath);
        if (v1 != null) {
            s = s.replace(v1.getKey(), v1.getVal());
        }
        if (v2 != null) {
            s = s.replace(v2.getKey(), v2.getVal());
        }

        return Text.color(s);
    }

    public void send(CommandSender p) {
        Text.send(p, this.get());
    }

    public enum ReplacableVar {

        NAME("%name%"),
        WORLD("%world%"),
        INPUT("%input%"),
        MINE("%mine%"),
        TIME("%time%"),
        MONEY("%money%"),
        FACE("%face%"),
        MATERIAL("%material%"),
        VALUE("%value%"),
        PERCENTAGE("%percentage%"),
        AGE("%age%"),
        REMAININGBLOCKS("%remainingblocks%"),
        TOTALBLOCKS("%totalblocks%"),
        BAR("%bar%"),
        TASK("%task%"),
        DELAY("%delay%"),
        OBJECT("%object%"),
        COUNT("%count%");

        private final String key;
        private String val;

        ReplacableVar(String key) {
            this.key = key;
        }

        public ReplacableVar eq(String val) {
            this.val = val;
            return this;
        }

        public String getKey() {
            return key;
        }

        public String getVal() {
            return val;
        }
    }

}
