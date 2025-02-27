package com.gizmo.brennon.core.player;

public enum PlayerRank {
    OWNER("Owner", "§4", 100),
    ADMIN("Admin", "§c", 90),
    MOD("Mod", "§2", 80),
    HELPER("Helper", "§a", 70),
    VIP("VIP", "§6", 60),
    DEFAULT("Default", "§7", 0);

    private final String displayName;
    private final String color;
    private final int weight;

    PlayerRank(String displayName, String color, int weight) {
        this.displayName = displayName;
        this.color = color;
        this.weight = weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public int getWeight() {
        return weight;
    }

    public String getFormattedName() {
        return color + displayName;
    }

    public static PlayerRank fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DEFAULT;
        }
    }
}
