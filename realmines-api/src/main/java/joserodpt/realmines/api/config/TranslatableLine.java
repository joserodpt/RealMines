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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.utils.Text;
import org.bukkit.command.CommandSender;

public enum TranslatableLine {

    REGION_CREATED("Region.Created"),
    REGION_NAME_EMPTY("Region.Name-Empty"),
    REGION_NAME_DUPLICATE("Region.Name-Duplicate"),
    REGION_NON_EXISTENT_NAME("Region.Non-Existent-Name", ReplacableVar.NAME),
    REGION_NOT_IN_WORLD("Region.Not-In-World", ReplacableVar.NAME),
    REGION_DISPLAY_NAME_CHANGED("Region.Display-Name-Changed", ReplacableVar.INPUT),
    REGION_CANT_DELETE_INFINITE("Region.Cant-Delete-Infinite", ReplacableVar.NAME),
    REGION_IMPORTED_FROM_EXTERNAL("Region.Imported-From-External", ReplacableVar.NAME),

    REGION_DELETED("Region.Deleted", ReplacableVar.NAME),
    REGION_CANT_BREAK_BLOCK("Region.Cant-Break-Block"),
    REGION_CANT_PLACE_BLOCK("Region.Cant-Place-Block"),
    REGION_CANT_DROP_ITEMS("Region.Cant-Drop-Items"),
    REGION_CANT_PICKUP_ITEMS("Region.Cant-Pickup-Items"),
    REGION_CANT_ENTER_HERE("Region.Cant-Enter-Here"),
    REGION_CANT_INTERACT_BLOCKS("Region.Cant-Interact-Blocks"),
    REGION_CANT_INTERACT_CONTAINER("Region.Cant-Interact-Container"),
    REGION_CANT_INTERACT_CRAFTING_TABLES("Region.Cant-Interact-Crafting-Tables"),
    REGION_CANT_INTERACT_HOPPER("Region.Cant-Interact-Hopper"),
    REGION_CANT_OPEN_CHEST("Region.Cant-Open-Chest"),
    REGION_CANT_PVP("Region.Cant-PVP"),
    REGION_CANT_PVE("Region.Cant-PVE"),
    REGION_CANT_CHAT("Region.Cant-Chat"),
    REGION_CANT_CONSUME("Region.Cant-Consume"),
    REGION_DISABLED_END_PORTAL("Region.Disabled-End-Portal"),
    REGION_DISABLED_NETHER_PORTAL("Region.Disabled-Nether-Portal"),
    REGION_SET_BOUNDS("Region.Region-Set-Bounds"),

    //WORLD

    WORLD_BEING_IMPORTED("World.Being-Imported", ReplacableVar.NAME),
    WORLD_IMPORTED("World.Imported", ReplacableVar.NAME),
    WORLD_FAILED_TO_IMPORT("World.Failed-To-Import", ReplacableVar.NAME),
    WORLD_BEING_CREATED("World.Being-Created", ReplacableVar.NAME),
    WORLD_CREATED("World.Created", ReplacableVar.NAME),
    WORLD_FAILED_TO_CREATE("World.Failed-To-Create", ReplacableVar.NAME),
    WORLD_ALREADY_LOADED("World.Already-Loaded"),
    WORLD_LOADED("World.Loaded", ReplacableVar.NAME),
    WORLD_UNLOAD_DEFAULT_WORLDS("World.Unload-Default-Worlds"),
    WORLD_ALREADY_UNLOADED("World.Already-Unloaded"),
    WORLD_BEING_UNLOADED("World.Being-Unloaded", ReplacableVar.NAME),
    WORLD_UNLOADED("World.Unloaded"),
    WORLD_DELETE_DEFAULT_WORLDS("World.Delete-Default-Worlds"),
    WORLD_BEING_DELETED("World.Being-Deleted", ReplacableVar.NAME),
    WORLD_DELETED("World.Deleted", ReplacableVar.NAME),
    WORLD_UNREGISTERED("World.Unregistered", ReplacableVar.NAME),
    WORLD_NO_WORLD_NAMED("World.No-World-Named", ReplacableVar.WORLD),
    WORLD_NAME_EMPTY("World.Name-Empty"),
    WORLD_INVALID_TYPE("World.Invalid-Type", ReplacableVar.INPUT),

    //MENU
    MENU_UNLOADED_WORLD("Menu.Unloaded-World"),
    SEARCH_NO_RESULTS("Search.No-Results"),
    SELECTION_NONE("Selection.None"),
    INPUT_NOT_NUMBER("Input.Not-Number"),

    PRIORITY_CHANGED("Priority.Changed", ReplacableVar.INPUT),

    SYSTEM_RELOADED("System.Reloaded"),
    SYSTEM_NOT_FOUND("System.Not-Found", ReplacableVar.NAME),
    SYSTEM_ERROR_REMOVING_FILES("System.Error-Removing-Files", ReplacableVar.NAME);


    private final String configPath;
    private ReplacableVar v1 = null;

    TranslatableLine(String configPath) {
        this.configPath = configPath;
    }
    TranslatableLine(String configPath, ReplacableVar v1) {
        this.configPath = configPath;
        this.v1 = v1;
    }

    public TranslatableLine setV1(ReplacableVar v1) {
        this.v1 = v1;
        return this;
    }

    public String get() {
        String s = RMLanguageConfig.file().getString(this.configPath);
        if (v1 != null) {
            s = s.replace(v1.getKey(), v1.getVal());
        }

        return s;
    }

    public void send(CommandSender p) {
        Text.send(p, this.get());
    }

    public enum ReplacableVar {

        NAME("%name%"),
        WORLD("%world%"),
        INPUT("%input%");

        private String key;
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
