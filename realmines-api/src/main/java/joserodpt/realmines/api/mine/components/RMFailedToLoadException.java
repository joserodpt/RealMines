package joserodpt.realmines.api.mine.components;

public class RMFailedToLoadException extends Exception {
    private final String mineName;
    private final String reason;

    public RMFailedToLoadException(String mineName, String reason) {
        super("RealMines '" + mineName + "' did not load properly: " + reason);
        this.mineName = mineName;
        this.reason = reason;
    }

    public String getMineName() {
        return mineName;
    }

    public String getReason() {
        return reason;
    }
}