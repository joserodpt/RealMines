package joserodpt.realmines.api.mine.components;

public enum RMineSettings {
    BREAK_PERMISSION("settings.break-permission", "Mine break permission"),
    DISCARD_BREAK_ACTION_MESSAGES("settings.discard-break-action-messages", "Discard break action messages"),
    BLOCK_SETS_MODE("settings.block-sets-mode", "Block sets mode"),
    ;

    private final String key, description;

    RMineSettings(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getConfigKey() {
        return this.key;
    }

    public String getDescription() {
        return this.description;
    }
}